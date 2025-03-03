package no.nav.brukerdialog.pdf

import no.nav.brukerdialog.common.PdfConfig
import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.integrasjon.familiepdf.FamiliePdfService
import no.nav.brukerdialog.integrasjon.familiepdf.dto.FamiliePdfPostRequest
import org.springframework.stereotype.Service

@Service
class PdfService(
    private val pdfGenerator: PDFGenerator,
    private val familiePdfService: FamiliePdfService,
) {
    suspend fun genererPDF(
        pdfData: PdfData,
        brukFellesPDFLøsning: Boolean = false,
    ): ByteArray {
        if (brukFellesPDFLøsning) {
            if (pdfData.ytelse() == Ytelse.PLEIEPENGER_SYKT_BARN) {
                val request =
                    FamiliePdfPostRequest(
                        label = pdfData.ytelse().toString(),
                        verdiliste = pdfData.nyPdfData(),
                        pdfConfig = PdfConfig(true, pdfData.språk().name),
                    )
                val response = familiePdfService.lagPdfKvittering(request)
                return response
            } else {
                val request =
                    FamiliePdfPostRequest(
                        label = pdfData.ytelse().toString(),
                        verdiliste = listOf(VerdilisteElement(label = "label", verdi = "verdi")),
                        pdfConfig = PdfConfig(true, pdfData.språk().name),
                    )
                val response = familiePdfService.lagPdfKvittering(request)
                return response
            }
        }

        return pdfGenerator.genererPDF(pdfData)
    }
}
