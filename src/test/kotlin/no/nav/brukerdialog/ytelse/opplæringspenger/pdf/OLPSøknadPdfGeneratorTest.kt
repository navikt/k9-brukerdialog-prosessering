package no.nav.brukerdialog.ytelse.opplæringspenger.pdf

import no.nav.brukerdialog.pdf.PDFGenerator
import no.nav.brukerdialog.utils.PathUtils.pdfPath
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.felles.*
import no.nav.brukerdialog.ytelse.opplæringspenger.utils.OlpPdfSøknadUtils
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDate
import java.time.ZonedDateTime

class OLPSøknadPdfGeneratorTest {

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
        const val PDF_PREFIX = "olp"
        val generator = PDFGenerator()

        fun genererOppsummeringsPdfer(writeBytes: Boolean) {
            var id = "1-full-søknad"
            var pdf = generator.genererPDF(
                pdfData = OlpPdfSøknadUtils.gyldigSøknad(soknadsId = id, mottatt = ZonedDateTime.now()).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "2-språk-nynorsk"
            pdf = generator.genererPDF(
                pdfData = OlpPdfSøknadUtils.gyldigSøknad(id).copy(språk = "nn").pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "3-utenArbeidsgivere"
            pdf = generator.genererPDF(
                pdfData = OlpPdfSøknadUtils.gyldigSøknad(id).copy(arbeidsgivere = listOf()).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "4-flerePlanlagteUtenlandsopphold"
            pdf = generator.genererPDF(
                pdfData = OlpPdfSøknadUtils.gyldigSøknad(id).copy(
                    medlemskap = Medlemskap(
                        harBoddIUtlandetSiste12Mnd = false,
                        utenlandsoppholdSiste12Mnd = listOf(),
                        skalBoIUtlandetNeste12Mnd = true,
                        utenlandsoppholdNeste12Mnd = listOf(
                            Bosted(
                                LocalDate.of(2022, 1, 2),
                                LocalDate.of(2022, 1, 3),
                                "US", "USA"
                            ), Bosted(
                                LocalDate.of(2022, 1, 3),
                                LocalDate.of(2022, 1, 4),
                                "DK", "Danmark"
                            )
                        )
                    )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "5-har-lastet-opp-vedlegg"
            pdf = generator.genererPDF(
                pdfData = OlpPdfSøknadUtils.gyldigSøknad(id).copy(vedleggId = listOf("12345")).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "6-kun-frilans-arbeidsforhold"
            pdf = generator.genererPDF(
                pdfData = OlpPdfSøknadUtils.gyldigSøknad(id).copy(
                    selvstendigNæringsdrivende = null,
                    arbeidsgivere = listOf()
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "7-barn-med-årsakManglerIdentitetsnummer"
            pdf = generator.genererPDF(
                pdfData = OlpPdfSøknadUtils.gyldigSøknad(id).copy(
                    barn = Barn(
                        navn = "OLE DOLE",
                        fødselsdato = LocalDate.now(),
                        årsakManglerIdentitetsnummer = ÅrsakManglerIdentitetsnummer.NYFØDT,
                        aktørId = "11111111111"
                    )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "8-med-opptjening-i-utlandet"
            pdf = generator.genererPDF(
                pdfData = OlpPdfSøknadUtils.gyldigSøknad(id).copy(
                    utenlandskNæring = listOf(),
                    opptjeningIUtlandet = listOf(
                        OpptjeningIUtlandet(
                            navn = "Kiwi AS",
                            opptjeningType = OpptjeningType.ARBEIDSTAKER,
                            land = Land(
                                landkode = "IKKE GYLDIG",
                                landnavn = "Belgia",
                            ),
                            fraOgMed = LocalDate.parse("2022-01-01"),
                            tilOgMed = LocalDate.parse("2022-01-10")
                        )
                    )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "9-med-utenlandsk-næring"
            pdf = generator.genererPDF(
                pdfData = OlpPdfSøknadUtils.gyldigSøknad(id).copy(
                    selvstendigNæringsdrivende = null,
                    frilans = null,
                    arbeidsgivere = listOf(),
                    utenlandskNæring = listOf(
                        UtenlandskNæring(
                            næringstype = Næringstype.FISKE,
                            navnPåVirksomheten = "Fiskeriet AS",
                            land = Land(landkode = "NDL", landnavn = "Nederland"),
                            organisasjonsnummer = "123ABC",
                            fraOgMed = LocalDate.parse("2020-01-09")
                        ),
                        UtenlandskNæring(
                            næringstype = Næringstype.DAGMAMMA,
                            navnPåVirksomheten = "Dagmamma AS",
                            land = Land(landkode = "NDL", landnavn = "Nederland"),
                            organisasjonsnummer = null,
                            fraOgMed = LocalDate.parse("2020-01-09"),
                            tilOgMed = LocalDate.parse("2022-01-09")
                        )
                    )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "10-har-lastet-opp-id-ved-manglende-norskIdentifikator"
            pdf = generator.genererPDF(
                pdfData = OlpPdfSøknadUtils.gyldigSøknad(id).copy(
                    barn = Barn(
                        navn = "Barn uten norsk identifikasjonsnummer",
                        fødselsnummer = null,
                        fødselsdato = LocalDate.now().minusDays(7),
                        aktørId = null,
                        årsakManglerIdentitetsnummer = ÅrsakManglerIdentitetsnummer.NYFØDT
                    ),
                    fødselsattestVedleggId = listOf("123")
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "11-har-ikke-lastet-opp-id-ved-manglende-norskIdentifikator"
            pdf = generator.genererPDF(
                pdfData = OlpPdfSøknadUtils.gyldigSøknad(id).copy(
                    barn = Barn(
                        navn = "Barn uten norsk identifikasjonsnummer",
                        fødselsnummer = null,
                        fødselsdato = LocalDate.now().minusYears(45),
                        aktørId = null,
                        årsakManglerIdentitetsnummer = ÅrsakManglerIdentitetsnummer.BARNET_BOR_I_UTLANDET
                    ),
                    fødselsattestVedleggId = listOf()
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "12-sluttet-som-frilans-før-søknadsperioden"
            pdf = generator.genererPDF(
                pdfData = OlpPdfSøknadUtils.gyldigSøknad(id).copy(
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
        }
    }
}
