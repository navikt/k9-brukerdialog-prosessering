package no.nav.brukerdialog.pleiepengersyktbarn.api.domene.arbeid

import jakarta.validation.Valid
import jakarta.validation.constraints.AssertFalse
import no.nav.brukerdialog.pleiepengersyktbarn.api.domene.Periode
import no.nav.brukerdialog.pleiepengersyktbarn.api.domene.arbeid.RedusertArbeidstidType.PROSENT_AV_NORMALT
import no.nav.brukerdialog.pleiepengersyktbarn.api.domene.arbeid.RedusertArbeidstidType.TIMER_I_SNITT_PER_UKE
import no.nav.brukerdialog.pleiepengersyktbarn.api.domene.arbeid.RedusertArbeidstidType.ULIKE_UKER_TIMER
import java.time.Duration

data class ArbeidsRedusert(
    val type: no.nav.brukerdialog.pleiepengersyktbarn.api.domene.arbeid.RedusertArbeidstidType,
    val prosentAvNormalt: Double? = null,
    val timerPerUke: Duration? = null,
    val arbeidsuker: List<no.nav.brukerdialog.pleiepengersyktbarn.api.domene.arbeid.ArbeidsUke>? = null,
) {

    @AssertFalse(message = "Må være satt dersom type=PROSENT_AV_NORMALT")
    fun isProsentAvNormalt(): Boolean = type == PROSENT_AV_NORMALT && prosentAvNormalt == null

    @AssertFalse(message = "Må være satt dersom type=TIMER_I_SNITT_PER_UKE")
    fun isTimerPerUke(): Boolean = type == TIMER_I_SNITT_PER_UKE && timerPerUke == null

    @AssertFalse(message = "Må være satt dersom type=ULIKE_UKER_TIMER")
    fun isArbeidsuker(): Boolean = type == ULIKE_UKER_TIMER && arbeidsuker == null
}

data class ArbeidIPeriode(
    val type: no.nav.brukerdialog.pleiepengersyktbarn.api.domene.arbeid.ArbeidIPeriodeType,
    @field:Valid val redusertArbeid: no.nav.brukerdialog.pleiepengersyktbarn.api.domene.arbeid.ArbeidsRedusert? = null,
) {

    @AssertFalse(message = "Må være satt dersom type=ARBEIDER_REDUSERT")
    fun isArbeiderRedusert(): Boolean {
        return type == no.nav.brukerdialog.pleiepengersyktbarn.api.domene.arbeid.ArbeidIPeriodeType.ARBEIDER_REDUSERT && redusertArbeid == null
    }

    override fun equals(other: Any?) = this === other || other is no.nav.brukerdialog.pleiepengersyktbarn.api.domene.arbeid.ArbeidIPeriode && this.equals(other)
    private fun equals(other: no.nav.brukerdialog.pleiepengersyktbarn.api.domene.arbeid.ArbeidIPeriode) = this.type == other.type && this.redusertArbeid == other.redusertArbeid

}

data class ArbeidsUke(val periode: Periode, val timer: Duration)
