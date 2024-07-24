package no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.domene

import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.Frilans
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.FrilansType
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.ArbeidIPeriode
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.ArbeidIPeriodeType
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.ArbeidsRedusert
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.Arbeidsforhold
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.NULL_TIMER
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.NormalArbeidstid
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.arbeid.RedusertArbeidstidType
import no.nav.k9brukerdialogprosessering.utils.TestUtils.Validator
import no.nav.k9brukerdialogprosessering.utils.TestUtils.verifiserIngenValideringsFeil
import no.nav.k9brukerdialogprosessering.utils.TestUtils.verifiserValideringsFeil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate

class FrilansTest {

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
    }

    @Test
    fun `Frilans med valideringsfeil i arbeidsforhold`() {
        val startdato = LocalDate.parse("2020-01-01")
        Validator.verifiserValideringsFeil(
            Frilans(
                startdato = startdato,
                sluttdato = null,
                jobberFortsattSomFrilans = true,
                harInntektSomFrilanser = true,
                type = FrilansType.FRILANS,
                arbeidsforhold = Arbeidsforhold(
                    normalarbeidstid = NormalArbeidstid(
                        timerPerUkeISnitt = syvOgEnHalvTime
                    ),
                    arbeidIPeriode = ArbeidIPeriode(
                        type = ArbeidIPeriodeType.ARBEIDER_REDUSERT,
                        redusertArbeid = ArbeidsRedusert(
                            type = RedusertArbeidstidType.PROSENT_AV_NORMALT,
                            prosentAvNormalt = null
                        )
                    )
                )
            ),
            1, "Må være satt dersom type=PROSENT_AV_NORMALT"
        )
    }

    @Test
    fun `Frilans hvor sluttdato er før startdato skal gi valideringsfeil`() {
        val startdato = LocalDate.parse("2020-01-01")
        Frilans(
            startdato = startdato,
            sluttdato = LocalDate.parse("2019-01-01"),
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            type = FrilansType.FRILANS,
            arbeidsforhold = null
        )
            .valider("test", startdato)
            .verifiserValideringsFeil(1, listOf("test.sluttdato kan ikke være etter startdato"))
    }

    @Test
    fun `Frilans hvor sluttdato og startdato er lik skal ikke gi valideringsfeil`() {
        val startdato = LocalDate.parse("2020-01-01")
        Frilans(
            startdato = startdato,
            sluttdato = LocalDate.parse("2020-01-01"),
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            type = FrilansType.FRILANS,
            arbeidsforhold = null
        ).valider("test", startdato).verifiserIngenValideringsFeil()
    }

    @Test
    fun `Frilans hvor sluttdato er etter startdato skal ikke gi valideringsfeil`() {
        val startdato = LocalDate.parse("2020-01-01")
        Frilans(
            startdato = startdato,
            sluttdato = LocalDate.parse("2021-01-01"),
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            type = FrilansType.FRILANS,
            arbeidsforhold = null
        ).valider("test", startdato).verifiserIngenValideringsFeil()
    }

    @Test
    fun `Frilans hvor frilansyper inneholder FRILANS krever startdato og jobberFortsattSomFrilans`() {
        Frilans(
            startdato = null,
            jobberFortsattSomFrilans = null,
            sluttdato = LocalDate.parse("2029-01-01"),
            type = FrilansType.FRILANS,
            harInntektSomFrilanser = true,
            arbeidsforhold = null
        )
            .valider("test", LocalDate.parse("2020-01-01"))
            .verifiserValideringsFeil(
                2, listOf(
                    "test.startdato kan ikke være null dersom test.type er FRILANS",
                    "test.jobberFortsattSomFrilans kan ikke være null dersom test.type er FRILANS"
                )
            )
    }


    @Test
    fun `Frilans jobber som vanlig i hele søknadsperioden`() {
        val frilans = Frilans(
            startdato = LocalDate.parse("2020-01-01"),
            sluttdato = null,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )

        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(1, perioder.size)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Frilans uten arbeidsforhold, forventer at hele søknadsperioden fylles med 0-0 timer`() {
        val frilans = Frilans(
            harInntektSomFrilanser = false
        )

        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(1, perioder.size)
        assertEquals(NULL_TIMER, perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(NULL_TIMER, perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Frilans som sluttet i søknadsperioden med normaltid oppgitt som snittPerUke`() {
        val frilans = Frilans(
            startdato = mandag,
            sluttdato = torsdag,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )
        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(2, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, torsdag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, torsdag)]!!.faktiskArbeidTimerPerDag)

        assertEquals(NULL_TIMER, perioder[Periode(fredag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(NULL_TIMER, perioder[Periode(fredag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Frilans som sluttet første dag i søknadsperioden med normaltid oppgitt som snittPerUke`() {
        val frilans = Frilans(
            startdato = mandag,
            sluttdato = mandag,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )
        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(2, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, mandag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, mandag)]!!.faktiskArbeidTimerPerDag)

        assertEquals(NULL_TIMER, perioder[Periode(tirsdag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(NULL_TIMER, perioder[Periode(tirsdag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Frilans som sluttet siste dag i søknadsperioden med normaltid oppgitt som snittPerUke`() {
        val frilans = Frilans(
            startdato = mandag,
            sluttdato = fredag,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )
        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Frilans som sluttet etter søknadsperioden med normaltid oppgitt som snittPerUke`() {
        val frilans = Frilans(
            startdato = mandag,
            sluttdato = fredag,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )
        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, torsdag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, torsdag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, torsdag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Frilans som startet etter søknadsperioden startet med normaltid oppgitt som snittPerUke`() {
        val frilans = Frilans(
            startdato = onsdag,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )
        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(2, perioder.size)

        assertEquals(NULL_TIMER, perioder[Periode(mandag, tirsdag)]!!.jobberNormaltTimerPerDag)
        assertEquals(NULL_TIMER, perioder[Periode(mandag, tirsdag)]!!.faktiskArbeidTimerPerDag)

        assertEquals(syvOgEnHalvTime, perioder[Periode(onsdag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(onsdag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Frilans som startet og sluttet i søknadsperioden med normaltid oppgitt som snittPerUke`() {
        val frilans = Frilans(
            startdato = tirsdag,
            sluttdato = torsdag,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = arbeidsforholdMedNormaltidSomSnittPerUke
        )
        val k9ArbeidstidInfo = frilans.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(3, perioder.size)

        listOf(mandag, fredag).forEach { dag ->
            assertEquals(NULL_TIMER, perioder[Periode(dag, dag)]!!.jobberNormaltTimerPerDag)
            assertEquals(NULL_TIMER, perioder[Periode(dag, dag)]!!.faktiskArbeidTimerPerDag)
        }

        assertEquals(syvOgEnHalvTime, perioder[Periode(tirsdag, torsdag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(tirsdag, torsdag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Frilans med tom type lik null gir valideringsfeil`() {
        val startdato = LocalDate.now()
        Frilans(
            startdato = startdato,
            sluttdato = LocalDate.parse("2029-01-01"),
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = null,
            type = null
        )
            .valider("test", startdato)
            .verifiserValideringsFeil(
                1, listOf(
                    "test.type kan ikke være null dersom søker har inntekt som frilanser",
                )
            )
    }

    @Test
    fun `Frilans med startdato etter søknadsperiodens start oppgir å ha startet før opptjeningsperiode får validerignsfeil`() {
        val søknadsperiodeStartdato = LocalDate.parse("2023-08-31")
        val frilansStart = LocalDate.parse("2023-05-05")
        Frilans(
            startetFørSisteTreHeleMåneder = true,
            startdato = frilansStart,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = null,
            type = FrilansType.FRILANS
        )
            .valider("test", søknadsperiodeStartdato)
            .verifiserValideringsFeil(
                1, listOf(
                    "Når test.startetFørSisteTreHeleMåneder er true, må test.startdato (2023-05-05) må være 3 mnd før søknadsperioden (2023-05-01)"
                )
            )
    }

    @Test
    fun `Frilans med startdato før søknadsperiodens start oppgir å ha startet før opptjeningsperiode får ikke validerignsfeil`() {
        val søknadsperiodeStartdato = LocalDate.parse("2023-08-31")
        val frilansStart = LocalDate.parse("2023-04-30")
        Frilans(
            startetFørSisteTreHeleMåneder = true,
            startdato = frilansStart,
            jobberFortsattSomFrilans = true,
            harInntektSomFrilanser = true,
            arbeidsforhold = null,
            type = FrilansType.FRILANS
        )
            .valider("test", søknadsperiodeStartdato)
            .verifiserIngenValideringsFeil()
    }
}
