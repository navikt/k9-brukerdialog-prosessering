package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.api.domene.arbeid

import no.nav.brukerdialog.api.ytelse.fellesdomene.Næringstype
import no.nav.brukerdialog.api.ytelse.fellesdomene.Regnskapsfører
import no.nav.brukerdialog.api.ytelse.fellesdomene.Virksomhet
import no.nav.brukerdialog.api.ytelse.fellesdomene.YrkesaktivSisteTreFerdigliknedeArene
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.brukerdialog.utils.SøknadUtils.Companion.metadata
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.api.domene.BarnDetaljer
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.api.domene.Frilans
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.api.domene.Medlemskap
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.api.domene.Periode
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.api.domene.PleiepengerSyktBarnSøknad
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.api.domene.SelvstendigNæringsdrivende
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.api.domene.Språk
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.api.domene.UtenlandsoppholdIPerioden
import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarn
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarnSøknadValidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.felles.type.Periode as K9Periode

class SelvstendigArbeidsforholdTest {

    companion object {
        private val syvOgEnHalvTime = Duration.ofHours(7).plusMinutes(30)
        val mandag = LocalDate.parse("2022-01-03")
        val tirsdag = mandag.plusDays(1)
        val onsdag = tirsdag.plusDays(1)
        val torsdag = onsdag.plusDays(1)
        val fredag = torsdag.plusDays(1)
        val arbeidsforholdMedNormaltidSomSnittPerUke = Arbeidsforhold(
            normalarbeidstid = NormalArbeidstid(
                timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
            ),
            arbeidIPeriode = ArbeidIPeriode(
                type = ArbeidIPeriodeType.ARBEIDER_VANLIG
            )
        )

        fun arbeidsforholdMedRedusertSnittPerUke(fraOgMed: LocalDate, tilOgMed: LocalDate) = Arbeidsforhold(
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
                                fraOgMed = fraOgMed,
                                tilOgMed = tilOgMed
                            ),
                            timer = Duration.ofHours(15)
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Selvstendig næringsdrivende jobber som normalt i hele søknadsperioden`() {
        val selvstendig = SelvstendigNæringsdrivende(
            harInntektSomSelvstendig = true,
            virksomhet = Virksomhet(
                næringstype = Næringstype.JORDBRUK_SKOGBRUK,
                fiskerErPåBladB = false,
                fraOgMed = LocalDate.parse("2021-02-07"),
                næringsinntekt = 1233123,
                navnPåVirksomheten = "TullOgTøys",
                registrertINorge = false,
                organisasjonsnummer = "101010",
                yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeArene(LocalDate.now()),
                regnskapsfører = Regnskapsfører(
                    navn = "Kjell",
                    telefon = "84554"
                ),
                harFlereAktiveVirksomheter = false,
                erNyoppstartet = true
            ),
            arbeidsforhold = Arbeidsforhold(
                normalarbeidstid = NormalArbeidstid(
                    timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
                ),
                arbeidIPeriode = ArbeidIPeriode(
                    type = ArbeidIPeriodeType.ARBEIDER_VANLIG
                )
            )
        )

        val k9ArbeidstidInfo = selvstendig.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(1, perioder.size)
        assertEquals(syvOgEnHalvTime, perioder[K9Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[K9Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Selvstendig næringsdrivende uten arbeidsforhold, forventer at hele søknadsperioden fylles med 0-0 timer`() {
        val selvstendig = SelvstendigNæringsdrivende(
            harInntektSomSelvstendig = false
        )
        val k9ArbeidstidInfo = selvstendig.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(1, perioder.size)
        assertEquals(NULL_TIMER, perioder[K9Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(NULL_TIMER, perioder[K9Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Selvstendig næringsdrivende som sluttet i søknadsperioden med normaltid oppgitt som snittPerUke`() {
        val selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
            harInntektSomSelvstendig = true,
            virksomhet = Virksomhet(
                fraOgMed = mandag,
                tilOgMed = torsdag,
                næringstype = Næringstype.JORDBRUK_SKOGBRUK,
                navnPåVirksomheten = "TullOgTøys",
                organisasjonsnummer = "101010",
                erNyoppstartet = true,
                registrertINorge = true
            ),
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )

        val k9ArbeidstidInfo = selvstendigNæringsdrivende.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[K9Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[K9Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Selvstendig næringsdrivende som sluttet første dag i søknadsperioden med normaltid oppgitt som snittPerUke`() {
        val selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
            harInntektSomSelvstendig = true,
            virksomhet = Virksomhet(
                fraOgMed = mandag,
                tilOgMed = mandag,
                næringstype = Næringstype.JORDBRUK_SKOGBRUK,
                navnPåVirksomheten = "TullOgTøys",
                organisasjonsnummer = "101010",
                erNyoppstartet = true,
                registrertINorge = true
            ),
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )

        val k9ArbeidstidInfo = selvstendigNæringsdrivende.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[K9Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[K9Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Selvstendig næringsdrivende som sluttet siste dag i søknadsperioden med normaltid oppgitt som snittPerUke`() {
        val selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
            harInntektSomSelvstendig = true,
            virksomhet = Virksomhet(
                fraOgMed = mandag,
                tilOgMed = fredag,
                næringstype = Næringstype.JORDBRUK_SKOGBRUK,
                navnPåVirksomheten = "TullOgTøys",
                organisasjonsnummer = "101010",
                erNyoppstartet = true,
                registrertINorge = true
            ),
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )

        val k9ArbeidstidInfo = selvstendigNæringsdrivende.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[K9Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[K9Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Selvstendig næringsdrivende som sluttet etter søknadsperioden med normaltid oppgitt som snittPerUke`() {
        val selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
            harInntektSomSelvstendig = true,
            virksomhet = Virksomhet(
                fraOgMed = mandag,
                tilOgMed = fredag,
                næringstype = Næringstype.JORDBRUK_SKOGBRUK,
                navnPåVirksomheten = "TullOgTøys",
                organisasjonsnummer = "101010",
                erNyoppstartet = true,
                registrertINorge = true
            ),
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )

        val k9ArbeidstidInfo = selvstendigNæringsdrivende.k9ArbeidstidInfo(mandag, torsdag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[K9Periode(mandag, torsdag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[K9Periode(mandag, torsdag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Selvstendig næringsdrivende som startet etter søknadsperioden startet med normaltid oppgitt som snittPerUke`() {
        val selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
            harInntektSomSelvstendig = true,
            virksomhet = Virksomhet(
                fraOgMed = onsdag,
                næringstype = Næringstype.JORDBRUK_SKOGBRUK,
                navnPåVirksomheten = "TullOgTøys",
                organisasjonsnummer = "101010",
                erNyoppstartet = true,
                registrertINorge = true
            ),
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )

        val k9ArbeidstidInfo = selvstendigNæringsdrivende.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[K9Periode(onsdag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[K9Periode(onsdag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Selvstendig næringsdrivende som startet og sluttet i søknadsperioden med normaltid oppgitt som snittPerUke`() {
        val selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
            harInntektSomSelvstendig = true,
            virksomhet = Virksomhet(
                fraOgMed = tirsdag,
                tilOgMed = torsdag,
                næringstype = Næringstype.JORDBRUK_SKOGBRUK,
                navnPåVirksomheten = "TullOgTøys",
                organisasjonsnummer = "101010",
                erNyoppstartet = true,
                registrertINorge = true
            ),
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )

        val k9ArbeidstidInfo = selvstendigNæringsdrivende.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[K9Periode(tirsdag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[K9Periode(tirsdag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `sn slutter i søknadsperidoen - k9Format validering`() {
        val psbSøknad = PleiepengerSyktBarnSøknad(
            fraOgMed = tirsdag,
            tilOgMed = torsdag,
            selvstendigNæringsdrivende = SelvstendigNæringsdrivende(
                harInntektSomSelvstendig = true,
                virksomhet = Virksomhet(
                    fraOgMed = mandag.minusYears(1),
                    tilOgMed = onsdag,
                    næringstype = Næringstype.JORDBRUK_SKOGBRUK,
                    navnPåVirksomheten = "TullOgTøys",
                    organisasjonsnummer = "825905162",
                    registrertINorge = true,
                    erNyoppstartet = true
                ),
                arbeidsforhold = arbeidsforholdMedRedusertSnittPerUke(tirsdag, onsdag)
            ),
            frilans = Frilans(harInntektSomFrilanser = false),
            mottatt = ZonedDateTime.now(),
            søknadId = UUID.randomUUID().toString(),
            språk = Språk.nb,
            barn = BarnDetaljer(
                fødselsnummer = "10910198357",
                fødselsdato = null,
                aktørId = "123456",
                navn = "Navn Navnesen",
            ),
            arbeidsgivere = listOf(),
            medlemskap = Medlemskap(
                harBoddIUtlandetSiste12Mnd = false,
                utenlandsoppholdSiste12Mnd = listOf(),
                skalBoIUtlandetNeste12Mnd = false,
                utenlandsoppholdNeste12Mnd = listOf()
            ),
            utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(
                skalOppholdeSegIUtlandetIPerioden = false,
                opphold = listOf()
            ),
            ferieuttakIPerioden = null,
            opptjeningIUtlandet = listOf(),
            utenlandskNæring = listOf(),
            harForståttRettigheterOgPlikter = true,
            harBekreftetOpplysninger = true,
            omsorgstilbud = null,
            newVersion = null
        )

        val psbK9FormatSøknad = psbSøknad.somK9Format(
            Søker(
                aktørId = "987654321",
                fødselsdato = LocalDate.parse("1987-01-01"),
                fødselsnummer = "29897497326",
                fornavn = "Ole",
                mellomnavn = null,
                etternavn = "Doffen"
            ), metadata
        ) as Søknad

        PleiepengerSyktBarnSøknadValidator().forsikreValidert(psbK9FormatSøknad)

        val arbeidstidInfo =
            psbK9FormatSøknad.getYtelse<PleiepengerSyktBarn>().arbeidstid.selvstendigNæringsdrivendeArbeidstidInfo.get()

        val perioder = arbeidstidInfo.perioder
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[K9Periode(tirsdag, onsdag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[K9Periode(tirsdag, onsdag)]!!.faktiskArbeidTimerPerDag)
    }
}
