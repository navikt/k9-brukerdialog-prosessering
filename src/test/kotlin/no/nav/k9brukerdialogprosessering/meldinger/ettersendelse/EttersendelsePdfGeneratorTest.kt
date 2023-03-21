package no.nav.k9brukerdialogprosessering.meldinger.ettersendelse

import no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.domene.Søknadstype
import no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.utils.EttersendingUtils
import no.nav.k9brukerdialogprosessering.pdf.PDFGenerator
import no.nav.k9brukerdialogprosessering.utils.PathUtils.pdfPath
import org.junit.jupiter.api.Test
import java.io.File
import java.time.ZonedDateTime

class EttersendelsePdfGeneratorTest {

    @Test
    fun `generering av oppsummerings-PDF fungerer`() {
        genererOppsummeringsPdfer(false)
    }

    @Test
    //@Ignore
    fun `opprett lesbar oppsummerings-PDF`() {
        genererOppsummeringsPdfer(true)
    }

    private companion object {
        const val PDF_PREFIX = "ettersendelse"
        private val generator = PDFGenerator()
    }

    private fun genererOppsummeringsPdfer(writeBytes: Boolean) {
        var id = "omsorgspenger"
        var pdf = generator.genererPDF(
            EttersendingUtils.defaultEttersendelse(id, ZonedDateTime.now()).copy(
                søknadstype = Søknadstype.OMP_UTV_KS,
                beskrivelse = null
            ).pdfData()
        )

        if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

        id = "pleiepenger"
        pdf = generator.genererPDF(
            EttersendingUtils.defaultEttersendelse(id, ZonedDateTime.now()).copy(
                søknadstype = Søknadstype.PLEIEPENGER_SYKT_BARN,
                beskrivelse = null
            ).pdfData()
        )
        if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

        id = "omsorgspenger-utbetaling-snf"
        pdf = generator.genererPDF(
            EttersendingUtils.defaultEttersendelse(id, ZonedDateTime.now()).copy(
                søknadstype = Søknadstype.OMP_UT_SNF,
                beskrivelse = null
            ).pdfData()
        )
        if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

        id = "omsorgspenger-utbetaling-arbeidstaker"
        pdf = generator.genererPDF(
            EttersendingUtils.defaultEttersendelse(id, ZonedDateTime.now()).copy(
                søknadstype = Søknadstype.OMP_UT_ARBEIDSTAKER,
                beskrivelse = null
            ).pdfData()
        )
        if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

        id = "omsorgspenger-midlertidig-alene"
        pdf = generator.genererPDF(
            EttersendingUtils.defaultEttersendelse(id, ZonedDateTime.now()).copy(
                søknadstype = Søknadstype.OMP_UTV_MA,
                beskrivelse = null
            ).pdfData()
        )
        if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

        id = "melding-dele-dager"
        pdf = generator.genererPDF(
            EttersendingUtils.defaultEttersendelse(id, ZonedDateTime.now()).copy(
                søknadstype = Søknadstype.OMP_DELE_DAGER,
                beskrivelse = null
            ).pdfData()
        )
        if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

        id = "pp-livets-sluttfase"
        pdf = generator.genererPDF(
            EttersendingUtils.defaultEttersendelse(id, ZonedDateTime.now()).copy(
                søknadstype = Søknadstype.PLEIEPENGER_LIVETS_SLUTTFASE,
                beskrivelse = null
            ).pdfData()
        )
        if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)
    }
}
