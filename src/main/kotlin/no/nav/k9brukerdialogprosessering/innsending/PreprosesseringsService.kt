package no.nav.k9brukerdialogprosessering.innsending

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogprosessering.mellomlagring.Dokument
import no.nav.k9brukerdialogprosessering.mellomlagring.DokumentEier
import no.nav.k9brukerdialogprosessering.mellomlagring.K9MellomlagringService
import no.nav.k9brukerdialogprosessering.mellomlagring.dokumentId
import no.nav.k9brukerdialogprosessering.pdf.PDFGenerator
import no.nav.k9brukerdialogprosessering.pdf.PdfData
import org.springframework.stereotype.Service

@Service
class PreprosesseringsService(
    private val pdfGenerator: PDFGenerator,
    private val mapper: ObjectMapper,
    private val k9MellomlagringService: K9MellomlagringService,
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
        val oppsummringPdf = pdfGenerator.genererPDF(preprosesseringsData.pdfData)

        logger.info("Mellomlagrer Oppsummerings-PDF...")
        val oppsummeringPdfDokumentId = k9MellomlagringService.lagreDokument(
            Dokument(
                eier = dokumentEier,
                content = oppsummringPdf,
                contentType = "application/pdf",
                title = preprosesseringsData.pdfJournalføringsTittel
            )
        ).dokumentId()

        logger.info("Mellomlagrer Oppsummerings-JSON")
        val soknadJsonDokumentId = k9MellomlagringService.lagreDokument(
            dokument = Dokument(
                eier = dokumentEier,
                content = mapper.writeValueAsBytes(k9FormatSøknad),
                contentType = "application/json",
                title = preprosesseringsData.jsonJournalføringsTittel
            )
        ).dokumentId()

        val komplettDokumentId = mutableListOf(
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
    val k9FormatSøknad: Søknad,
    val vedleggId: List<String>,
    val fødselsattestVedleggId: List<String>?,
    val pdfData: PdfData,
    val pdfJournalføringsTittel: String,
    val jsonJournalføringsTittel: String,
)

data class PreprosesseringsResultat(
    val dokumenter: List<List<String>>,
)
