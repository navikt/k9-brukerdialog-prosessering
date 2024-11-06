package no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid

import jakarta.validation.constraints.NotEmpty
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import java.time.Duration
import java.time.LocalDate

enum class JobberIPeriodeSvar { SOM_VANLIG, REDUSERT, HELT_FRAVÆR }

class ArbeidIPeriode(
    private val jobberIPerioden: JobberIPeriodeSvar,
    @field:NotEmpty(message = "Kan ikke være tom liste") private val enkeltdager: List<Enkeltdag>,
) {

    internal fun somK9ArbeidstidInfo(normaltimerPerDag: Duration) =
        ArbeidstidInfo().apply { leggTilPerioderFraEnkeltdager(normaltimerPerDag, enkeltdager) }
}

private fun ArbeidstidInfo.leggTilPerioderFraEnkeltdager(
    normaltimerPerDag: Duration,
    enkeltdager: List<Enkeltdag>,
) {

    enkeltdager.forEach { enkeltdag ->
        leggTilPeriode(
            fraOgMed = enkeltdag.dato,
            tilOgMed = enkeltdag.dato,
            normalTimerPerDag = normaltimerPerDag,
            faktiskTimerPerDag = enkeltdag.tid
        )
    }
}

private fun ArbeidstidInfo.leggTilPeriode(
    fraOgMed: LocalDate,
    tilOgMed: LocalDate,
    normalTimerPerDag: Duration,
    faktiskTimerPerDag: Duration,
) {
    leggeTilPeriode(
        Periode(fraOgMed, tilOgMed),
        ArbeidstidPeriodeInfo()
            .medFaktiskArbeidTimerPerDag(faktiskTimerPerDag)
            .medJobberNormaltTimerPerDag(normalTimerPerDag)
    )
}
