package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.utils

import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.PSBMottattSøknad
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.ArbeidIPeriode
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.ArbeidIPeriodeType
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Arbeidsforhold
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Arbeidsgiver
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Barn
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Beredskap
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Bosted
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Ferieuttak
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.FerieuttakIPerioden
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Frilans
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.FrilansType
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Land
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Medlemskap
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Nattevåk
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.NormalArbeidstid
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Næringstyper
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.OpptjeningIUtlandet
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.OpptjeningType
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Periode
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.SelvstendigNæringsdrivende
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.UtenlandskNæring
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Utenlandsopphold
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.UtenlandsoppholdIPerioden
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Virksomhet
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.YrkesaktivSisteTreFerdigliknedeÅrene
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Årsak
import no.nav.brukerdialog.utils.K9FormatUtils.defaultK9FormatPSB
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.fosterhjemgodtgjørelse.FosterhjemgodtgjørelseType
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.fosterhjemgodtgjørelse.FosterhjemsgodtgjørelseFrikjøpt
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.omsorgsstønad.OmsorgsstønadMottarDelerAvPerioden
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.omsorgsstønad.OmsorgsstønadType
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime

internal object PSBSøknadUtils {

    internal fun psbMottattSøknad(søknadId: String, mottatt: ZonedDateTime) = PSBMottattSøknad(
        språk = "nb",
        søknadId = søknadId,
        mottatt = mottatt,
        fraOgMed = LocalDate.parse("2021-01-01"),
        tilOgMed = LocalDate.parse("2021-01-01"),
        søker = Søker(
            aktørId = "123456",
            fødselsnummer = "02119970078",
            etternavn = "Nordmann",
            mellomnavn = "Mellomnavn",
            fornavn = "Ola",
            fødselsdato = LocalDate.parse("1999-11-02"),
        ),
        barn = Barn(
            navn = "Ole Dole",
            fødselsnummer = "02119970078",
            aktørId = "11111111111"
        ),
        vedleggId = listOf("123", "456"),
        fødselsattestVedleggId = listOf("789"),
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
        harBekreftetOpplysninger = true,
        harForståttRettigheterOgPlikter = true,
        beredskap = Beredskap(
            beredskap = true,
            tilleggsinformasjon = "I Beredskap"
        ),
        nattevåk = Nattevåk(
            harNattevåk = true,
            tilleggsinformasjon = "Har Nattevåk"
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
                    landnavn = "Svergie",
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
                Ferieuttak(LocalDate.parse("2020-01-07"), LocalDate.parse("2020-01-08")),
                Ferieuttak(LocalDate.parse("2020-01-09"), LocalDate.parse("2020-01-10"))
            )
        ),
        frilans = Frilans(
            harInntektSomFrilanser = true,
            startetFørSisteTreHeleMåneder = true,
            startdato = LocalDate.parse("2019-01-01"),
            jobberFortsattSomFrilans = true,
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
        fosterhjemgodtgjørelse = FosterhjemsgodtgjørelseFrikjøpt(
            type = FosterhjemgodtgjørelseType.MOTTAR_FRIKJØPT,
            mottarFosterhjemsgodtgjørelse = true,
            erFrikjøptFraJobb = true,
            frikjøptBeskrivelse = "Frikjøpt fra jobb",
        ),
        omsorgsstønad = OmsorgsstønadMottarDelerAvPerioden(
            type = OmsorgsstønadType.MOTTAR_I_DELER_AV_PERIODEN,
            mottarOmsorgsstønad = true,
            startdato = LocalDate.parse("2023-03-13"),
            sluttdato = LocalDate.parse("2023-03-29"),
            antallTimerIUken = Duration.ofHours(25)
        ),
        selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
            harInntektSomSelvstendig = true,
            virksomhet = Virksomhet(
                næringstype = Næringstyper.ANNEN,
                fraOgMed = LocalDate.parse("2021-01-01"),
                tilOgMed = LocalDate.parse("2021-01-10"),
                navnPåVirksomheten = "Kjells Møbelsnekkeri",
                registrertINorge = true,
                yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeÅrene(LocalDate.parse("2021-01-01")),
                organisasjonsnummer = "111111"
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
                        type = ArbeidIPeriodeType.ARBEIDER_VANLIG
                    )
                )
            )
        ),
        harVærtEllerErVernepliktig = true,
        k9FormatSøknad = defaultK9FormatPSB(søknadId, mottatt),
        omsorgstilbud = null,
        barnRelasjon = null,
        barnRelasjonBeskrivelse = null,
        utenlandskNæring = listOf(
            UtenlandskNæring(
                næringstype = Næringstyper.DAGMAMMA,
                navnPåVirksomheten = "Dagmamma AS",
                land = Land(landkode = "NDL", landnavn = "Nederland"),
                fraOgMed = LocalDate.parse("2020-01-01")
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
        )
    )
}
