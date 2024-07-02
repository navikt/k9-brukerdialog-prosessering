package no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetssluttfase

import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetssluttfase.utils.PilsSøknadUtils
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.FlereSokereSvar
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Frilans
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Land
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Næringstype
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Pleietrengende
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.UtenlandskNæring
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.ÅrsakManglerIdentitetsnummer
import no.nav.k9brukerdialogprosessering.pdf.PDFGenerator
import no.nav.k9brukerdialogprosessering.utils.PathUtils.pdfPath
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDate

class PILSPleiepengerSyktBarnSøknadPdfGeneratorTest {

    @Test
    fun `generering av oppsummerings-PDF fungerer`() {
        genererOppsummeringsPdfer(false)
    }

    @Test
    fun `opprett lesbar oppsummerings-PDF`() {
        genererOppsummeringsPdfer(true)
    }

    private companion object {
        const val PDF_PREFIX = "pils"
        val generator = PDFGenerator()

        fun genererOppsummeringsPdfer(writeBytes: Boolean) {
            var id = "1-full-søknad"
            var pdf = generator.genererPDF(PilsSøknadUtils.gyldigSøknad(søknadId = id).pdfData())
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "2-søknad-pleietrengende-uten-norskidentifikator"
            pdf = generator.genererPDF(
                PilsSøknadUtils.gyldigSøknad(søknadId = id).copy(
                    opptjeningIUtlandet = listOf(),
                    utenlandskNæring = listOf(),
                    pleietrengende = Pleietrengende(
                        navn = "Bjarne",
                        fødselsdato = LocalDate.now().minusYears(45),
                        årsakManglerIdentitetsnummer = ÅrsakManglerIdentitetsnummer.BOR_I_UTLANDET
                    )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "3-søknad-med-utenlandsk-næring"
            pdf = generator.genererPDF(
                PilsSøknadUtils.gyldigSøknad(søknadId = id).copy(
                    opptjeningIUtlandet = listOf(),
                    utenlandskNæring = listOf(
                        UtenlandskNæring(
                            næringstype = Næringstype.DAGMAMMA,
                            navnPåVirksomheten = "Dagmamma AS",
                            land = Land(landkode = "NDL", landnavn = "Nederland"),
                            organisasjonsnummer = "123ABC",
                            fraOgMed = LocalDate.parse("2022-01-01"),
                            tilOgMed = LocalDate.parse("2022-01-10")
                        ),
                        UtenlandskNæring(
                            næringstype = Næringstype.FISKE,
                            navnPåVirksomheten = "Fiskeriet AS",
                            land = Land(landkode = "NDL", landnavn = "Nederland"),
                            organisasjonsnummer = null,
                            fraOgMed = LocalDate.parse("2022-01-01")
                        )
                    )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "4-sluttet-som-frilans-før-søknadsperioden"
            pdf = generator.genererPDF(
                 PilsSøknadUtils.gyldigSøknad(søknadId = id).copy(
                    opptjeningIUtlandet = listOf(),
                    utenlandskNæring = listOf(),
                    fraOgMed = LocalDate.parse("2022-01-05"),
                    tilOgMed = LocalDate.parse("2022-01-10"),
                    frilans = Frilans(
                        startdato = LocalDate.parse("2000-01-01"),
                        jobberFortsattSomFrilans = false,
                        sluttdato = LocalDate.parse("2022-01-04"),
                        arbeidsforhold = null,
                        harHattInntektSomFrilanser = false
                    )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "5-har-lastet-opp-id-ved-manglende-norskIdentifikator"
            pdf = generator.genererPDF(
               PilsSøknadUtils.gyldigSøknad(søknadId = id).copy(
                    opptjeningIUtlandet = listOf(),
                    utenlandskNæring = listOf(),
                    opplastetIdVedleggId = listOf("123"),
                    pleietrengende = Pleietrengende(
                        navn = "Bjarne",
                        fødselsdato = LocalDate.now().minusYears(45),
                        årsakManglerIdentitetsnummer = ÅrsakManglerIdentitetsnummer.BOR_I_UTLANDET
                    )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "6-har-ikke-lastet-opp-id-ved-manglende-norskIdentifikator"
            pdf = generator.genererPDF(
                PilsSøknadUtils.gyldigSøknad(søknadId = id).copy(
                    opptjeningIUtlandet = listOf(),
                    utenlandskNæring = listOf(),
                    vedleggId = emptyList(),
                    opplastetIdVedleggId = emptyList(),
                    pleietrengende = Pleietrengende(
                        navn = "Bjarne",
                        fødselsdato = LocalDate.now().minusYears(45),
                        årsakManglerIdentitetsnummer = ÅrsakManglerIdentitetsnummer.BOR_I_UTLANDET
                    )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "7-flere-søkere-ja"
            pdf = generator.genererPDF(
                PilsSøknadUtils.gyldigSøknad(søknadId = id).copy(
                    flereSokere = FlereSokereSvar.JA
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)
        }
    }
}
