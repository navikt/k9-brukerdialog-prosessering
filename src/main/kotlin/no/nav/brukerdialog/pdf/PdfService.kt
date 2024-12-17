package no.nav.brukerdialog.pdf

import org.springframework.stereotype.Service

@Service
class PdfService(private val pdfGenerator: PDFGenerator,) {

    fun genererPDF(pdfData: PdfData, brukFellesPDFLøsning: Boolean = false): ByteArray {
        if (brukFellesPDFLøsning) {
            // TODO: Bruk felles PDF-løsning
            throw UnsupportedOperationException("Felles PDF-løsning er ikke implementert")
        }

        return pdfGenerator.genererPDF(pdfData)
    }
}
