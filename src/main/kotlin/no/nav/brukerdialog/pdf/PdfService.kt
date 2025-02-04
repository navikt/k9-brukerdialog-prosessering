package no.nav.brukerdialog.pdf

import no.nav.brukerdialog.integrasjon.familiepdf.FamiliePdfService
import no.nav.brukerdialog.integrasjon.familiepdf.dto.FamiliePdfPostRequest
import no.nav.brukerdialog.integrasjon.familiepdf.dto.PdfConfig
import no.nav.brukerdialog.integrasjon.familiepdf.dto.VerdilisteElement
import org.springframework.stereotype.Service

@Service
class PdfService(private val pdfGenerator: PDFGenerator,private val familiePdfService: FamiliePdfService) {
    suspend fun genererPDF(pdfData: PdfData, brukFellesPDFLøsning: Boolean = false): ByteArray {
        if (brukFellesPDFLøsning) {
            val request = FamiliePdfPostRequest(
                label = pdfData.ytelse().toString(),
                verdiliste = listOf(VerdilisteElement(label = "label", verdi = "verdi")),
                pdfConfig = PdfConfig(true, pdfData.språk()),
            )
            val response = familiePdfService.lagPdfKvittering(request)
            return response
        }

        return pdfGenerator.genererPDF(pdfData)
    }
}
