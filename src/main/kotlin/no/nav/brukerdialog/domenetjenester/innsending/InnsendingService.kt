package no.nav.brukerdialog.domenetjenester.innsending

import jakarta.validation.ConstraintViolationException
import jakarta.validation.Validation
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.common.formaterStatuslogging
import no.nav.brukerdialog.integrasjon.k9mellomlagring.K9DokumentMellomlagringService
import no.nav.brukerdialog.kafka.KafkaProducerService
import no.nav.brukerdialog.mellomlagring.dokument.Dokument
import no.nav.brukerdialog.mellomlagring.dokument.DokumentEier
import no.nav.brukerdialog.mellomlagring.dokument.valider
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.brukerdialog.oppslag.soker.SøkerService
import no.nav.brukerdialog.utils.TokenUtils.personIdent
import no.nav.brukerdialog.validation.ParameterType
import no.nav.brukerdialog.validation.ValidationErrorResponseException
import no.nav.brukerdialog.validation.ValidationProblemDetails
import no.nav.brukerdialog.validation.Violation
import no.nav.brukerdialog.ytelse.Ytelse
import no.nav.k9.ettersendelse.Ettersendelse
import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarnSøknadValidator
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import org.springframework.web.ErrorResponseException

@Service
@Validated
class InnsendingService(
    private val søkerService: SøkerService,
    private val kafkaProdusent: KafkaProducerService,
    private val k9DokumentMellomlagringService: K9DokumentMellomlagringService,
    private val springTokenValidationContextHolder: SpringTokenValidationContextHolder,
) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
        val validator = Validation.buildDefaultValidatorFactory().validator
    }

    internal suspend fun registrer(
        innsending: Innsending,
        metadata: MetaInfo,
    ) {
        forsikreValidert(innsending)
        val søker = søkerService.hentSøker()
        logger.info(formaterStatuslogging(innsending.ytelse(), innsending.innsendingId(), "registreres."))

        innsending.valider()
        val k9Format = innsending.somK9Format(søker, metadata)
        k9Format?.let { validerK9Format(innsending, it) }

        if (innsending.inneholderVedlegg()) registrerSøknadMedVedlegg(
            innsending,
            søker,
            k9Format,
            metadata
        )
        else registrerSøknadUtenVedlegg(innsending, søker, k9Format, metadata)
    }

    fun forsikreValidert(innsending: Innsending) {
        logger.info("Validerer innsending...")

        forsikreInnloggetBrukerErSøker(innsending)

        val violations = validator.validate(innsending)
        if (violations.isNotEmpty()) {
            throw ConstraintViolationException(violations)
        }
        logger.info("Innsending validert")
    }

    fun forsikreInnloggetBrukerErSøker(innsending: Innsending) {
        val søkerNorskIdent = innsending.søkerNorskIdent()
        if (!søkerNorskIdent.isNullOrBlank()) { // TODO: fjern isNullOrBlank() når søkerNorskIdent() returnerer String
            val innloggetPersonIdent = springTokenValidationContextHolder.personIdent()
            if (innloggetPersonIdent != søkerNorskIdent) {
                val problemDetail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED)
                problemDetail.title = "Innsendingen er ikke gyldig for innlogget bruker."
                problemDetail.detail = "Innlogget bruker er ikke samme person som innsendingen er registrert på."
                val exception = ErrorResponseException(HttpStatus.UNAUTHORIZED, problemDetail, null)
                logger.error("Innsendingen er ikke gyldig for innlogget bruker.", exception)
                throw exception
            }
        }
    }

    private fun registrerSøknadUtenVedlegg(
        innsending: Innsending,
        søker: Søker,
        k9Format: no.nav.k9.søknad.Innsending?,
        metadata: MetaInfo,
    ) {
        try {
            val komplettInnsending = innsending.somKomplettSøknad(søker, k9Format)
            kafkaProdusent.produserKafkaMelding(
                metadata,
                komplettInnsending,
                innsending.ytelse()
            )
        } catch (exception: Exception) {
            logger.error("Feilet ved å legge melding på Kafka.", exception)
            throw MeldingRegistreringFeiletException("Feilet ved å legge melding på Kafka")
        }
    }

    private suspend fun registrerSøknadMedVedlegg(
        innsending: Innsending,
        søker: Søker,
        k9Format: no.nav.k9.søknad.Innsending?,
        metadata: MetaInfo,
    ) {
        logger.info("Validerer ${innsending.vedlegg().size} vedlegg.")
        val dokumentEier = søker.somDokumentEier()
        val vedlegg = k9DokumentMellomlagringService.hentDokumenter(innsending.vedlegg(), dokumentEier)
        validerVedlegg(innsending, vedlegg)

        persisterVedlegg(innsending, dokumentEier)

        try {
            val komplettInnsending = innsending.somKomplettSøknad(søker, k9Format, vedlegg.map { it.title })
            kafkaProdusent.produserKafkaMelding(
                metadata,
                komplettInnsending,
                innsending.ytelse()
            )
        } catch (exception: Exception) {
            logger.error("Feilet ved å legge melding på Kafka.", exception)
            logger.info("Fjerner hold på persisterte vedlegg")
            fjernHoldPåPersisterteVedlegg(innsending, dokumentEier)
            throw MeldingRegistreringFeiletException("Feilet ved å legge melding på Kafka")
        }
    }

    fun validerK9Format(
        innsending: Innsending,
        k9Format: no.nav.k9.søknad.Innsending,
    ) {
        val feil = when (k9Format) {
            is Søknad -> {
                when (innsending.ytelse()) {
                    Ytelse.ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN -> {
                        requireNotNull(innsending.gyldigeEndringsPerioder()) {
                            "GyldigeEndringsPerioder kan ikke være null for ${Ytelse.ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN}"
                        }
                        val søknadValidator = innsending.søknadValidator() as PleiepengerSyktBarnSøknadValidator
                        søknadValidator.valider(k9Format, innsending.gyldigeEndringsPerioder())
                    }

                    else -> {
                        innsending.søknadValidator()?.valider(k9Format)
                    }
                }
            }

            is Ettersendelse -> innsending.ettersendelseValidator()?.valider(k9Format)
            else -> null
        }?.map {
            logger.error("${it.felt} feilet pga. ${it.feilkode}")
            Violation(
                parameterName = it.felt,
                parameterType = ParameterType.ENTITY,
                reason = it.feilmelding,
                invalidValue = "K9-format valideringsfeil"
            )
        }?.toMutableSet()

        if (!feil.isNullOrEmpty()) {
            throw ValidationErrorResponseException(ValidationProblemDetails(feil))
        }
    }

    private fun validerVedlegg(innsending: Innsending, vedlegg: List<Dokument>) {
        logger.info("Validerer vedlegg")
        vedlegg.valider("vedlegg", innsending.vedlegg())
    }

    private suspend fun persisterVedlegg(innsending: Innsending, eier: DokumentEier) {
        logger.info("Persisterer vedlegg")
        k9DokumentMellomlagringService.persisterDokumenter(innsending.vedlegg(), eier)
    }

    private suspend fun fjernHoldPåPersisterteVedlegg(innsending: Innsending, eier: DokumentEier) {
        if (innsending.inneholderVedlegg()) {
            logger.info("Fjerner hold på persisterte vedlegg")
            k9DokumentMellomlagringService.fjernHoldPåPersisterteDokumenter(innsending.vedlegg(), eier)
        }
    }
}
