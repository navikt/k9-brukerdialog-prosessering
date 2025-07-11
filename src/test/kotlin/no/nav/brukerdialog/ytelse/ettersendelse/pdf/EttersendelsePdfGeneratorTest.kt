package no.nav.brukerdialog.ytelse.ettersendelse.pdf

import no.nav.brukerdialog.ytelse.ettersendelse.kafka.domene.Søknadstype
import no.nav.brukerdialog.ytelse.ettersendelse.utils.EttersendingUtils
import no.nav.brukerdialog.pdf.PDFGenerator
import no.nav.brukerdialog.utils.PathUtils.pdfPath
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.io.File
import java.time.ZonedDateTime

class EttersendelsePdfGeneratorTest {

    @ParameterizedTest
    @EnumSource(Søknadstype::class)
    fun `generering av oppsummerings-PDF fungerer`(søknadstype: Søknadstype) {
        genererOppsummeringsPdfer(søknadstype, false)
    }

    @ParameterizedTest
    @EnumSource(Søknadstype::class)
    fun `opprett lesbar oppsummerings-PDF`(søknadstype: Søknadstype) {
        genererOppsummeringsPdfer(søknadstype, true)
    }

    @Test
    fun `informasjon om barn inkluderes i pdf om det er pleietrengende`() {
        val html = generator.genererHTML(EttersendingUtils.defaultEttersendelse().copy().pdfData())
        Assertions.assertTrue(html.contains("Pleietrengende"))
    }

    @Test
    fun `informasjon om barn inkluderes ikke i pdf om det ikke er pleietrengende`() {
        val html = generator.genererHTML(
            EttersendingUtils.defaultEttersendelse().copy(
                pleietrengende = null
        ).pdfData())
        Assertions.assertFalse(html.contains("Pleietrengende"))
    }


    private companion object {
        const val PDF_PREFIX = "ettersendelse"
        private val generator = PDFGenerator()
    }

    private fun genererOppsummeringsPdfer(søknadstype: Søknadstype, writeBytes: Boolean) {
        val id = søknadstype.name.lowercase()
        val pdf = generator.genererPDF(
                EttersendingUtils.defaultEttersendelse(id, ZonedDateTime.now()).copy(
                        søknadstype = søknadstype
                ).pdfData()
        )

        if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)
    }
}
