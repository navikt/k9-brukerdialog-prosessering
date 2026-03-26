package no.nav.brukerdialog.ytelse.ungdomsytelse.pdf

import no.nav.brukerdialog.pdf.PDFGenerator
import no.nav.brukerdialog.utils.PathUtils.pdfPath
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.KomplettEndretPeriodeUngdomsytelseOppgaveDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.KomplettEndretSluttdatoUngdomsytelseOppgaveDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.KomplettEndretStartdatoUngdomsytelseOppgaveDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.KomplettKontrollerRegisterInntektOppgaveTypeDataDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.UngdomsytelseOppgaveUttalelseDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.utils.UngdomsytelseOppgavebekreftelseUtils
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.endretperiode.PeriodeDTO
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.ArbeidOgFrilansRegisterInntektDTO
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.RegisterinntektDTO
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.YtelseRegisterInntektDTO
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.YtelseType
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
                                listOf(
                                    ArbeidOgFrilansRegisterInntektDTO(
                                        1000,
                                        "123456789",
                                        "Arbeidsgiver 1"
                                    ),
                                    ArbeidOgFrilansRegisterInntektDTO(
                                        2000,
                                         "987654321",
                                        "Arbeidsgiver 2"
                                    )
                                ),
                                listOf(
                                    YtelseRegisterInntektDTO(
                                        3000,
                                        YtelseType.PLEIEPENGER
                                    ),
                                    YtelseRegisterInntektDTO(
                                        4000,
                                        YtelseType.OMSORGSPENGER
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
                                listOf(
                                    ArbeidOgFrilansRegisterInntektDTO(
                                        1000,
                                        "123456789",
                                        "Arbeidsgiver 1"
                                    ),
                                    ArbeidOgFrilansRegisterInntektDTO(
                                        2000,
                                        "987654321",
                                        "Arbeidsgiver 2"
                                    )
                                ),
                                listOf(
                                    YtelseRegisterInntektDTO(
                                        3000,
                                        YtelseType.PLEIEPENGER
                                    ),
                                    YtelseRegisterInntektDTO(
                                        4000,
                                        YtelseType.OMSORGSPENGER
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

            id = "7-godtar-endret-periode-uten-periode"
            pdf = generator.genererPDF(
                UngdomsytelseOppgavebekreftelseUtils.oppgavebekreftelseMottatt()
                    .copy(
                        oppgave = KomplettEndretPeriodeUngdomsytelseOppgaveDTO(
                            oppgaveReferanse = UUID.randomUUID().toString(),
                            uttalelse = UngdomsytelseOppgaveUttalelseDTO(
                                harUttalelse = false,
                            ),
                            nyPeriode = null
                        )
                    ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "8-avslår-endret-periode-uten-periode"
            pdf = generator.genererPDF(
                UngdomsytelseOppgavebekreftelseUtils.oppgavebekreftelseMottatt()
                    .copy(
                        oppgave = KomplettEndretPeriodeUngdomsytelseOppgaveDTO(
                            oppgaveReferanse = UUID.randomUUID().toString(),
                            uttalelse = UngdomsytelseOppgaveUttalelseDTO(
                                harUttalelse = true,
                                uttalelseFraDeltaker = "Jeg ønsker en annen periode"
                            ),
                            nyPeriode = null
                        )
                    ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "9-godtar-endret-periode-med-periode-start-og-slutt"
            pdf = generator.genererPDF(
                UngdomsytelseOppgavebekreftelseUtils.oppgavebekreftelseMottatt()
                    .copy(
                        oppgave = KomplettEndretPeriodeUngdomsytelseOppgaveDTO(
                            oppgaveReferanse = UUID.randomUUID().toString(),
                            uttalelse = UngdomsytelseOppgaveUttalelseDTO(
                                harUttalelse = false,
                            ),
                            nyPeriode = PeriodeDTO(
                                LocalDate.parse("2025-01-01"),
                                LocalDate.parse("2025-12-31")
                            )
                        )
                    ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "10-avslår-endret-periode-med-periode-start-og-slutt"
            pdf = generator.genererPDF(
                UngdomsytelseOppgavebekreftelseUtils.oppgavebekreftelseMottatt()
                    .copy(
                        oppgave = KomplettEndretPeriodeUngdomsytelseOppgaveDTO(
                            oppgaveReferanse = UUID.randomUUID().toString(),
                            uttalelse = UngdomsytelseOppgaveUttalelseDTO(
                                harUttalelse = true,
                                uttalelseFraDeltaker = "Jeg ønsker en annen periode"
                            ),
                            nyPeriode = PeriodeDTO(
                                LocalDate.parse("2025-01-01"),
                                LocalDate.parse("2025-12-31")
                            )
                        )
                    ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "11-godtar-endret-periode-med-periode-kun-start"
            pdf = generator.genererPDF(
                UngdomsytelseOppgavebekreftelseUtils.oppgavebekreftelseMottatt()
                    .copy(
                        oppgave = KomplettEndretPeriodeUngdomsytelseOppgaveDTO(
                            oppgaveReferanse = UUID.randomUUID().toString(),
                            uttalelse = UngdomsytelseOppgaveUttalelseDTO(
                                harUttalelse = false,
                            ),
                            nyPeriode = PeriodeDTO(
                                LocalDate.parse("2025-01-01"),
                                null
                            )
                        )
                    ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "12-avslår-endret-periode-med-periode-kun-start"
            pdf = generator.genererPDF(
                UngdomsytelseOppgavebekreftelseUtils.oppgavebekreftelseMottatt()
                    .copy(
                        oppgave = KomplettEndretPeriodeUngdomsytelseOppgaveDTO(
                            oppgaveReferanse = UUID.randomUUID().toString(),
                            uttalelse = UngdomsytelseOppgaveUttalelseDTO(
                                harUttalelse = true,
                                uttalelseFraDeltaker = "Jeg ønsker en annen periode"
                            ),
                            nyPeriode = PeriodeDTO(
                                LocalDate.parse("2025-01-01"),
                                null
                            )
                        )
                    ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

        }
    }
}
