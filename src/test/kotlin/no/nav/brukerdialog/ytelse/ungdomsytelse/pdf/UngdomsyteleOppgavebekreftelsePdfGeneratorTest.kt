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
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.BekreftelseSvar
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.EndretSluttdatoUngdomsytelseOppgaveDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.EndretStartdatoUngdomsytelseOppgaveDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.UngdomsytelseIkkeGodkjentResponse
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
        const val PDF_PREFIX = "ung-oppgave"
        val generator = PDFGenerator()

        fun genererOppsummeringsPdfer(writeBytes: Boolean) {
            var id = "1-godtar-endret-startdato"
            var pdf = generator.genererPDF(UngdomsytelseOppgavebekreftelseUtils.oppgavebekreftelseMottatt(oppgaveId = id).pdfData())
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "2-avslår-endret-startdato"
            pdf = generator.genererPDF(UngdomsytelseOppgavebekreftelseUtils.oppgavebekreftelseMottatt(oppgaveId = id)
                .copy(oppgave = EndretStartdatoUngdomsytelseOppgaveDTO(
                    oppgaveId = id,
                    veilederRef = "Pål Hønesen",
                    meldingFraVeileder = """Hei, jeg har endret startdatoen som vi avtalte i møtet. Fra: Pål Hønesen.
                    """.trimMargin(),
                    nyStartdato = LocalDate.parse("2025-01-01"),
                    bekreftelseSvar = BekreftelseSvar.AVSLÅR,
                    ikkeGodkjentResponse = UngdomsytelseIkkeGodkjentResponse(
                        korrigertDato = LocalDate.parse("2025-02-01"),
                        meldingFraDeltaker = "Jeg ønsker en senere startdato",
                        kontaktVeilederSvar = false
                    )
                )).pdfData())
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "3-godtar-endret-sluttdato"
            pdf = generator.genererPDF(UngdomsytelseOppgavebekreftelseUtils.oppgavebekreftelseMottatt(oppgaveId = id)
                .copy(oppgave = EndretSluttdatoUngdomsytelseOppgaveDTO(
                    oppgaveId = id,
                    veilederRef = "Pål Hønesen",
                    meldingFraVeileder = """Hei, jeg har endret sluttdatoen som vi avtalte i møtet. Fra: Pål Hønesen.
                    """.trimMargin(),
                    nySluttdato = LocalDate.parse("2025-06-01"),
                    bekreftelseSvar = BekreftelseSvar.GODTAR
                )).pdfData())
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "4-avslår-endret-sluttdato"
            pdf = generator.genererPDF(UngdomsytelseOppgavebekreftelseUtils.oppgavebekreftelseMottatt(oppgaveId = id)
                .copy(oppgave = EndretSluttdatoUngdomsytelseOppgaveDTO(
                    oppgaveId = id,
                    veilederRef = "Pål Hønesen",
                    meldingFraVeileder = """Hei, jeg har endret sluttdatoen som vi avtalte i møtet. Fra: Pål Hønesen.
                    """.trimMargin(),
                    nySluttdato = LocalDate.parse("2025-06-01"),
                    bekreftelseSvar = BekreftelseSvar.AVSLÅR,
                    ikkeGodkjentResponse = UngdomsytelseIkkeGodkjentResponse(
                        korrigertDato = LocalDate.parse("2025-12-01"),
                        meldingFraDeltaker = "Jeg ønsker en senere sluttdato",
                        kontaktVeilederSvar = false
                    )
                )).pdfData())
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)
        }
    }
}
