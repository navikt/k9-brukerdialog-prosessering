package no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.utils

import no.nav.helse.felles.*
import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene.PSBMottattSøknad
import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene.felles.ArbeidIPeriode
import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene.felles.ArbeidIPeriodeType
import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene.felles.ArbeiderIPeriodenSvar
import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene.felles.Arbeidsforhold
import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene.felles.Arbeidsgiver
import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene.felles.NormalArbeidstid
import no.nav.k9brukerdialogprosessering.utils.K9FormatUtils.defaultK9FormatPSB
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

internal object PSBSøknadUtils {

    internal fun defaultSøknad(søknadId: String, mottatt: ZonedDateTime) = PSBMottattSøknad(
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
            fornavn = "Ola"
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
            startdato = LocalDate.parse("2019-01-01"),
            jobberFortsattSomFrilans = true,
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
                        type = ArbeidIPeriodeType.ARBEIDER_VANLIG,
                        arbeiderIPerioden = ArbeiderIPeriodenSvar.SOM_VANLIG
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
