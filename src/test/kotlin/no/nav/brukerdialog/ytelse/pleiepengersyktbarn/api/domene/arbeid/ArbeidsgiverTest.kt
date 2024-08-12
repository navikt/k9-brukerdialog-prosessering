package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.api.domene.arbeid

import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.api.domene.Arbeidsgiver
import no.nav.k9.søknad.felles.type.Periode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate

class ArbeidsgiverTest {

    companion object {
        private val syvOgEnHalvTime = Duration.ofHours(7).plusMinutes(30)
        val mandag = LocalDate.parse("2022-01-03")
        val tirsdag = mandag.plusDays(1)
        val onsdag = tirsdag.plusDays(1)
        val torsdag = onsdag.plusDays(1)
        val fredag = torsdag.plusDays(1)
    }

    @Test
    fun `Arbeidstaker med valideringsfeil i arbeidsforhold`() {
        Validator.verifiserValideringsFeil(
            Arbeidsgiver(
                navn = "Coop",
                organisasjonsnummer = "977155436",
                erAnsatt = true,
                arbeidsforhold = Arbeidsforhold(
                    normalarbeidstid = NormalArbeidstid(
                        timerPerUkeISnitt = syvOgEnHalvTime
                    ),
                    arbeidIPeriode = ArbeidIPeriode(
                        type = ArbeidIPeriodeType.ARBEIDER_REDUSERT,
                        redusertArbeid = ArbeidsRedusert(
                            type = RedusertArbeidstidType.ULIKE_UKER_TIMER,
                            arbeidsuker = null
                        )
                    )
                )
            ), 1, "Må være satt dersom type=ULIKE_UKER_TIMER"
        )
    }

    @Test
    fun `Arbeidstaker jobber som vanlig i hele søknadsperioden`() {
        val arbeidsgiver = Arbeidsgiver(
            navn = "Coop",
            organisasjonsnummer = "977155436",
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

        val k9ArbeidstidInfo = arbeidsgiver.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(1, perioder.size)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(syvOgEnHalvTime, perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Arbeidstaker uten arbeidsforhold, forventer at hele søknadsperioden fylles med 0-0 timer`() {
        val arbeidsgiver = Arbeidsgiver(
            navn = "Coop",
            organisasjonsnummer = "977155436",
            erAnsatt = false,
            arbeidsforhold = null
        )


        val k9ArbeidstidInfo = arbeidsgiver.k9ArbeidstidInfo(mandag, fredag)
        val perioder = k9ArbeidstidInfo.perioder
        assertEquals(1, perioder.size)
        assertEquals(NULL_TIMER, perioder[Periode(mandag, fredag)]!!.jobberNormaltTimerPerDag)
        assertEquals(NULL_TIMER, perioder[Periode(mandag, fredag)]!!.faktiskArbeidTimerPerDag)
    }

    @Test
    fun `Arbeidsgiver uten navn gir feil`() {
        Validator.verifiserValideringsFeil(
            Arbeidsgiver(
                navn = "",
                organisasjonsnummer = "977155436",
                erAnsatt = false,
                arbeidsforhold = null
            ), 1, "navn kan ikke være tomt eller blankt"
        )
    }

    @Test
    fun `Arbeidsgiver uten gyldig organisasjonsnummer gir feil`() {
        Validator.verifiserValideringsFeil(
            Arbeidsgiver(
                navn = "Kiwi AS",
                organisasjonsnummer = "IKKE GYLDIG",
                erAnsatt = false,
                arbeidsforhold = null
            ), 1, "'IKKE GYLDIG' matcher ikke tillatt pattern '^\\d+$'"
        )
    }
}
