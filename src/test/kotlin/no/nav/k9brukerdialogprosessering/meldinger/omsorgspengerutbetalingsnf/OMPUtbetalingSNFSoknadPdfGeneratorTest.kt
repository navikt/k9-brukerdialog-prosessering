package no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerutbetalingsnf

import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerutbetalingsnf.utils.OMPUtbetalingSNFSøknadUtils
import no.nav.k9brukerdialogprosessering.pdf.PDFGenerator
import no.nav.k9brukerdialogprosessering.utils.PathUtils.pdfPath
import org.junit.jupiter.api.Test
import java.io.File
import java.time.ZonedDateTime

class OMPUtbetalingSNFSoknadPdfGeneratorTest {

    @Test
    fun `generering av oppsummerings-PDF fungerer`() {
        genererOppsummeringsPdfer(false)
    }

    @Test
    fun `opprett lesbar oppsummerings-PDF`() {
        genererOppsummeringsPdfer(true)
    }

    private companion object {
        const val PDF_PREFIX = "omp_utb_snf"
        val generator = PDFGenerator()
    }

    fun genererOppsummeringsPdfer(writeBytes: Boolean) {
        var id = "1-full-søknad-selvstendig-næringsdrivende-frilanser"
        var pdf = generator.genererPDF(
            OMPUtbetalingSNFSøknadUtils.defaultSøknad(søknadId = id, mottatt = ZonedDateTime.now()).pdfData()
        )
        if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)
    }
}

