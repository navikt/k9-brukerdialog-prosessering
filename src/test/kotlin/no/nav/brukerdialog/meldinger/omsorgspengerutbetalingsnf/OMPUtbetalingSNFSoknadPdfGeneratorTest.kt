package no.nav.brukerdialog.meldinger.omsorgspengerutbetalingsnf

import no.nav.brukerdialog.meldinger.omsorgspengerutbetalingsnf.utils.OMPUtbetalingSNFSøknadUtils
import no.nav.brukerdialog.pdf.PDFGenerator
import no.nav.brukerdialog.utils.PathUtils.pdfPath
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
        var id = "1-full-søknad"
        var pdf = generator.genererPDF(
            OMPUtbetalingSNFSøknadUtils.defaultSøknad(søknadId = id, mottatt = ZonedDateTime.now()).pdfData()
        )
        if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

        id = "2-søknad-om-barn-og-dager-du-må-dekke-selv-true"
        pdf = generator.genererPDF(
            OMPUtbetalingSNFSøknadUtils.defaultSøknad(søknadId = id, mottatt = ZonedDateTime.now()).copy(
                harSyktBarn = true,
                harAleneomsorg = true
            )
                .pdfData()
        )
        if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

        id = "3-søknad-om-barn-og-dager-du-må-dekke-selv-false"
        pdf = generator.genererPDF(
            OMPUtbetalingSNFSøknadUtils.defaultSøknad(søknadId = id, mottatt = ZonedDateTime.now()).copy(
                harDekketTiFørsteDagerSelv = false,
                harSyktBarn = false,
                harAleneomsorg = false
            )
                .pdfData()
        )
        if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)
    }
}

