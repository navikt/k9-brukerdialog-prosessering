package no.nav.brukerdialog.ytelse.ungdomsytelse.pdf

import no.nav.brukerdialog.pdf.PDFGenerator
import no.nav.brukerdialog.utils.PathUtils.pdfPath
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.BekreftelseSvar
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.KomplettEndretPeriodeUngdomsytelseOppgaveDTO
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
            var id = "1-godtar-endret-periode"
            var pdf = generator.genererPDF(
                UngdomsytelseOppgavebekreftelseUtils.oppgavebekreftelseMottatt().pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "2-avslår-endret-periode"
            pdf = generator.genererPDF(
                UngdomsytelseOppgavebekreftelseUtils.oppgavebekreftelseMottatt()
                    .copy(
                        oppgave = KomplettEndretPeriodeUngdomsytelseOppgaveDTO(
                            oppgaveReferanse = UUID.randomUUID().toString(),
                            uttalelse = UngdomsytelseOppgaveUttalelseDTO(
                                bekreftelseSvar = BekreftelseSvar.AVSLÅR,
                                meldingFraDeltaker = "Jeg ønsker en senere startdato"
                            ),
                            nyStartdato = LocalDate.parse("2025-01-01"),
                            //nySluttdato = LocalDate.parse("2025-12-01"),
                        )
                    ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "3-godtar-kontrollert-registerinntekt"
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
                                bekreftelseSvar = BekreftelseSvar.GODTAR,
                            )
                        )
                    ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "4-avslår-kontrollert-registerinntekt"
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
                                bekreftelseSvar = BekreftelseSvar.AVSLÅR,
                                meldingFraDeltaker = "Inntektsopplysningene i A-ordningen er feil. Jeg har sendt inn korrekt informasjon."
                            )
                        )
                    ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)
        }
    }
}
