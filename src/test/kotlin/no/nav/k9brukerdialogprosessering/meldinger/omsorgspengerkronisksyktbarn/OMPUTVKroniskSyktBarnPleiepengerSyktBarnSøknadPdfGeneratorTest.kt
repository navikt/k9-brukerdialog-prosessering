package no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn

import no.nav.k9brukerdialogprosessering.meldinger.felles.domene.Søker
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn.domene.Barn
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn.domene.BarnSammeAdresse
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn.domene.OMPUTVKroniskSyktBarnSøknadMottatt
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn.domene.SøkerBarnRelasjon
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn.utils.SøknadUtils
import no.nav.k9brukerdialogprosessering.pdf.PDFGenerator
import no.nav.k9brukerdialogprosessering.utils.PathUtils.pdfPath
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDate
import java.time.ZonedDateTime

class OMPUTVKroniskSyktBarnPleiepengerSyktBarnSøknadPdfGeneratorTest {

    @Test
    fun `generering av oppsummerings-PDF fungerer`() {
        genererOppsummeringsPdfer(false)
    }

    @Test
    //@Ignore
    fun `opprett lesbar oppsummerings-PDF`() {
        genererOppsummeringsPdfer(true)
    }

    private companion object {
        const val PDF_PREFIX = "omp_utv_ks"
        val generator = PDFGenerator()
        private fun fullGyldigMelding(
            soknadsId: String,
            legeerklæringVedleggId: List<String> = listOf(),
        ) = OMPUTVKroniskSyktBarnSøknadMottatt(
            språk = "nb",
            søknadId = soknadsId,
            mottatt = ZonedDateTime.now(),
            søker = Søker(
                aktørId = "123456",
                fornavn = "Ærling",
                mellomnavn = "Øverbø",
                etternavn = "Ånsnes",
                fødselsnummer = "29099012345",
                fødselsdato = LocalDate.now().minusYears(20)
            ),
            barn = Barn(
                norskIdentifikator = "02119970078",
                fødselsdato = LocalDate.now(),
                aktørId = "123456",
                navn = "Ole Dole"
            ),
            legeerklæringVedleggId = legeerklæringVedleggId,
            relasjonTilBarnet = SøkerBarnRelasjon.MOR,
            kroniskEllerFunksjonshemming = true,
            harForståttRettigheterOgPlikter = true,
            harBekreftetOpplysninger = true,
            sammeAdresse = BarnSammeAdresse.JA_DELT_BOSTED,
            høyereRisikoForFravær = true,
            høyereRisikoForFraværBeskrivelse = "Beskrivelse av høyere risiko for fravær",
            k9FormatSøknad = SøknadUtils.defaultK9Format(soknadsId, ZonedDateTime.now())
        )
    }

    fun genererOppsummeringsPdfer(writeBytes: Boolean) {
        var id = "1-full-søknad"
        var pdf = generator.genererPDF(
            pdfData = fullGyldigMelding(soknadsId = id).pdfData(),
        )
        if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

        id = "2-full-søknad-legeerklæring-lastet-opp"
        pdf = generator.genererPDF(
            pdfData = fullGyldigMelding(soknadsId = id, legeerklæringVedleggId = listOf("123")).pdfData(),
        )
        if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)
    }
}

