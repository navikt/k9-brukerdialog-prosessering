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
                fraOgMed = LocalDate.now().plusDays(6),
                tilOgMed = LocalDate.now().plusDays(35),
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
                        næringstype = Næringstyper.FISKE,
                        navnPåVirksomheten = "Fiskeriet AS",
                        land = Land(landkode = "NDL", landnavn = "Nederland"),
                        organisasjonsnummer = "123ABC",
                        fraOgMed = LocalDate.parse("2020-01-09")
                    )
                ),
                frilans = Frilans(
                    harInntektSomFrilanser = true,
                    startdato = LocalDate.now().minusYears(3),
                    sluttdato = LocalDate.now(),
                    jobberFortsattSomFrilans = false,
                    type = FrilansType.FRILANS,
                    misterHonorar = true,
                    arbeidsforhold = Arbeidsforhold(
                        normalarbeidstid = NormalArbeidstid(
                            timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
                        ),
                        arbeidIPeriode = ArbeidIPeriode(
                            type = ArbeidIPeriodeType.ARBEIDER_VANLIG
                        )
                    )
                ),
                stønadGodtgjørelse = StønadGodtgjørelse(
                    mottarStønadGodtgjørelse = true,
                    startdato = LocalDate.now().minusDays(10),
                    sluttdato = LocalDate.now().plusDays(10)
                ),
                selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
                    harInntektSomSelvstendig = true,
                    virksomhet = Virksomhet(
                        næringstype = Næringstyper.FISKE,
                        fiskerErPåBladB = true,
                        fraOgMed = LocalDate.now(),
                        næringsinntekt = 1111,
                        navnPåVirksomheten = "Tull Og Tøys",
                        registrertINorge = false,
                        registrertIUtlandet = Land(
                            landkode = "DEU",
                            landnavn = "Tyskland"
                        ),
                        yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeÅrene(LocalDate.now()),
                        varigEndring = VarigEndring(
                            dato = LocalDate.now().minusDays(20),
                            inntektEtterEndring = 234543,
                            forklaring = "Forklaring som handler om varig endring"
                        ),
                        regnskapsfører = Regnskapsfører(
                            navn = "Bjarne Regnskap",
                            telefon = "65484578"
                        ),
                        harFlereAktiveVirksomheter = true
                    ),
                    arbeidsforhold = Arbeidsforhold(
                        normalarbeidstid = NormalArbeidstid(
                            timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
                        ),
                        arbeidIPeriode = ArbeidIPeriode(
                            type = ArbeidIPeriodeType.ARBEIDER_VANLIG
                        )
                    )
                ),
                arbeidsgivere = listOf(
                    Arbeidsgiver(
                        navn = "Peppes",
                        organisasjonsnummer = "917755736",
                        erAnsatt = true,
                        arbeidsforhold = Arbeidsforhold(
                            normalarbeidstid = NormalArbeidstid(
                                timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
                            ),
                            arbeidIPeriode = ArbeidIPeriode(
                                type = ArbeidIPeriodeType.ARBEIDER_VANLIG
                            )
                        )
                    ),
                    Arbeidsgiver(
                        navn = "Pizzabakeren",
                        organisasjonsnummer = "917755736",
                        erAnsatt = true,
                        arbeidsforhold = Arbeidsforhold(
                            normalarbeidstid = NormalArbeidstid(
                                timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
                            ),
                            arbeidIPeriode = ArbeidIPeriode(
                                type = ArbeidIPeriodeType.ARBEIDER_REDUSERT,
                                redusertArbeid = ArbeidsRedusert(
                                    type = RedusertArbeidstidType.TIMER_I_SNITT_PER_UKE,
                                    timerPerUke = Duration.ofHours(37).plusMinutes(30)
                                )
                            )
                        )
                    ),
                    Arbeidsgiver(
                        navn = "Sluttaaaa",
                        organisasjonsnummer = "917755736",
                        erAnsatt = false,
                        arbeidsforhold = null,
                        sluttetFørSøknadsperiode = true
                    )
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
                    selvstendigNæringsdrivende = SelvstendigNæringsdrivende(harInntektSomSelvstendig = false),
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
                    selvstendigNæringsdrivende = SelvstendigNæringsdrivende(false),
                    frilans = Frilans(false),
                    arbeidsgivere = listOf(),
                    utenlandskNæring = listOf(
                        UtenlandskNæring(
                            næringstype = Næringstyper.FISKE,
                            navnPåVirksomheten = "Fiskeriet AS",
                            land = Land(landkode = "NDL", landnavn = "Nederland"),
                            organisasjonsnummer = "123ABC",
                            fraOgMed = LocalDate.parse("2020-01-09")
                        ),
                        UtenlandskNæring(
                            næringstype = Næringstyper.DAGMAMMA,
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

            id = "12-ulike-uker_ulike_timer"
            pdf = generator.genererPDF(
                pdfData = fullGyldigMelding(id).copy(
                    arbeidsgivere = listOf(
                        Arbeidsgiver(
                            navn = "Varierende frisør",
                            organisasjonsnummer = "917755736",
                            erAnsatt = true,
                            arbeidsforhold = Arbeidsforhold(
                                normalarbeidstid = NormalArbeidstid(
                                    timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
                                ),
                                arbeidIPeriode = ArbeidIPeriode(
                                    type = ArbeidIPeriodeType.ARBEIDER_REDUSERT,
                                    redusertArbeid = ArbeidsRedusert(
                                        type = RedusertArbeidstidType.ULIKE_UKER_TIMER,
                                        arbeidsuker = listOf(
                                            ArbeidsUke(
                                                periode = Periode(
                                                    fraOgMed = LocalDate.parse("2022-10-24"),
                                                    tilOgMed = LocalDate.parse("2022-10-30")
                                                ),
                                                timer = Duration.ofHours(25).plusMinutes(30)
                                            ),
                                            ArbeidsUke(
                                                periode = Periode(
                                                    fraOgMed = LocalDate.parse("2022-10-31"),
                                                    tilOgMed = LocalDate.parse("2022-11-06")
                                                ),
                                                timer = Duration.ofHours(15).plusMinutes(30)
                                            ),
                                            ArbeidsUke(
                                                periode = Periode(
                                                    fraOgMed = LocalDate.parse("2022-10-17"),
                                                    tilOgMed = LocalDate.parse("2022-10-23")
                                                ),
                                                timer = Duration.ofHours(37).plusMinutes(30)
                                            ),
                                            ArbeidsUke(
                                                periode = Periode(
                                                    fraOgMed = LocalDate.parse("2022-11-14"),
                                                    tilOgMed = LocalDate.parse("2022-11-20")
                                                ),
                                                timer = Duration.ofHours(5).plusMinutes(30)
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "13-frilanser-med-honorarer"
            pdf = generator.genererPDF(
                pdfData = fullGyldigMelding(id).copy(
                    frilans = Frilans(
                        harInntektSomFrilanser = true,
                        type = FrilansType.FRILANS_HONORAR,
                        startdato = LocalDate.parse("2021-01-01"),
                        sluttdato = LocalDate.parse("2021-01-31"),
                        jobberFortsattSomFrilans = false,
                        misterHonorar = null,
                        arbeidsforhold = Arbeidsforhold(
                            normalarbeidstid = NormalArbeidstid(
                                timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
                            ),
                            arbeidIPeriode = ArbeidIPeriode(
                                type = ArbeidIPeriodeType.ARBEIDER_VANLIG
                            )
                        )
                    )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "14-kun-honorarer"
            pdf = generator.genererPDF(
                pdfData = fullGyldigMelding(id).copy(
                    frilans = Frilans(
                        harInntektSomFrilanser = true,
                        type = FrilansType.HONORAR,
                        startdato = LocalDate.parse("2021-01-01"),
                        sluttdato = LocalDate.parse("2021-01-31"),
                        jobberFortsattSomFrilans = false,
                        misterHonorar = true,
                        arbeidsforhold = Arbeidsforhold(
                            normalarbeidstid = NormalArbeidstid(
                                timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
                            ),
                            arbeidIPeriode = ArbeidIPeriode(
                                type = ArbeidIPeriodeType.ARBEIDER_IKKE
                            )
                        )
                    )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "15-frilanser-starter-tre-måneder-før-søknadsperiode"
            pdf = generator.genererPDF(
                pdfData = fullGyldigMelding(id).copy(
                    fraOgMed = LocalDate.parse("2023-08-31"),
                    frilans = Frilans(
                        harInntektSomFrilanser = true,
                        startetFørSisteTreHeleMåneder = true,
                        type = FrilansType.FRILANS,
                        startdato = LocalDate.parse("2023-05-30"),
                        jobberFortsattSomFrilans = true,
                        misterHonorar = false,
                        arbeidsforhold = Arbeidsforhold(
                            normalarbeidstid = NormalArbeidstid(
                                timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
                            ),
                            arbeidIPeriode = ArbeidIPeriode(
                                type = ArbeidIPeriodeType.ARBEIDER_IKKE
                            )
                        )
                    )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "16-uke-53"
            pdf = generator.genererPDF(
                pdfData = fullGyldigMelding(id).copy(
                    fraOgMed = LocalDate.parse("2024-12-30"),
                    tilOgMed = LocalDate.parse("2025-01-02"),
                    arbeidsgivere = listOf(
                        Arbeidsgiver(
                            navn = "Varierende frisør",
                            organisasjonsnummer = "917755736",
                            erAnsatt = true,
                            arbeidsforhold = Arbeidsforhold(
                                normalarbeidstid = NormalArbeidstid(
                                    timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
                                ),
                                arbeidIPeriode = ArbeidIPeriode(
                                    type = ArbeidIPeriodeType.ARBEIDER_REDUSERT,
                                    redusertArbeid = ArbeidsRedusert(
                                        type = RedusertArbeidstidType.ULIKE_UKER_TIMER,
                                        arbeidsuker = listOf(
                                            // Uke 52
                                            ArbeidsUke(
                                                periode = Periode(
                                                    fraOgMed = LocalDate.parse("2024-12-23"),
                                                    tilOgMed = LocalDate.parse("2024-12-27")
                                                ),
                                                timer = Duration.ofHours(25).plusMinutes(30)
                                            ),
                                            // Uke 53 -> 1
                                            ArbeidsUke(
                                                periode = Periode(
                                                    fraOgMed = LocalDate.parse("2024-12-30"),
                                                    tilOgMed = LocalDate.parse("2025-01-03")
                                                ),
                                                timer = Duration.ofHours(15).plusMinutes(30)
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)
        }
    }
}
