package no.nav.k9brukerdialogprosessering.meldinger.omsorgspengermidlertidigalene

import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengermidlertidigalene.utils.OMPMidlertidigAleneSoknadUtils
import no.nav.k9brukerdialogprosessering.pdf.PDFGenerator
import no.nav.k9brukerdialogprosessering.utils.PathUtils.pdfPath
import org.junit.jupiter.api.Test
import java.io.File
import java.time.ZonedDateTime

class OMPMidlertidigAleneSoknadPdfGeneratorTest {

    @Test
    fun `generering av oppsummerings-PDF fungerer`() {
        genererOppsummeringsPdfer(false)
    }

    @Test
    fun `opprett lesbar oppsummerings-PDF`() {
        genererOppsummeringsPdfer(true)
    }

    private companion object {
        const val PDF_PREFIX = "omp_ma"
        val generator = PDFGenerator()
    }

    fun genererOppsummeringsPdfer(writeBytes: Boolean) {
        var id = "1-full-søknad"
        var pdf = generator.genererPDF(
            OMPMidlertidigAleneSoknadUtils.defaultSøknad(
                søknadId = id,
                mottatt = ZonedDateTime.now(),
            ).pdfData()
        )
        if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)
    }
}

