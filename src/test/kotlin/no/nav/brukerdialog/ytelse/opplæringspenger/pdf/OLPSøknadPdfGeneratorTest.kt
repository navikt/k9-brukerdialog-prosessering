package no.nav.brukerdialog.ytelse.opplæringspenger.pdf

import no.nav.brukerdialog.pdf.PDFGenerator
import no.nav.brukerdialog.utils.K9FormatUtils
import no.nav.brukerdialog.utils.PathUtils.pdfPath
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.OLPMottattSøknad
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.felles.*
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
        fun fullGyldigMelding(soknadsId: String, mottatt: ZonedDateTime = ZonedDateTime.now()): OLPMottattSøknad {
            return OLPMottattSøknad(
                språk = "nb",
                søknadId = soknadsId,
                mottatt = mottatt,
                fraOgMed = LocalDate.parse("2022-01-01"),
                tilOgMed = LocalDate.parse("2022-02-01"),
                søker = Søker(
                    aktørId = "123456",
                    fornavn = "Ærling",
                    mellomnavn = "ØVERBØ",
                    etternavn = "ÅNSNES",
                    fødselsnummer = "29099012345",
                    fødselsdato = LocalDate.parse("1990-09-29"),
                ),
                barn = Barn(
                    fødselsnummer = "02119970078",
                    navn = "OLE DOLE",
                    aktørId = "11111111111"
                ),
                vedleggId = listOf("123", "456"),
                medlemskap = Medlemskap(
                    harBoddIUtlandetSiste12Mnd = true,
                    utenlandsoppholdSiste12Mnd = listOf(
                        Bosted(
                            LocalDate.of(2020, 1, 2),
                            LocalDate.of(2020, 1, 3),
                            "US", "USA"
                        )
                    ),
                    skalBoIUtlandetNeste12Mnd = false
                ),
                harForståttRettigheterOgPlikter = true,
                harBekreftetOpplysninger = true,
                utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(
                    skalOppholdeSegIUtlandetIPerioden = true,
                    opphold = listOf(
                        Utenlandsopphold(
                            fraOgMed = LocalDate.parse("2020-01-01"),
                            tilOgMed = LocalDate.parse("2020-01-10"),
                            landnavn = "Bahamas",
                            landkode = "BAH",
                            erUtenforEøs = true,
                            erBarnetInnlagt = true,
                            erSammenMedBarnet = true,
                            perioderBarnetErInnlagt = listOf(
                                Periode(
                                    fraOgMed = LocalDate.parse("2020-01-01"),
                                    tilOgMed = LocalDate.parse("2020-01-01")
                                ),
                                Periode(
                                    fraOgMed = LocalDate.parse("2020-01-03"),
                                    tilOgMed = LocalDate.parse("2020-01-04")
                                )
                            ),
                            årsak = Årsak.ANNET
                        ),
                        Utenlandsopphold(
                            fraOgMed = LocalDate.parse("2020-01-01"),
                            tilOgMed = LocalDate.parse("2020-01-10"),
                            landnavn = "Sverige",
                            landkode = "BHS",
                            erUtenforEøs = false,
                            erBarnetInnlagt = true,
                            erSammenMedBarnet = true,
                            perioderBarnetErInnlagt = listOf(
                                Periode(
                                    fraOgMed = LocalDate.parse("2020-01-01"),
                                    tilOgMed = LocalDate.parse("2020-01-01")
                                ),
                                Periode(
                                    fraOgMed = LocalDate.parse("2020-01-03"),
                                    tilOgMed = LocalDate.parse("2020-01-04")
                                ),
                                Periode(
                                    fraOgMed = LocalDate.parse("2020-01-05"),
                                    tilOgMed = LocalDate.parse("2020-01-05")
                                )
                            ),
                            årsak = Årsak.ANNET
                        )
                    )
                ),
                ferieuttakIPerioden = FerieuttakIPerioden(
                    skalTaUtFerieIPerioden = true,
                    ferieuttak = listOf(
                        Ferieuttak(fraOgMed = LocalDate.parse("2020-01-01"), tilOgMed = LocalDate.parse("2020-01-05")),
                        Ferieuttak(fraOgMed = LocalDate.parse("2020-01-07"), tilOgMed = LocalDate.parse("2020-01-15")),
                        Ferieuttak(fraOgMed = LocalDate.parse("2020-02-01"), tilOgMed = LocalDate.parse("2020-02-05"))
                    )
                ),
                utenlandskNæring = listOf(
                    UtenlandskNæring(
                        næringstype = Næringstype.FISKE,
                        navnPåVirksomheten = "Fiskeriet AS",
                        land = Land(landkode = "NDL", landnavn = "Nederland"),
                        organisasjonsnummer = "123ABC",
                        fraOgMed = LocalDate.parse("2020-01-09")
                    )
                ),

                stønadGodtgjørelse = StønadGodtgjørelse(
                    mottarStønadGodtgjørelse = true,
                    startdato = LocalDate.now().minusDays(10),
                    sluttdato = LocalDate.now().plusDays(10)
                ),
                arbeidsgivere = listOf(
                    Arbeidsgiver(
                        navn = "Org",
                        organisasjonsnummer = "917755736",
                        erAnsatt = true,
                        arbeidsforhold = Arbeidsforhold(
                            jobberNormaltTimer = 40.0,
                            arbeidIPeriode = ArbeidIPeriode(
                                jobberIPerioden = JobberIPeriodeSvar.REDUSERT,
                                enkeltdager = listOf(
                                    Enkeltdag(LocalDate.parse("2022-01-01"), Duration.ofHours(4)),
                                    Enkeltdag(LocalDate.parse("2022-01-02"), Duration.ofHours(4)),
                                    Enkeltdag(LocalDate.parse("2022-01-03"), Duration.ofHours(4)),
                                    Enkeltdag(LocalDate.parse("2022-01-04"), Duration.ofHours(4)),
                                    Enkeltdag(LocalDate.parse("2022-02-01"), Duration.ofHours(4)),
                                    Enkeltdag(LocalDate.parse("2022-02-02"), Duration.ofHours(4)),
                                    Enkeltdag(LocalDate.parse("2022-04-10"), Duration.ofHours(4)),
                                )
                            )
                        )
                    ),
                    Arbeidsgiver(
                        navn = "JobberIkkeHerLenger",
                        organisasjonsnummer = "977155436",
                        erAnsatt = false,
                        sluttetFørSøknadsperiode = false
                    )
                ),
                frilans = Frilans(
                    startdato = LocalDate.parse("2019-01-01"),
                    jobberFortsattSomFrilans = false,
                    sluttdato = LocalDate.parse("2021-05-01"),
                    harHattInntektSomFrilanser = false
                ),
                selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
                    virksomhet = Virksomhet(
                        fraOgMed = LocalDate.parse("2015-01-01"),
                        tilOgMed = LocalDate.parse("2021-01-01"),
                        næringstype = Næringstype.ANNEN,
                        fiskerErPåBladB = true,
                        navnPåVirksomheten = "Bjarnes Bakeri",
                        registrertINorge = false,
                        registrertIUtlandet = Land("CUB", "Cuba"),
                        næringsinntekt = 9656876,
                        erNyoppstartet = false,
                        harFlereAktiveVirksomheter = false
                    ),
                    arbeidsforhold = Arbeidsforhold(
                        37.5, ArbeidIPeriode(
                            JobberIPeriodeSvar.HELT_FRAVÆR,
                            listOf(
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
                ),
                harVærtEllerErVernepliktig = true,
                barnRelasjon = BarnRelasjon.ANNET,
                barnRelasjonBeskrivelse = "Blaabla annet",
                k9FormatSøknad = K9FormatUtils.defaultK9FormatPSB(soknadsId, mottatt),
                kurs = Kurs(
                    kursholder = Kursholder(
                        id = "0edb9541-dda5-4dc4-bfaf-587d01448a6a",
                        navn = "Opplæring for kurs AS"
                    ),
                    perioder = listOf(
                        KursPerioderMedReiseTid(
                            fraOgMed = LocalDate.parse("2020-01-01").atStartOfDay(),
                            tilOgMed = LocalDate.parse("2020-01-10").atStartOfDay(),
                            avreise = LocalDate.parse("2020-01-01"),
                            hjemkomst = LocalDate.parse("2020-01-10")
                        )
                    )
                ),
                opptjeningIUtlandet = listOf(
                    OpptjeningIUtlandet(
                        navn = "Yolo AS",
                        opptjeningType = OpptjeningType.ARBEIDSTAKER,
                        land = Land(landkode = "NDL", landnavn = "Nederland"),
                        fraOgMed = LocalDate.parse("2020-01-01"),
                        tilOgMed = LocalDate.parse("2020-10-01")
                    )
                ),
            )
        }

        fun genererOppsummeringsPdfer(writeBytes: Boolean) {
            var id = "1-full-søknad"
            var pdf = generator.genererPDF(
                pdfData = fullGyldigMelding(soknadsId = id, mottatt = ZonedDateTime.now()).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "2-utenSpråk"
            pdf = generator.genererPDF(
                pdfData = fullGyldigMelding(id).copy(språk = "nb").pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "3-utenArbeidsgivere"
            pdf = generator.genererPDF(
                pdfData = fullGyldigMelding(id).copy(arbeidsgivere = listOf()).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "4-flerePlanlagteUtenlandsopphold"
            pdf = generator.genererPDF(
                pdfData = fullGyldigMelding(id).copy(
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
                pdfData = fullGyldigMelding(id).copy(vedleggId = listOf("12345")).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "6-kun-frilans-arbeidsforhold"
            pdf = generator.genererPDF(
                pdfData = fullGyldigMelding(id).copy(
                    selvstendigNæringsdrivende = null,
                    arbeidsgivere = listOf()
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "7-barn-med-årsakManglerIdentitetsnummer"
            pdf = generator.genererPDF(
                pdfData = fullGyldigMelding(id).copy(
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
                pdfData = fullGyldigMelding(id).copy(
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
                pdfData = fullGyldigMelding(id).copy(
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
                pdfData = fullGyldigMelding(id).copy(
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
                pdfData = fullGyldigMelding(id).copy(
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

            id = "4-sluttet-som-frilans-før-søknadsperioden"
            pdf = generator.genererPDF(
                pdfData = fullGyldigMelding(id).copy(
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
