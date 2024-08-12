package no.nav.brukerdialog.pleiepengersyktbarn.api.domene.arbeid

import java.time.Duration

data class NormalArbeidstid(
    val timerPerUkeISnitt: Duration,
) {
    internal fun timerPerDagFraSnitt(): Duration = timerPerUkeISnitt.dividedBy(DAGER_PER_UKE)
}
