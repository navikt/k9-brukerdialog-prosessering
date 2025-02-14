package no.nav.brukerdialog.ytelse.opplæringspenger.pdf

import no.nav.brukerdialog.pdf.PDFGenerator
import no.nav.brukerdialog.utils.PathUtils.pdfPath
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.felles.*
import no.nav.brukerdialog.ytelse.opplæringspenger.utils.OlpPdfSøknadUtils
import no.nav.k9.søknad.felles.type.Periode
import org.junit.jupiter.api.Test
import java.io.File
import java.time.Duration
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

            id = "4-jobber-som-fisker"
            pdf = generator.genererPDF(
                pdfData = OlpPdfSøknadUtils.gyldigSøknad(id).copy(
                    selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
                        virksomhet = Virksomhet(
                            fraOgMed = LocalDate.parse("2015-01-01"),
                            tilOgMed = LocalDate.parse("2021-01-01"),
                            næringstype = Næringstype.FISKE,
                            fiskerErPåBladB = true,
                            navnPåVirksomheten = "Fisk Fiskeri",
                            organisasjonsnummer = "123456789",
                            registrertINorge = true,
                            registrertIUtlandet = null,
                            næringsinntekt = 9656876,
                            erNyoppstartet = false,
                            harFlereAktiveVirksomheter = false
                        ),
                        arbeidsforhold = Arbeidsforhold(
                            37.5, ArbeidIPeriode(
                                JobberIPeriodeSvar.HELT_FRAVÆR, listOf(
                                    Enkeltdag(LocalDate.parse("2021-01-01"), Duration.ofHours(7).plusMinutes(30)),
                                    Enkeltdag(LocalDate.parse("2021-01-02"), Duration.ofHours(7).plusMinutes(30)),
                                    Enkeltdag(LocalDate.parse("2021-01-03"), Duration.ofHours(7).plusMinutes(30)),
                                    Enkeltdag(LocalDate.parse("2021-01-04"), Duration.ofHours(7).plusMinutes(30)),
                                    Enkeltdag(LocalDate.parse("2021-01-05"), Duration.ofHours(7).plusMinutes(30)),
                                    Enkeltdag(LocalDate.parse("2021-01-06"), Duration.ofHours(7).plusMinutes(30)),
                                    Enkeltdag(LocalDate.parse("2021-01-07"), Duration.ofHours(7).plusMinutes(30)),
                                    Enkeltdag(LocalDate.parse("2021-01-08"), Duration.ofHours(7).plusMinutes(30)),
                                    Enkeltdag(LocalDate.parse("2021-01-09"), Duration.ofHours(7).plusMinutes(30)),
                                    Enkeltdag(LocalDate.parse("2021-01-10"), Duration.ofHours(7).plusMinutes(30)),
                                )
                            )
                        ),
                    )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "5-flerePlanlagteUtenlandsopphold"
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

            id = "6-har-ikke-lastet-opp-vedlegg"
            pdf = generator.genererPDF(
                pdfData = OlpPdfSøknadUtils.gyldigSøknad(id).copy(vedleggId = listOf()).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "7-kun-frilans-arbeidsforhold"
            pdf = generator.genererPDF(
                pdfData = OlpPdfSøknadUtils.gyldigSøknad(id).copy(
                    selvstendigNæringsdrivende = null,
                    arbeidsgivere = listOf()
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "8-barn-med-nyfødt-barn"
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

            id = "9-barn-med-barnet-bor-i-utlandet"
            pdf = generator.genererPDF(
                pdfData = OlpPdfSøknadUtils.gyldigSøknad(id).copy(
                    barn = Barn(
                        navn = "OLE DOLE",
                        fødselsdato = LocalDate.now().minusYears(5),
                        årsakManglerIdentitetsnummer = ÅrsakManglerIdentitetsnummer.BARNET_BOR_I_UTLANDET,
                        aktørId = "11111111111"
                    )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "10-med-opptjening-i-utlandet"
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

            id = "11-med-utenlandsk-næring"
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

            id = "12-har-lastet-opp-id-ved-manglende-norskIdentifikator"
            pdf = generator.genererPDF(
                pdfData = OlpPdfSøknadUtils.gyldigSøknad(id).copy(
                    barn = Barn(
                        navn = "Barn uten norsk identifikasjonsnummer",
                        norskIdentifikator = null,
                        fødselsdato = LocalDate.now().minusDays(7),
                        aktørId = null,
                        årsakManglerIdentitetsnummer = ÅrsakManglerIdentitetsnummer.NYFØDT,
                        fødselsattestVedleggId = listOf("123")
                    )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "13-har-ikke-lastet-opp-id-ved-manglende-norskIdentifikator"
            pdf = generator.genererPDF(
                pdfData = OlpPdfSøknadUtils.gyldigSøknad(id).copy(
                    barn = Barn(
                        navn = "Barn uten norsk identifikasjonsnummer",
                        norskIdentifikator = null,
                        fødselsdato = LocalDate.now().minusYears(45),
                        aktørId = null,
                        årsakManglerIdentitetsnummer = ÅrsakManglerIdentitetsnummer.BARNET_BOR_I_UTLANDET,
                        fødselsattestVedleggId = listOf()
                    )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "14-sluttet-som-frilans-før-søknadsperioden"
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

            id = "15-med-2-perioder"
            pdf = generator.genererPDF(
                pdfData = OlpPdfSøknadUtils.gyldigSøknad(id).copy(
                    kurs = Kurs(
                        kursholder = "Senter for Kurs AS",
                        kursperioder = listOf(
                            Periode(LocalDate.parse("2020-01-02"), LocalDate.parse("2020-01-07")),
                            Periode(LocalDate.parse("2020-03-01"), LocalDate.parse("2020-03-02"))
                        ),
                        reise = Reise(
                            reiserUtenforKursdager = true,
                            reisedager = listOf(
                                LocalDate.parse("2020-01-01"),
                                LocalDate.parse("2020-01-07")
                            ),
                            reisedagerBeskrivelse = "Reisetid til kurs tok mer enn en dag. Det var en utrolig lang " +
                                    "og utmattende reise over fjellet som viste seg å være stengt på grunn av snø."
                        )
                    )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "16-uten-reise"
            pdf = generator.genererPDF(
                pdfData = OlpPdfSøknadUtils.gyldigSøknad(id).copy(
                    kurs = Kurs(
                        kursholder = "Senter for Kurs AS",
                        kursperioder = listOf(
                            Periode(LocalDate.parse("2020-01-02"), LocalDate.parse("2020-01-07")),
                            Periode(LocalDate.parse("2020-03-01"), LocalDate.parse("2020-03-02"))
                        ),
                        reise = Reise(
                            reiserUtenforKursdager = false
                        )
                    )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "17-med-relasjon-medmor"
            pdf = generator.genererPDF(
                pdfData = OlpPdfSøknadUtils.gyldigSøknad(id).copy(
                    barn = OlpPdfSøknadUtils.gyldigSøknad(id).barn.copy(
                        relasjonTilBarnet = BarnRelasjon.MEDMOR,
                        relasjonTilBarnetBeskrivelse = null
                    )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)
        }
    }
}
