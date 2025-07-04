package no.nav.brukerdialog.domenetjenester.mottak

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.brukerdialog.mellomlagring.dokument.Dokument
import no.nav.brukerdialog.mellomlagring.dokument.DokumentEier
import no.nav.brukerdialog.mellomlagring.dokument.DokumentService
import no.nav.brukerdialog.pdf.PdfData
import no.nav.brukerdialog.pdf.PdfService
import no.nav.k9.søknad.Innsending
import org.springframework.stereotype.Service

@Service
class PreprosesseringsService(
    private val pdfService: PdfService,
    private val mapper: ObjectMapper,
    private val dokumentService: DokumentService,
) {
    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(PreprosesseringsService::class.java)
    }

    suspend fun preprosesser(preprosesseringsData: PreprosesseringsData): PreprosesseringsResultat {
        val k9FormatSøknad = preprosesseringsData.k9FormatSøknad
        logger.info("Preprosesserer med søknadId: {}", k9FormatSøknad.søknadId)

        val søkerFødselsnummer = preprosesseringsData.søkerFødselsnummer
        val dokumentEier = DokumentEier(søkerFødselsnummer)

        logger.info("Genererer Oppsummerings-PDF...")
        val oppsummeringPdf = pdfService.genererPDF(preprosesseringsData.pdfData)

        logger.info("Mellomlagrer Oppsummerings-PDF...")
        val oppsummeringPdfDokumentId = dokumentService.lagreDokument(
            dokument = Dokument(
                eier = dokumentEier,
                content = oppsummeringPdf,
                contentType = "application/pdf",
                title = preprosesseringsData.pdfJournalføringsTittel
            ),
            dokumentEier = DokumentEier(søkerFødselsnummer),
            medHold = true,
            skannForVirus = false
        )

        logger.info("Mellomlagrer Oppsummerings-JSON")
        val soknadJsonDokumentId = dokumentService.lagreDokument(
            dokument = Dokument(
                eier = dokumentEier,
                content = mapper.writeValueAsBytes(k9FormatSøknad),
                contentType = "application/json",
                title = preprosesseringsData.jsonJournalføringsTittel
            ),
            dokumentEier = DokumentEier(søkerFødselsnummer),
            medHold = true,
            skannForVirus = false
        )

        val komplettDokumentId: MutableList<List<String>> = mutableListOf(
            listOf(
                oppsummeringPdfDokumentId,
                soknadJsonDokumentId
            )
        )

        val vedleggId = preprosesseringsData.vedleggId
        if (vedleggId.isNotEmpty()) {
            logger.info("Legger til ${vedleggId.size} vedlegg Id's fra meldingen som dokument.")
            vedleggId.forEach { komplettDokumentId.add(listOf(it)) }
        }
        val fødselsattestVedleggId = preprosesseringsData.fødselsattestVedleggId
        if (!fødselsattestVedleggId.isNullOrEmpty()) {
            logger.info("Legger til ${fødselsattestVedleggId.size} fødselsattest fra søknad som dokument.")
            fødselsattestVedleggId.forEach { komplettDokumentId.add(listOf(it)) }
        }

        logger.info("Totalt ${komplettDokumentId.size} dokumentbolker med totalt ${komplettDokumentId.flatten().size} dokumenter")

        return PreprosesseringsResultat(
            dokumenter = komplettDokumentId.toList()
        )
    }
}

data class PreprosesseringsData(
    val søkerFødselsnummer: String,
    val k9FormatSøknad: Innsending,
    val vedleggId: List<String>,
    val fødselsattestVedleggId: List<String>?,
    val pdfData: PdfData,
    val pdfJournalføringsTittel: String,
    val jsonJournalføringsTittel: String,
)

data class PreprosesseringsResultat(
    val dokumenter: List<List<String>>,
)
