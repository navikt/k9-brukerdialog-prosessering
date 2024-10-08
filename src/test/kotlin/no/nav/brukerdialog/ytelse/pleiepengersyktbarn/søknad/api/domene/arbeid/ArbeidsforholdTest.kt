package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.arbeid

import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.Periode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate
import no.nav.k9.søknad.felles.type.Periode as K9Periode

class ArbeidsforholdTest {

    companion object {
        val syvOgEnHalvTime = Duration.ofHours(7).plusMinutes(30)
        val femTimer = Duration.ofHours(5)
        val halvArbeidsdag = Duration.ofHours(3).plusMinutes(45)
        val mandag = LocalDate.parse("2022-01-03")
        val tirsdag = mandag.plusDays(1)
        val onsdag = tirsdag.plusDays(1)
        val torsdag = onsdag.plusDays(1)
        val fredag = torsdag.plusDays(1)
        val lørdag = fredag.plusDays(1)
        val søndag = lørdag.plusDays(1)
    }

    @Test
    fun `Skal gi valideringsfeil dersom arbeidIPeriode er feil`() {
        Validator.verifiserValideringsFeil(
            Arbeidsforhold(
                normalarbeidstid = NormalArbeidstid(
                    timerPerUkeISnitt = Duration.ofHours(32)
                ),
                arbeidIPeriode = ArbeidIPeriode(
                    type = ArbeidIPeriodeType.ARBEIDER_REDUSERT,
                    redusertArbeid = ArbeidsRedusert(
                        type = RedusertArbeidstidType.PROSENT_AV_NORMALT,
                        prosentAvNormalt = null
                    )
                )
            ), 1, "Må være satt dersom type=PROSENT_AV_NORMALT"
        )
    }

    @Test
    fun `Jobber som vanlig med normal arbeidstid oppgitt som snitt per uke`() {
        val arbeidsforhold = Arbeidsforhold(
            normalarbeidstid = NormalArbeidstid(
                timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
            ),
            arbeidIPeriode = ArbeidIPeriode(
                type = ArbeidIPeriodeType.ARBEIDER_VANLIG
            )
        )
        val k9Arbeid = arbeidsforhold.tilK9ArbeidstidInfo(mandag, fredag)
        val perioder = k9Arbeid.perioder
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[K9Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[K9Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Jobber ikke med normal arbeidstid oppgitt som snitt per uke`() {
        val arbeidsforhold = Arbeidsforhold(
            normalarbeidstid = NormalArbeidstid(
                timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
            ),
            arbeidIPeriode = ArbeidIPeriode(
                type = ArbeidIPeriodeType.ARBEIDER_IKKE
            )
        )

        val k9Arbeid = arbeidsforhold.tilK9ArbeidstidInfo(mandag, fredag)
        val perioder = k9Arbeid.perioder
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[K9Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(NULL_TIMER, perioder[K9Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Jobber prosent av normalt`() {
        val arbeidsforhold = Arbeidsforhold(
            normalarbeidstid = NormalArbeidstid(
                timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
            ),
            arbeidIPeriode = ArbeidIPeriode(
                type = ArbeidIPeriodeType.ARBEIDER_REDUSERT,
                redusertArbeid = ArbeidsRedusert(
                    type = RedusertArbeidstidType.PROSENT_AV_NORMALT,
                    prosentAvNormalt = 50.0
                )
            )
        )

        val k9Arbeid = arbeidsforhold.tilK9ArbeidstidInfo(mandag, fredag)
        val perioder = k9Arbeid.perioder
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[K9Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime.dividedBy(2), perioder[K9Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Jobber timer i snitt per uke`() {
        val arbeidsforhold = Arbeidsforhold(
            normalarbeidstid = NormalArbeidstid(
                timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
            ),
            arbeidIPeriode = ArbeidIPeriode(
                type = ArbeidIPeriodeType.ARBEIDER_REDUSERT,
                redusertArbeid = ArbeidsRedusert(
                    type = RedusertArbeidstidType.TIMER_I_SNITT_PER_UKE,
                    timerPerUke = Duration.ofHours(25)
                )
            )
        )

        val k9Arbeid = arbeidsforhold.tilK9ArbeidstidInfo(mandag, fredag)
        val perioder = k9Arbeid.perioder
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[K9Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(femTimer, perioder[K9Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Jobber ulike timer per uke`() {
        val arbeidsforhold = Arbeidsforhold(
            normalarbeidstid = NormalArbeidstid(
                timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
            ), arbeidIPeriode = ArbeidIPeriode(
                type = ArbeidIPeriodeType.ARBEIDER_REDUSERT,
                redusertArbeid = ArbeidsRedusert(
                    type = RedusertArbeidstidType.ULIKE_UKER_TIMER,
                    arbeidsuker = listOf(
                        ArbeidsUke(
                            periode = Periode(mandag, søndag),
                            timer = Duration.ofHours(18).plusMinutes(45), // 50% av normal arbeidstid.
                        )
                    )
                )
            )
        )

        val k9Arbeid = arbeidsforhold.tilK9ArbeidstidInfo(mandag, fredag)
        val perioder = k9Arbeid.perioder
        assertEquals(1, perioder.size)

        assertEquals(syvOgEnHalvTime, perioder[K9Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(halvArbeidsdag, perioder[K9Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }
}
