package no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene.felles

import no.nav.helse.felles.Periode
import java.time.Duration

data class Arbeidsgiver(
    val navn: String? = null,
    val organisasjonsnummer: String,
    val erAnsatt: Boolean,
    val arbeidsforhold: Arbeidsforhold? = null,
    val sluttetFørSøknadsperiode: Boolean? = null
)

data class NormalArbeidstid (
    val timerPerUkeISnitt: Duration
)

data class Arbeidsforhold(
    val normalarbeidstid: NormalArbeidstid,
    val arbeidIPeriode: ArbeidIPeriode
)

data class ArbeidIPeriode(
    val type: ArbeidIPeriodeType,
    val arbeiderIPerioden: ArbeiderIPeriodenSvar,
    val prosentAvNormalt: Double? = null,
    val timerPerUke: Duration? = null,
    val arbeidsuker: List<ArbeidsUke>? = null
)

enum class ArbeiderIPeriodenSvar {
    SOM_VANLIG,
    REDUSERT,
    HELT_FRAVÆR;

    fun jobber() = this != HELT_FRAVÆR
}

data class ArbeidsUke(
    val periode: Periode,
    val timer: Duration? = null
)

enum class ArbeidIPeriodeType {
    ARBEIDER_IKKE,
    ARBEIDER_VANLIG,
    ARBEIDER_PROSENT_AV_NORMALT,
    ARBEIDER_TIMER_I_SNITT_PER_UKE,
    ARBEIDER_ULIKE_UKER_TIMER,
}
