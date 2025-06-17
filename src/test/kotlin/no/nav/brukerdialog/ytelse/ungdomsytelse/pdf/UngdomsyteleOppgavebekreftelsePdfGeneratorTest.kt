package no.nav.brukerdialog.ytelse.ungdomsytelse.pdf

import no.nav.brukerdialog.pdf.PDFGenerator
import no.nav.brukerdialog.utils.PathUtils.pdfPath
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.KomplettEndretSluttdatoUngdomsytelseOppgaveDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.KomplettEndretStartdatoUngdomsytelseOppgaveDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.KomplettKontrollerRegisterInntektOppgaveTypeDataDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.UngdomsytelseOppgaveUttalelseDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.utils.UngdomsytelseOppgavebekreftelseUtils
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.ArbeidOgFrilansRegisterInntektDTO
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.RegisterinntektDTO
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.YtelseRegisterInntektDTO
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.registerinntekt.YtelseType
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDate
import java.util.*

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
                            oppgaveReferanse = UUID.randomUUID().toString(),
                            uttalelse = UngdomsytelseOppgaveUttalelseDTO(
                                harUttalelse = true,
                                uttalelseFraDeltaker = "Jeg ønsker en senere startdato"
                            ),
                            nyStartdato = LocalDate.parse("2025-01-01"),
                        )
                    ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "3-godtar-endret-sluttdato"
            pdf = generator.genererPDF(
                UngdomsytelseOppgavebekreftelseUtils.oppgavebekreftelseMottatt()
                    .copy(
                        oppgave = KomplettEndretSluttdatoUngdomsytelseOppgaveDTO(
                            oppgaveReferanse = UUID.randomUUID().toString(),
                            uttalelse = UngdomsytelseOppgaveUttalelseDTO(
                                harUttalelse = false,
                            ),
                            nySluttdato = LocalDate.parse("2025-01-01"),
                        )
                    ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "4-avslår-endret-sluttdato"
            pdf = generator.genererPDF(
                UngdomsytelseOppgavebekreftelseUtils.oppgavebekreftelseMottatt()
                    .copy(
                        oppgave = KomplettEndretSluttdatoUngdomsytelseOppgaveDTO(
                            oppgaveReferanse = UUID.randomUUID().toString(),
                            uttalelse = UngdomsytelseOppgaveUttalelseDTO(
                                harUttalelse = true,
                                uttalelseFraDeltaker = "Jeg ønsker en senere sluttdato"
                            ),
                            nySluttdato = LocalDate.parse("2025-01-01"),
                        )
                    ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "5-godtar-kontrollert-registerinntekt"
            pdf = generator.genererPDF(
                UngdomsytelseOppgavebekreftelseUtils.oppgavebekreftelseMottatt()
                    .copy(
                        oppgave = KomplettKontrollerRegisterInntektOppgaveTypeDataDTO(
                            oppgaveReferanse = UUID.randomUUID().toString(),
                            fraOgMed = LocalDate.parse("2025-06-01"),
                            tilOgMed = LocalDate.parse("2025-12-01"),
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
                                        ytelsetype = YtelseType.PLEIEPENGER_SYKT_BARN
                                    ),
                                    YtelseRegisterInntektDTO(
                                        inntekt = 4000,
                                        ytelsetype = YtelseType.OMSORGSPENGER
                                    )
                                )
                            ),
                            uttalelse = UngdomsytelseOppgaveUttalelseDTO(
                                harUttalelse = false,
                            )
                        )
                    ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "6-avslår-kontrollert-registerinntekt"
            pdf = generator.genererPDF(
                UngdomsytelseOppgavebekreftelseUtils.oppgavebekreftelseMottatt()
                    .copy(
                        oppgave = KomplettKontrollerRegisterInntektOppgaveTypeDataDTO(
                            oppgaveReferanse = id,
                            fraOgMed = LocalDate.parse("2025-06-01"),
                            tilOgMed = LocalDate.parse("2025-12-01"),
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
                                        ytelsetype = YtelseType.PLEIEPENGER_SYKT_BARN
                                    ),
                                    YtelseRegisterInntektDTO(
                                        inntekt = 4000,
                                        ytelsetype = YtelseType.OMSORGSPENGER
                                    )
                                )
                            ),
                            uttalelse = UngdomsytelseOppgaveUttalelseDTO(
                                harUttalelse = true,
                                uttalelseFraDeltaker = "Inntektsopplysningene i A-ordningen er feil. Jeg har sendt inn korrekt informasjon."
                            )
                        )
                    ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)
        }
    }
}
