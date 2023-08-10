package no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn

import no.nav.helse.felles.Enkeltdag
import no.nav.helse.felles.Omsorgstilbud
import no.nav.helse.felles.OmsorgstilbudSvarFortid
import no.nav.helse.felles.OmsorgstilbudSvarFremtid
import no.nav.helse.felles.PlanUkedager
import no.nav.k9brukerdialogprosessering.meldinger.felles.domene.Søker
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.PSBMottattSøknad
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.ArbeidIPeriode
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.ArbeidIPeriodeType
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.ArbeiderIPeriodenSvar
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.ArbeidsUke
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Arbeidsforhold
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Arbeidsgiver
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Barn
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.BarnRelasjon
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Beredskap
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Bosted
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Ferieuttak
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.FerieuttakIPerioden
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Frilans
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.FrilansType
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Land
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Medlemskap
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Nattevåk
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.NormalArbeidstid
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Næringstyper
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.OpptjeningIUtlandet
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.OpptjeningType
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Periode
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Regnskapsfører
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.SelvstendigNæringsdrivende
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.StønadGodtgjørelse
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.UtenlandskNæring
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Utenlandsopphold
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.UtenlandsoppholdIPerioden
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.VarigEndring
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Virksomhet
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.YrkesaktivSisteTreFerdigliknedeÅrene
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Årsak
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.ÅrsakManglerIdentitetsnummer
import no.nav.k9brukerdialogprosessering.pdf.PDFGenerator
import no.nav.k9brukerdialogprosessering.utils.K9FormatUtils
import no.nav.k9brukerdialogprosessering.utils.PathUtils.pdfPath
import org.junit.jupiter.api.Test
import java.io.File
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime

