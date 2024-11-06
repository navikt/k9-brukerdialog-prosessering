package no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid

import jakarta.validation.Valid
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.brukerdialog.ytelse.fellesdomene.ArbeidUtils.arbeidstidInfoMedNullTimer
import no.nav.brukerdialog.ytelse.fellesdomene.ArbeidUtils.tilDuration
import no.nav.brukerdialog.ytelse.fellesdomene.ArbeidUtils.tilTimerPerDag
import java.time.LocalDate

class Arbeidsforhold(
    val jobberNormaltTimer: Double,
    @field:Valid val arbeidIPeriode: ArbeidIPeriode
) {
    companion object{
        internal fun Arbeidsforhold?.somK9ArbeidstidInfo(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
            if(this == null) return arbeidstidInfoMedNullTimer(fraOgMed, tilOgMed)
            return arbeidIPeriode.somK9ArbeidstidInfo(jobberNormaltTimer.tilTimerPerDag().tilDuration())
        }
    }
}
