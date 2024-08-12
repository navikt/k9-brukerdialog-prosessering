package no.nav.brukerdialog.pleiepengerilivetssluttfase.api.domene

import no.nav.brukerdialog.pleiepengerilivetsslutttfase.api.domene.Enkeltdag
import java.time.Duration
import java.time.LocalDate

object PILSTestUtils {
    val INGEN_ARBEIDSDAG = Duration.ZERO
    val FULL_ARBEIDSDAG = Duration.ofHours(7).plusMinutes(30)
    val HALV_ARBEIDSDAG = Duration.ofHours(3)

    val mandag = LocalDate.parse("2022-08-01")
    val tirsdag = mandag.plusDays(1)
    val onsdag = tirsdag.plusDays(1)
    val torsdag = onsdag.plusDays(1)
    val fredag = torsdag.plusDays(1)

    val enkeltDagerMedJobbSomVanlig = listOf(
        Enkeltdag(mandag, FULL_ARBEIDSDAG),
        Enkeltdag(tirsdag, FULL_ARBEIDSDAG),
        Enkeltdag(onsdag, FULL_ARBEIDSDAG),
        Enkeltdag(torsdag, FULL_ARBEIDSDAG),
        Enkeltdag(fredag, FULL_ARBEIDSDAG),
    )

    val enkeltDagerMedFulltFravær = listOf(
        Enkeltdag(mandag, INGEN_ARBEIDSDAG),
        Enkeltdag(tirsdag, INGEN_ARBEIDSDAG),
        Enkeltdag(onsdag, INGEN_ARBEIDSDAG),
        Enkeltdag(torsdag, INGEN_ARBEIDSDAG),
        Enkeltdag(fredag, INGEN_ARBEIDSDAG),
    )

    val enkeltDagerMedRedusertArbeid = listOf(
        Enkeltdag(mandag, INGEN_ARBEIDSDAG),
        Enkeltdag(tirsdag, HALV_ARBEIDSDAG),
        Enkeltdag(onsdag, FULL_ARBEIDSDAG),
        Enkeltdag(torsdag, INGEN_ARBEIDSDAG),
        Enkeltdag(fredag, FULL_ARBEIDSDAG),
    )
}