class PSBSøknadPdfGeneratorTest {

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
        const val PDF_PREFIX = "psb"
        val generator = PDFGenerator()
        fun fullGyldigMelding(soknadsId: String, mottatt: ZonedDateTime = ZonedDateTime.now()): PSBMottattSøknad {
            return PSBMottattSøknad(
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
                nattevåk = Nattevåk(
                    harNattevåk = true,
                    tilleggsinformasjon = "Har nattevåk"
                ),
                beredskap = Beredskap(
                    beredskap = true,
                    tilleggsinformasjon = "Jeg er i beredskap\rmed\nlinje\r\nlinjeskift. Jeg har snakket med NAV\u0002konsulent"
                ),
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
                    misterHonorarer = true,
                    arbeidsforhold = Arbeidsforhold(
                        normalarbeidstid = NormalArbeidstid(
                            timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
                        ),
                        arbeidIPeriode = ArbeidIPeriode(
                            type = ArbeidIPeriodeType.ARBEIDER_VANLIG,
                            arbeiderIPerioden = ArbeiderIPeriodenSvar.SOM_VANLIG
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
                            type = ArbeidIPeriodeType.ARBEIDER_VANLIG,
                            arbeiderIPerioden = ArbeiderIPeriodenSvar.SOM_VANLIG
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
                                type = ArbeidIPeriodeType.ARBEIDER_VANLIG,
                                arbeiderIPerioden = ArbeiderIPeriodenSvar.SOM_VANLIG
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
                                type = ArbeidIPeriodeType.ARBEIDER_TIMER_I_SNITT_PER_UKE,
                                arbeiderIPerioden = ArbeiderIPeriodenSvar.REDUSERT,
                                timerPerUke = Duration.ofHours(37).plusMinutes(30)
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
                omsorgstilbud = Omsorgstilbud(
                    erLiktHverUke = true,
                    ukedager = PlanUkedager(
                        mandag = Duration.ofHours(3),
                        onsdag = Duration.ofHours(3),
                        fredag = Duration.ofHours(3)
                    ),
                    enkeltdager = listOf(
                        Enkeltdag(LocalDate.now(), Duration.ofHours(3)),
                        Enkeltdag(LocalDate.now().plusDays(3), Duration.ofHours(2)),
                        Enkeltdag(LocalDate.now().plusWeeks(4), Duration.ofHours(4)),
                        Enkeltdag(LocalDate.now().plusWeeks(4), Duration.ofHours(6).plusMinutes(45)),
                        Enkeltdag(LocalDate.now().plusWeeks(9).plusDays(2), Duration.ofHours(3))
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
                pdfData = fullGyldigMelding(id).copy(språk = null).pdfData()
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

            id = "6-omsorgstilbud-nei-til-omsorgstilbud"
            pdf = generator.genererPDF(
                pdfData = fullGyldigMelding(id).copy(
                    omsorgstilbud = null
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "7-omsorgstilbud-omsorgstilbud-enkeltdager"
            pdf = generator.genererPDF(
                pdfData = fullGyldigMelding(id).copy(
                    omsorgstilbud = Omsorgstilbud(
                        erLiktHverUke = false,
                        enkeltdager = listOf(
                            Enkeltdag(LocalDate.now(), Duration.ofHours(3)),
                            Enkeltdag(LocalDate.now().plusDays(3), Duration.ofHours(2)),
                            Enkeltdag(LocalDate.now().plusWeeks(4), Duration.ofHours(4)),
                            Enkeltdag(LocalDate.now().plusWeeks(4), Duration.ofHours(6).plusMinutes(45)),
                            Enkeltdag(LocalDate.now().plusWeeks(9).plusDays(2), Duration.ofHours(3))
                        )
                    )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "8-omsorgstilbud-omsorgstilbud-ukedager"
            pdf = generator.genererPDF(
                pdfData = fullGyldigMelding(id).copy(
                    omsorgstilbud = Omsorgstilbud(
                        erLiktHverUke = true,
                        ukedager = PlanUkedager(
                            mandag = Duration.ofHours(3),
                            onsdag = Duration.ofHours(3),
                            fredag = Duration.ofHours(3)
                        ),
                    )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "9-kun-frilans-arbeidsforhold"
            pdf = generator.genererPDF(
                pdfData = fullGyldigMelding(id).copy(
                    selvstendigNæringsdrivende = SelvstendigNæringsdrivende(harInntektSomSelvstendig = false),
                    arbeidsgivere = listOf()
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "10-barn-med-årsakManglerIdentitetsnummer"
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

            id = "11-med-opptjening-i-utlandet"
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

            id = "12-med-utenlandsk-næring"
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

            id = "13-omsorgstilbud-kunFortid"
            pdf = generator.genererPDF(
                pdfData = fullGyldigMelding(id).copy(
                    omsorgstilbud = Omsorgstilbud(
                        svarFortid = OmsorgstilbudSvarFortid.JA,
                        svarFremtid = null,
                        erLiktHverUke = true,
                        ukedager = PlanUkedager(
                            mandag = Duration.ofHours(3),
                            onsdag = Duration.ofHours(3),
                            fredag = Duration.ofHours(3)
                        ),
                    )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "14-omsorgstilbud-kunFremtid"
            pdf = generator.genererPDF(
                pdfData = fullGyldigMelding(id).copy(
                    omsorgstilbud = Omsorgstilbud(
                        svarFortid = null,
                        svarFremtid = OmsorgstilbudSvarFremtid.JA,
                        enkeltdager = listOf(
                            Enkeltdag(LocalDate.now(), Duration.ofHours(3)),
                            Enkeltdag(LocalDate.now().plusDays(3), Duration.ofHours(2)),
                        )
                    )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "15-omsorgstilbud-ja-fortidOgFremtid"
            pdf = generator.genererPDF(
                pdfData = fullGyldigMelding(id).copy(
                    omsorgstilbud = Omsorgstilbud(
                        svarFortid = OmsorgstilbudSvarFortid.JA,
                        svarFremtid = OmsorgstilbudSvarFremtid.JA,
                        erLiktHverUke = true,
                        ukedager = PlanUkedager(
                            mandag = Duration.ofHours(3),
                            onsdag = Duration.ofHours(3),
                            fredag = Duration.ofHours(3)
                        )
                    )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "16-omsorgstilbud-nei-fortidOgFremtid"
            pdf = generator.genererPDF(
                pdfData = fullGyldigMelding(id).copy(
                    omsorgstilbud = Omsorgstilbud(
                        svarFortid = OmsorgstilbudSvarFortid.NEI,
                        svarFremtid = OmsorgstilbudSvarFremtid.NEI
                    )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "17-har-lastet-opp-id-ved-manglende-norskIdentifikator"
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

            id = "18-har-ikke-lastet-opp-id-ved-manglende-norskIdentifikator"
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

            id = "19-ulike-uker_ulike_timer"
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
                                    type = ArbeidIPeriodeType.ARBEIDER_ULIKE_UKER_TIMER,
                                    arbeiderIPerioden = ArbeiderIPeriodenSvar.REDUSERT,
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
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "20-frilanser-med-honorarer"
            pdf = generator.genererPDF(
                pdfData = fullGyldigMelding(id).copy(
                   frilans = Frilans(
                       harInntektSomFrilanser = true,
                       type = FrilansType.FRILANS_HONORAR,
                       startdato = LocalDate.parse("2021-01-01"),
                       sluttdato = LocalDate.parse("2021-01-31"),
                       jobberFortsattSomFrilans = false,
                       misterHonorarer = null,
                       arbeidsforhold = Arbeidsforhold(
                           normalarbeidstid = NormalArbeidstid(
                               timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
                           ),
                           arbeidIPeriode = ArbeidIPeriode(
                               type = ArbeidIPeriodeType.ARBEIDER_VANLIG,
                               arbeiderIPerioden = ArbeiderIPeriodenSvar.SOM_VANLIG
                           )
                       )
                   )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "21-kun-honorarer"
            pdf = generator.genererPDF(
                pdfData = fullGyldigMelding(id).copy(
                   frilans = Frilans(
                       harInntektSomFrilanser = true,
                       type = FrilansType.HONORAR,
                       startdato = LocalDate.parse("2021-01-01"),
                       sluttdato = LocalDate.parse("2021-01-31"),
                       jobberFortsattSomFrilans = false,
                       misterHonorarer = true,
                       arbeidsforhold = Arbeidsforhold(
                           normalarbeidstid = NormalArbeidstid(
                               timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
                           ),
                           arbeidIPeriode = ArbeidIPeriode(
                               type = ArbeidIPeriodeType.ARBEIDER_VANLIG,
                               arbeiderIPerioden = ArbeiderIPeriodenSvar.SOM_VANLIG
                           )
                       )
                   )
                ).pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)
        }
    }
}
