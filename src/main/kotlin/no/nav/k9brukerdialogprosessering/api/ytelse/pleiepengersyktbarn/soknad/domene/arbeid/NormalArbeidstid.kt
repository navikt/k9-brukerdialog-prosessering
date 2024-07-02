package no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.arbeid

import java.time.Duration

data class NormalArbeidstid(
    val timerPerUkeISnitt: Duration,
) {
    internal fun timerPerDagFraSnitt(): Duration = timerPerUkeISnitt.dividedBy(DAGER_PER_UKE)
}
