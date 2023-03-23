package no.nav.k9brukerdialogprosessering.meldinger.ettersendelse

import no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.domene.Søknadstype
import no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.utils.EttersendingUtils
import no.nav.k9brukerdialogprosessering.pdf.PDFGenerator
import no.nav.k9brukerdialogprosessering.utils.PathUtils.pdfPath
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.io.File
import java.time.ZonedDateTime
import java.util.*

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

    private companion object {
        const val PDF_PREFIX = "ettersendelse"
        private val generator = PDFGenerator()
    }

    private fun genererOppsummeringsPdfer(søknadstype: Søknadstype, writeBytes: Boolean) {
        val id = søknadstype.name.lowercase()
        val pdf = generator.genererPDF(
            EttersendingUtils.defaultEttersendelse(id, ZonedDateTime.now()).copy(
                søknadstype = søknadstype,
                beskrivelse = null
            ).pdfData()
        )

        if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)
    }
}
