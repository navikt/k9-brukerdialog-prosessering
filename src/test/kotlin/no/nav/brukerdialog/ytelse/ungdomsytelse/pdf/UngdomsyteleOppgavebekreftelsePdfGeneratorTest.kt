package no.nav.brukerdialog.ytelse.ungdomsytelse.pdf

import no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser.ArbeidOgFrilansRegisterInntektDTO
import no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser.RegisterinntektDTO
import no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser.YtelseRegisterInntektDTO
import no.nav.brukerdialog.pdf.PDFGenerator
import no.nav.brukerdialog.utils.PathUtils.pdfPath
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.BekreftelseSvar
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.KomplettEndretSluttdatoUngdomsytelseOppgaveDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.KomplettEndretStartdatoUngdomsytelseOppgaveDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.KomplettKontrollerRegisterInntektOppgaveTypeDataDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.UngdomsytelseIkkeGodkjentResponse
import no.nav.brukerdialog.ytelse.ungdomsytelse.utils.UngdomsytelseOppgavebekreftelseUtils
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDate
import java.util.UUID

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
            var pdf = generator.genererPDF(
                UngdomsytelseOppgavebekreftelseUtils.oppgavebekreftelseMottatt().pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "2-avslår-endret-startdato"
            pdf = generator.genererPDF(
                UngdomsytelseOppgavebekreftelseUtils.oppgavebekreftelseMottatt()
                    .copy(
                        oppgave = KomplettEndretStartdatoUngdomsytelseOppgaveDTO(
                            oppgaveId = UUID.randomUUID().toString(),
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
                        )
                    ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "3-godtar-endret-sluttdato"
            pdf = generator.genererPDF(
                UngdomsytelseOppgavebekreftelseUtils.oppgavebekreftelseMottatt()
                    .copy(
                        oppgave = KomplettEndretSluttdatoUngdomsytelseOppgaveDTO(
                            oppgaveId = UUID.randomUUID().toString(),
                            veilederRef = "Pål Hønesen",
                            meldingFraVeileder = """Hei, jeg har endret sluttdatoen som vi avtalte i møtet. Fra: Pål Hønesen.
                    """.trimMargin(),
                            nySluttdato = LocalDate.parse("2025-06-01"),
                            bekreftelseSvar = BekreftelseSvar.GODTAR
                        )
                    ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "4-avslår-endret-sluttdato"
            pdf = generator.genererPDF(
                UngdomsytelseOppgavebekreftelseUtils.oppgavebekreftelseMottatt()
                    .copy(
                        oppgave = KomplettEndretSluttdatoUngdomsytelseOppgaveDTO(
                            oppgaveId = UUID.randomUUID().toString(),
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
                        )
                    ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "5-godtar-kontrollert-registerinntekt"
            pdf = generator.genererPDF(
                UngdomsytelseOppgavebekreftelseUtils.oppgavebekreftelseMottatt()
                    .copy(
                        oppgave = KomplettKontrollerRegisterInntektOppgaveTypeDataDTO(
                            oppgaveId = UUID.randomUUID().toString(),
                            veilederRef = "n/a",
                            meldingFraVeileder = null,
                            fomDato = LocalDate.parse("2025-06-01"),
                            tomDato = LocalDate.parse("2025-12-01"),
                            registerinntekt = RegisterinntektDTO(
                                arbeidOgFrilansInntekter = listOf(
                                    ArbeidOgFrilansRegisterInntektDTO(
                                        inntekt = 1000,
                                        arbeidsgiver = "Arbeidsgiver 1"
                                    ),
                                    ArbeidOgFrilansRegisterInntektDTO(
                                        inntekt = 2000,
                                        arbeidsgiver = "Arbeidsgiver 2"
                                    )
                                ),
                                ytelseInntekter = listOf(
                                    YtelseRegisterInntektDTO(
                                        inntekt = 3000,
                                        ytelsetype = "Ytelse 1"
                                    ),
                                    YtelseRegisterInntektDTO(
                                        inntekt = 4000,
                                        ytelsetype = "Ytelse 2"
                                    )
                                )
                            ),
                            bekreftelseSvar = BekreftelseSvar.GODTAR
                        )
                    ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "6-avslår-kontrollert-registerinntekt"
            pdf = generator.genererPDF(
                UngdomsytelseOppgavebekreftelseUtils.oppgavebekreftelseMottatt()
                    .copy(
                        oppgave = KomplettKontrollerRegisterInntektOppgaveTypeDataDTO(
                            oppgaveId = id,
                            veilederRef = "Pål Hønesen",
                            meldingFraVeileder = null,
                            fomDato = LocalDate.parse("2025-06-01"),
                            tomDato = LocalDate.parse("2025-12-01"),
                            registerinntekt = RegisterinntektDTO(
                                arbeidOgFrilansInntekter = listOf(
                                    ArbeidOgFrilansRegisterInntektDTO(
                                        inntekt = 1000,
                                        arbeidsgiver = "Arbeidsgiver 1"
                                    ),
                                    ArbeidOgFrilansRegisterInntektDTO(
                                        inntekt = 2000,
                                        arbeidsgiver = "Arbeidsgiver 2"
                                    )
                                ),
                                ytelseInntekter = listOf(
                                    YtelseRegisterInntektDTO(
                                        inntekt = 3000,
                                        ytelsetype = "Ytelse 1"
                                    ),
                                    YtelseRegisterInntektDTO(
                                        inntekt = 4000,
                                        ytelsetype = "Ytelse 2"
                                    )
                                )
                            ),
                            bekreftelseSvar = BekreftelseSvar.AVSLÅR,
                            ikkeGodkjentResponse = UngdomsytelseIkkeGodkjentResponse(
                                korrigertDato = LocalDate.parse("2025-12-01"),
                                meldingFraDeltaker = "Inntektsopplysningene i A-ordningen er feil. Jeg har sendt inn korrekt informasjon.",
                                kontaktVeilederSvar = false
                            )
                        )
                    ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)
        }
    }
}
