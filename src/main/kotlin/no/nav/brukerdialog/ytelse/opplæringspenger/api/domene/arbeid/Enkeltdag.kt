package no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid

import java.time.Duration
import java.time.LocalDate

data class Enkeltdag(
    val dato: LocalDate,
    val tid: Duration,
) {
    companion object {
        internal fun List<Enkeltdag>.finnTidForGittDato(dato: LocalDate) = this.find { it.dato == dato }?.tid
    }
}
