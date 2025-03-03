package no.nav.brukerdialog.ytelse.ungdomsytelse.pdf

import no.nav.brukerdialog.pdf.PDFGenerator
import no.nav.brukerdialog.utils.PathUtils.pdfPath
import no.nav.brukerdialog.ytelse.ungdomsytelse.utils.UngdomsytelsesøknadUtils
import org.junit.jupiter.api.Test
import java.io.File

class UngdomsytelesøknadPdfGeneratorTest {
    @Test
    fun `generering av oppsummerings-PDF fungerer`() {
        genererOppsummeringsPdfer(false)
    }

    @Test
    fun `opprett lesbar oppsummerings-PDF`() {
        genererOppsummeringsPdfer(true)
    }

    private companion object {
        const val PDF_PREFIX = "ung"
        val generator = PDFGenerator()

        fun genererOppsummeringsPdfer(writeBytes: Boolean) {
            var id = "1-full-søknad"
            var pdf = generator.genererPDF(UngdomsytelsesøknadUtils.gyldigSøknad(søknadId = id).pdfData())
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)
        }
    }
}
