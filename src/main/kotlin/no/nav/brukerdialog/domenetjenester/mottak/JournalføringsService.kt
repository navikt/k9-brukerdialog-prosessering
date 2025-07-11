package no.nav.brukerdialog.domenetjenester.mottak

import no.nav.brukerdialog.integrasjon.dokarkiv.DokarkivService
import no.nav.brukerdialog.integrasjon.dokarkiv.Image2PDFConverter
import no.nav.brukerdialog.integrasjon.dokarkiv.dto.AvsenderMottakerIdType
import no.nav.brukerdialog.integrasjon.dokarkiv.dto.JournalPostRequestV1Factory
import no.nav.brukerdialog.integrasjon.dokarkiv.dto.YtelseType
import no.nav.brukerdialog.integrasjon.k9mellomlagring.ContentTypeService
import no.nav.brukerdialog.kafka.types.Journalfort
import no.nav.brukerdialog.kafka.types.JournalfortEttersendelse
import no.nav.brukerdialog.kafka.types.JournalfortOppgavebekreftelse
import no.nav.brukerdialog.kafka.types.JournalfortSøknad
import no.nav.brukerdialog.mellomlagring.dokument.Dokument
import no.nav.brukerdialog.mellomlagring.dokument.DokumentEier
import no.nav.brukerdialog.mellomlagring.dokument.DokumentService
import no.nav.brukerdialog.utils.MDCUtil
import no.nav.brukerdialog.ytelse.ettersendelse.kafka.domene.Søknadstype
import no.nav.brukerdialog.ytelse.fellesdomene.Navn
import no.nav.k9.oppgave.OppgaveBekreftelse
import no.nav.k9.søknad.Søknad
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import no.nav.k9.ettersendelse.Ettersendelse as K9Ettersendelse

@Service
class JournalføringsService(
    private val dokarkivService: DokarkivService,
    private val dokumentService: DokumentService,
    private val contentTypeService: ContentTypeService,
    private val image2PDFConverter: Image2PDFConverter,
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
            val dokumenter = dokumentService.hentDokumenter(
                dokumentIder = dokumentId,
                dokumentEier = DokumentEier(journalføringsRequest.norskIdent)
            )
            val pdfDokumenter = håndterOgKonverterTilPDF(dokumenter)
            alleDokumenter.add(pdfDokumenter)
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

    fun håndterOgKonverterTilPDF(dokumenter: List<Dokument>): List<Dokument> {
        logger.trace("Alle dokumenter hentet.")
        val bildeDokumenter = dokumenter.filter { contentTypeService.isSupportedImage(it.contentType) }
        logger.trace("${bildeDokumenter.size} bilder.")
        val applicationDokumenter = dokumenter.filter { contentTypeService.isSupportedApplication(it.contentType) }
        logger.trace("${applicationDokumenter.size} andre støttede dokumenter.")
        val ikkeSupporterteDokumenter = dokumenter.filter { !contentTypeService.isSupported(it.contentType) }
        if (ikkeSupporterteDokumenter.isNotEmpty()) {
            logger.warn("${ikkeSupporterteDokumenter.size} dokumenter som ikke støttes. Disse vil utelates fra journalføring.")
        }

        val supporterteDokumenter = applicationDokumenter.toMutableList()

        logger.trace("Gjør om de ${bildeDokumenter.size} bildene til PDF.")
        bildeDokumenter.forEach {
            supporterteDokumenter.add(
                Dokument(
                    title = it.title,
                    contentType = "application/pdf",
                    content = image2PDFConverter.convertToPDF(bytes = it.content, contentType = it.contentType)
                )
            )
        }

        logger.trace("Endringer fra bilde til PDF gjennomført.")

        return supporterteDokumenter
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

        is OppgaveBekreftelse -> JournalfortOppgavebekreftelse(
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
