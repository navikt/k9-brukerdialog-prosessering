package no.nav.k9brukerdialogprosessering.api.ytelse.ettersending.domene

import no.nav.k9.ettersendelse.Ettersendelse
import no.nav.k9.ettersendelse.EttersendelseType
import no.nav.k9.ettersendelse.EttersendelseValidator
import no.nav.k9.søknad.SøknadValidator
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9brukerdialogapi.ytelse.ettersending.domene.KomplettEttersendelse
import no.nav.k9brukerdialogapi.ytelse.ettersending.domene.Pleietrengende
import no.nav.k9brukerdialogapi.ytelse.ettersending.domene.Søknadstype
import no.nav.k9brukerdialogapi.ytelse.ettersending.domene.valider
import no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.domene.hentIdentitetsnummerForBarn
import no.nav.k9brukerdialogprosessering.api.innsending.Innsending
import no.nav.k9brukerdialogprosessering.api.ytelse.Ytelse
import no.nav.k9brukerdialogprosessering.common.MetaInfo
import no.nav.k9brukerdialogprosessering.mellomlagring.dokument.dokumentId
import no.nav.k9brukerdialogprosessering.oppslag.barn.BarnOppslag
import no.nav.k9brukerdialogprosessering.oppslag.soker.Søker
import no.nav.k9brukerdialogprosessering.utils.krever
import no.nav.k9brukerdialogprosessering.validation.ValidationErrorResponseException
import no.nav.k9brukerdialogprosessering.validation.ValidationProblemDetailsString
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

class Ettersendelse(
    internal val søknadId: String = UUID.randomUUID().toString(),
    private val språk: String,
    private val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    internal val vedlegg: List<URL>,
    private val beskrivelse: String? = null,
    internal val søknadstype: Søknadstype,
    internal val ettersendelsesType: EttersendelseType,
    internal val pleietrengende: Pleietrengende? = null,
    private val harBekreftetOpplysninger: Boolean,
    private val harForståttRettigheterOgPlikter: Boolean,
) : Innsending {

    override fun valider() = mutableListOf<String>().apply {
        if (ettersendelsesType == EttersendelseType.LEGEERKLÆRING) {
            krever(pleietrengende != null, "Pleietrengende må være satt dersom ettersendelsen gjelder legeerklæring")
        }

        if (pleietrengende != null) addAll(pleietrengende.valider("Pleietrengende"))

        krever(harForståttRettigheterOgPlikter, "harForståttRettigheterOgPlikter må være true")
        krever(harBekreftetOpplysninger, "harBekreftetOpplysninger må være true")
        krever(vedlegg.isNotEmpty(), "Liste over vedlegg kan ikke være tom")
        if (isNotEmpty()) throw ValidationErrorResponseException(ValidationProblemDetailsString(this))
    }

    override fun somKomplettSøknad(
        søker: Søker,
        k9Format: no.nav.k9.søknad.Innsending?,
        titler: List<String>,
    ): KomplettEttersendelse {
        requireNotNull(k9Format)
        return KomplettEttersendelse(
            søker = søker,
            språk = språk,
            mottatt = mottatt,
            vedleggId = vedlegg.map { it.toURI().dokumentId() },
            søknadId = søknadId,
            harForståttRettigheterOgPlikter = harForståttRettigheterOgPlikter,
            harBekreftetOpplysninger = harBekreftetOpplysninger,
            beskrivelse = beskrivelse,
            søknadstype = søknadstype,
            ettersendelsesType = ettersendelsesType,
            pleietrengende = pleietrengende,
            titler = titler,
            k9Format = k9Format as Ettersendelse
        )
    }

    override fun somK9Format(søker: Søker, metadata: MetaInfo): Ettersendelse {
        val ettersendelse = Ettersendelse.builder()
            .søknadId(SøknadId(søknadId))
            .mottattDato(mottatt)
            .søker(søker.somK9Søker())
            .ytelse(søknadstype.somK9Ytelse())
            .type(ettersendelsesType)

        pleietrengende?.let { ettersendelse.pleietrengende(it.tilK9Pleietrengende()) }
        return ettersendelse.build()
    }

    override fun ytelse(): Ytelse = Ytelse.ETTERSENDING

    override fun søknadId(): String = søknadId

    override fun vedlegg(): List<URL> = vedlegg

    override fun ettersendelseValidator(): SøknadValidator<Ettersendelse> = EttersendelseValidator()
    fun leggTilIdentifikatorPåBarnHvisMangler(barnFraOppslag: List<BarnOppslag>) {
        if (pleietrengende != null && pleietrengende.manglerIdentitetsnummer()) {
            pleietrengende oppdaterFødselsnummer barnFraOppslag.hentIdentitetsnummerForBarn(pleietrengende.aktørId)
        }
    }
}
