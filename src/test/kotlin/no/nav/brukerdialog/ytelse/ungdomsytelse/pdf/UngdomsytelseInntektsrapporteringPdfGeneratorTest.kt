package no.nav.brukerdialog.ytelse.ungdomsytelse.pdf

import no.nav.brukerdialog.pdf.PDFGenerator
import no.nav.brukerdialog.utils.PathUtils.pdfPath
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.inntektsrapportering.OppgittInntektForPeriode
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.inntektsrapportering.UngPeriode
import no.nav.brukerdialog.ytelse.ungdomsytelse.utils.InntektrapporteringUtils
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDate

class UngdomsytelseInntektsrapporteringPdfGeneratorTest {

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
            var id = "1-full-inntekt-alle"
            var pdf = generator.genererPDF(InntektrapporteringUtils.gyldigInntektsrapportering(
                søknadId = id, oppgittInntektForPeriode = OppgittInntektForPeriode(
                    arbeidstakerOgFrilansInntekt = 6000,
                    næringsinntekt = 4000,
                    inntektFraYtelse = 2000,
                    periodeForInntekt = UngPeriode(
                        fraOgMed = LocalDate.parse("2025-01-01"),
                        tilOgMed = LocalDate.parse("2025-01-31")
                    )
                )
            ).pdfData())
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "2-arbeidstaker-og-frilansinntekt"
            pdf = generator.genererPDF(InntektrapporteringUtils.gyldigInntektsrapportering(
                søknadId = id, oppgittInntektForPeriode = OppgittInntektForPeriode(
                    arbeidstakerOgFrilansInntekt = 6000,
                    næringsinntekt = null,
                    inntektFraYtelse = null,
                    periodeForInntekt = UngPeriode(
                        fraOgMed = LocalDate.parse("2025-01-01"),
                        tilOgMed = LocalDate.parse("2025-01-31")
                    )
                )
            ).pdfData())
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "3-næringsinntekt"
            pdf = generator.genererPDF(InntektrapporteringUtils.gyldigInntektsrapportering(
                søknadId = id, oppgittInntektForPeriode = OppgittInntektForPeriode(
                    arbeidstakerOgFrilansInntekt = null,
                    næringsinntekt = 6000,
                    inntektFraYtelse = null,
                    periodeForInntekt = UngPeriode(
                        fraOgMed = LocalDate.parse("2025-01-01"),
                        tilOgMed = LocalDate.parse("2025-01-31")
                    )
                )
            ).pdfData())
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "4-ytelse"
            pdf = generator.genererPDF(InntektrapporteringUtils.gyldigInntektsrapportering(
                søknadId = id, oppgittInntektForPeriode = OppgittInntektForPeriode(
                    arbeidstakerOgFrilansInntekt = null,
                    næringsinntekt = null,
                    inntektFraYtelse = 6000,
                    periodeForInntekt = UngPeriode(
                        fraOgMed = LocalDate.parse("2025-01-01"),
                        tilOgMed = LocalDate.parse("2025-01-31")
                    )
                )
            ).pdfData())
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)
        }
    }
}
