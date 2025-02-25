package no.nav.brukerdialog.ytelse.ungdomsytelse.pdf

import no.nav.brukerdialog.ytelse.ungdomsytelse.utils.UngdomsytelsesøknadUtils
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.FlereSokereSvar
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.Frilans
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.Land
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.Næringstype
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.Pleietrengende
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.UtenlandskNæring
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.ÅrsakManglerIdentitetsnummer
import no.nav.brukerdialog.pdf.PDFGenerator
import no.nav.brukerdialog.utils.PathUtils.pdfPath
import no.nav.brukerdialog.ytelse.ungdomsytelse.utils.UngdomsytelseOppgavebekreftelseUtils
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDate

class UngdomsyteleOppgavebekreftelsePdfGeneratorTest {

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
            var id = "1-oppgavebekreftelse"
            var pdf = generator.genererPDF(UngdomsytelseOppgavebekreftelseUtils.oppgavebekreftelseMottatt(oppgaveId = id).pdfData())
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)
        }
    }
}
