package no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerutbetalingat

import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerutbetalingat.utils.OMPUtbetalingATSøknadUtils
import no.nav.k9brukerdialogprosessering.pdf.PDFGenerator
import no.nav.k9brukerdialogprosessering.utils.PathUtils.pdfPath
import org.junit.jupiter.api.Test
import java.io.File
import java.time.ZonedDateTime

class OMPUtbetalingATSoknadPdfGeneratorTest {

    @Test
    fun `generering av oppsummerings-PDF fungerer`() {
        genererOppsummeringsPdfer(false)
    }

    @Test
    fun `opprett lesbar oppsummerings-PDF`() {
        genererOppsummeringsPdfer(true)
    }

    private companion object {
        const val PDF_PREFIX = "omp_utb_at"
        val generator = PDFGenerator()
    }

    fun genererOppsummeringsPdfer(writeBytes: Boolean) {
        var id = "1-full-søknad-arbeidstaker"
        var pdf = generator.genererPDF(
            OMPUtbetalingATSøknadUtils.defaultSøknad(søknadId = id, mottatt = ZonedDateTime.now()).pdfData()
        )
        if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)
    }
}

