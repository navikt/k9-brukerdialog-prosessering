package no.nav.brukerdialog.domenetjenester.mottak

import no.nav.brukerdialog.integrasjon.dokarkiv.DokarkivService
import no.nav.brukerdialog.integrasjon.dokarkiv.dto.AvsenderMottakerIdType
import no.nav.brukerdialog.integrasjon.dokarkiv.dto.JournalPostRequestV1Factory
import no.nav.brukerdialog.integrasjon.dokarkiv.dto.YtelseType
import no.nav.brukerdialog.integrasjon.k9mellomlagring.K9DokumentMellomlagringService
import no.nav.brukerdialog.kafka.types.Journalfort
import no.nav.brukerdialog.kafka.types.JournalfortEttersendelse
import no.nav.brukerdialog.kafka.types.JournalfortSøknad
import no.nav.brukerdialog.ytelse.ettersendelse.kafka.domene.Søknadstype
import no.nav.brukerdialog.mellomlagring.dokument.Dokument
import no.nav.brukerdialog.mellomlagring.dokument.DokumentEier
import no.nav.brukerdialog.utils.MDCUtil
import no.nav.brukerdialog.ytelse.fellesdomene.Navn
import no.nav.k9.søknad.Søknad
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import no.nav.k9.ettersendelse.Ettersendelse as K9Ettersendelse

@Service
class JournalføringsService(
    private val dokarkivService: DokarkivService,
    private val k9DokumentMellomlagringService: K9DokumentMellomlagringService,
) {
    private companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(JournalføringsService::class.java)
    }

    suspend fun journalfør(preprosessertSøknad: Preprosessert): Journalfort {
        logger.info("Journalfører dokumenter: ${preprosessertSøknad.dokumenter().size}")
        val journalføringsRequest = preprosessertSøknad.tilJournaførigsRequest()

        val alleDokumenter = mutableListOf<List<Dokument>>()
        journalføringsRequest.dokumentId.forEach { dokumentId: List<String> ->
            logger.info("Henter dokumenter basert på dokumentId")
            alleDokumenter.add(
                k9DokumentMellomlagringService.hentDokumenterMedString(
                    dokumentIder = dokumentId,
                    dokumentEier = DokumentEier(journalføringsRequest.norskIdent)
                )
            )
        }

        val correlationId = journalføringsRequest.correlationId ?: MDCUtil.callIdOrNew()

        val journalPostRequest = JournalPostRequestV1Factory.instance(
            mottaker = journalføringsRequest.norskIdent,
            dokumenter = alleDokumenter.toList(),
            datoMottatt = journalføringsRequest.mottatt,
            ytelseType = journalføringsRequest.ytelseType,
            avsenderMottakerIdType = AvsenderMottakerIdType.FNR,
            avsenderMottakerNavn = journalføringsRequest.sokerNavn.sammensattNavn(),
            eksternReferanseId = correlationId
        )

        val dokarkivJournalpostResponse = dokarkivService.journalfør(journalPostRequest)
        return resolve(preprosessertSøknad, JournalføringsResponse(dokarkivJournalpostResponse.journalpostId))
    }

    private fun resolve(
        preprosessertSøknad: Preprosessert,
        journalføringsResponse: JournalføringsResponse,
    ) = when (val innsending = preprosessertSøknad.k9FormatSøknad()) {
        is Søknad -> JournalfortSøknad(journalpostId = journalføringsResponse.journalPostId, søknad = innsending)
        is K9Ettersendelse -> JournalfortEttersendelse(
            journalpostId = journalføringsResponse.journalPostId,
            søknad = innsending
        )

        else -> {
            logger.error("Ukjent søknadstype: $innsending")
            throw IllegalStateException("Ukjent søknadstype: $innsending")
        }
    }

    private fun Navn.sammensattNavn() = when (mellomnavn) {
        null -> "$fornavn $etternavn"
        else -> "$fornavn $mellomnavn $etternavn"
    }

    data class JournalføringsRequest(
        val ytelseType: YtelseType,
        val correlationId: String? = null,
        val søknadstype: Søknadstype? = null,
        val norskIdent: String,
        val sokerNavn: Navn,
        val mottatt: ZonedDateTime,
        val dokumentId: List<List<String>>,
    )

    data class JournalføringsResponse(
        val journalPostId: String,
    )
}
