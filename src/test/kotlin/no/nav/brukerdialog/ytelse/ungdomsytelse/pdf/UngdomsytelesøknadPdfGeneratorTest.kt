package no.nav.brukerdialog.ytelse.ungdomsytelse.pdf

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.brukerdialog.common.PdfConfig
import no.nav.brukerdialog.integrasjon.familiepdf.dto.FamiliePdfPostRequest
import no.nav.brukerdialog.pdf.PDFGenerator
import no.nav.brukerdialog.utils.PathUtils.pdfPath
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.soknad.domene.UngdomsytelsesøknadMottatt
import no.nav.brukerdialog.ytelse.ungdomsytelse.utils.UngdomsytelsesøknadUtils
import no.nav.brukerdialog.ytelse.ungdomsytelse.utils.UngdomsytelsesøknadUtils.gyldigK9Format
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
    fun `ungdomsytelse test2`() {
        val id = "1-full-søknad"
        val søker = Søker(
            aktørId = "123456",
            fødselsnummer = "12345678912",
            fødselsdato = LocalDate.parse("2000-01-01"),
            etternavn = "Nordmann",
            mellomnavn = "Mellomnavn",
            fornavn = "Ola"
        )
        val søknadId: String = "test-soknad-id"
        val mottatt: ZonedDateTime = ZonedDateTime.now()
        val ungdomsytelsesøknadMottatt = UngdomsytelsesøknadPdfData( UngdomsytelsesøknadMottatt(
            språk = "nb",
            søknadId = søknadId,
            mottatt = mottatt,
            søker = Søker(
                aktørId = "123456",
                fødselsnummer = "12345678912",
                fødselsdato = LocalDate.parse("2000-01-01"),
                etternavn = "Nordmann",
                mellomnavn = "Mellomnavn",
                fornavn = "Ola"
            ),
            startdato = LocalDate.parse("2022-01-01"),
            k9Format = gyldigK9Format(søknadId, mottatt, LocalDate.parse("2022-01-01")),
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true
            )
        )

        val objectMapper = jacksonObjectMapper()
        val jsonString = objectMapper.writeValueAsString(
            FamiliePdfPostRequest(label = "Søknad om ungdomsytelse", ungdomsytelsesøknadMottatt.nyPdfData(), PdfConfig(true, "nb"))
        )
        println("Dette er: $jsonString")

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
