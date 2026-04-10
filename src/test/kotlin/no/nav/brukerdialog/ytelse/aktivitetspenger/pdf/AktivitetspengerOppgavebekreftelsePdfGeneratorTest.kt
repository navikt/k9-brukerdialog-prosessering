package no.nav.brukerdialog.ytelse.aktivitetspenger.pdf

import no.nav.brukerdialog.pdf.PDFGenerator
import no.nav.brukerdialog.utils.PathUtils.pdfPath
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.oppgavebekreftelse.AktivitetspengerOppgaveUttalelseDTO
import no.nav.brukerdialog.ytelse.aktivitetspenger.utils.AktivitetspengerOppgavebekreftelseUtils
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.ArbeidOgFrilansRegisterInntektDTO
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.RegisterinntektDTO
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.YtelseRegisterInntektDTO
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.YtelseType
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDate
import java.util.UUID

class AktivitetspengerOppgavebekreftelsePdfGeneratorTest {

    @Test
    fun `generering av oppsummerings-PDF fungerer`() {
        genererOppsummeringsPdfer(false)
    }

    @Test
    fun `opprett lesbar oppsummerings-PDF`() {
        genererOppsummeringsPdfer(true)
    }

    private companion object {
        const val PDF_PREFIX = "aktivitetspenger-oppgave"
        val generator = PDFGenerator()

        fun genererOppsummeringsPdfer(writeBytes: Boolean) {
            var id = "1-godtar-kontrollert-registerinntekt"
            var pdf = generator.genererPDF(
                AktivitetspengerOppgavebekreftelseUtils.oppgavebekreftelseMottatt().pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "2-avslår-kontrollert-registerinntekt"
            pdf = generator.genererPDF(
                AktivitetspengerOppgavebekreftelseUtils.oppgavebekreftelseMottatt(
                    oppgave = AktivitetspengerOppgavebekreftelseUtils.defaultKomplettOppgave.copy(
                        oppgaveReferanse = UUID.randomUUID().toString(),
                        fraOgMed = LocalDate.parse("2025-06-01"),
                        tilOgMed = LocalDate.parse("2025-06-30"),
                        registerinntekt = RegisterinntektDTO(
                            listOf(
                                ArbeidOgFrilansRegisterInntektDTO(50000, "123456789", "Testarbeidsgiver AS"),
                                ArbeidOgFrilansRegisterInntektDTO(25000, "987654321", null),
                            ),
                            listOf(
                                YtelseRegisterInntektDTO(8000, YtelseType.SYKEPENGER),
                                YtelseRegisterInntektDTO(3000, YtelseType.DAGPENGER),
                            ),
                        ),
                        uttalelse = AktivitetspengerOppgaveUttalelseDTO(
                            harUttalelse = true,
                            uttalelseFraDeltaker = "Inntektsopplysningene er feil. Jeg har hatt lavere inntekt denne perioden.",
                        ),
                    )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)
        }
    }
}
