package no.nav.brukerdialog.ytelse.ungdomsytelse.pdf

import no.nav.brukerdialog.pdf.PDFGenerator
import no.nav.brukerdialog.utils.PathUtils.pdfPath
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.domene.UngdomsytelsesøknadMottatt
import no.nav.brukerdialog.ytelse.ungdomsytelse.utils.UngdomsytelsesøknadUtils
import no.nav.k9.søknad.Søknad
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDate
import java.time.ZonedDateTime

class UngdomsytelesøknadPdfGeneratorTest {
    @Test
    fun `generering av oppsummerings-PDF fungerer`() {
        genererOppsummeringsPdfer(false)
    }

    @Test
    fun `opprett lesbar oppsummerings-PDF`() {
        genererOppsummeringsPdfer(true)
    }

    @Test
    fun `Dette er en test`() {
        val id = "1-full-søknad"
        val søker =
            Søker(
                aktørId = "1234",
                fødselsdato = LocalDate.now().minusDays(10000),
                fødselsnummer = "12345678912",
                fornavn = "Ola",
                mellomnavn = "Nordmann",
                etternavn = "Nordmannsen",
            )
        val ungdomsytelsesøknadMottatt =
            UngdomsytelsesøknadPdfData(
                UngdomsytelsesøknadMottatt(
                    søknadId = id,
                    mottatt = ZonedDateTime.now(),
                    språk = "nb",
                    søker = søker,
                    fraOgMed = LocalDate.now().minusDays(30),
                    tilOgMed = LocalDate.now(),
                    inntekt = 100000.0,
                    k9Format = Søknad(),
                    harForståttRettigheterOgPlikter = true,
                    harBekreftetOpplysninger = true,
                ),
            )
        println("Dette er: " + ungdomsytelsesøknadMottatt.nyPdfData())
    }

    private companion object {
        const val PDF_PREFIX = "ung"
        val generator = PDFGenerator()

        fun genererOppsummeringsPdfer(writeBytes: Boolean) {
            var id = "1-full-søknad"
            var pdf = generator.genererPDF(UngdomsytelsesøknadUtils.gyldigSøknad(søknadId = id).pdfData())
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)
        }
    }
}
