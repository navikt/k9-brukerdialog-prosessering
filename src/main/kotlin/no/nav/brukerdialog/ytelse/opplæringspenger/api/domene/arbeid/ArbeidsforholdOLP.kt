package no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid

import jakarta.validation.Valid
import no.nav.brukerdialog.ytelse.fellesdomene.ArbeidUtils.arbeidstidInfoMedNullTimer
import no.nav.brukerdialog.ytelse.fellesdomene.ArbeidUtils.tilDuration
import no.nav.brukerdialog.ytelse.fellesdomene.ArbeidUtils.tilTimerPerDag
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo

data class ArbeidsforholdOLP(
    val jobberNormaltTimer: Double,
    @field:Valid val arbeidIPeriode: ArbeidIPeriode
) {
    companion object{
        internal fun ArbeidsforholdOLP?.somK9ArbeidstidInfo(perioder: List<Periode>): ArbeidstidInfo {
            if(this == null) return arbeidstidInfoMedNullTimer(perioder)
            return arbeidIPeriode.somK9ArbeidstidInfo(jobberNormaltTimer.tilTimerPerDag().tilDuration())
        }
    }
}
