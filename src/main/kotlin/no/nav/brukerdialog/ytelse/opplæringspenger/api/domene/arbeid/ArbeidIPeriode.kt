package no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import java.time.Duration
import java.time.LocalDate

enum class JobberIPeriodeSvar { SOM_VANLIG, REDUSERT, HELT_FRAVÆR }

data class ArbeidIPeriode @JsonCreator constructor(
    @JsonProperty("jobberIPerioden") private val jobberIPerioden: JobberIPeriodeSvar,
    @JsonProperty("enkeltdager") @field:Valid private val enkeltdager: List<Enkeltdag> = emptyList(),
    @JsonProperty("enkeltdagerFravær") @field:Valid private val enkeltdagerFravær: List<Enkeltdag> = emptyList(),
) {
    init {
        require(enkeltdager.isNotEmpty() || enkeltdagerFravær.isNotEmpty()) {
            "Enten enkeltdager eller enkeltdagerFravær må være satt"
        }
    }

    internal fun somK9ArbeidstidInfo(normaltimerPerDag: Duration): ArbeidstidInfo {
        val arbeidsdager = when {
            // Hvis enkeltdagerFravær er satt, konverter fravær til arbeidstimer
            enkeltdagerFravær.isNotEmpty() -> {
                enkeltdagerFravær.map { fraværsdag ->
                    val arbeidstimer = normaltimerPerDag.minus(fraværsdag.tid).coerceAtLeast(Duration.ZERO)
                    Enkeltdag(
                        dato = fraværsdag.dato,
                        tid = Duration.ofMinutes(arbeidstimer)
                    )
                }
            }
            // Ellers bruk enkeltdager direkte
            else -> enkeltdager
        }

        return ArbeidstidInfo().apply {
            leggTilPerioderFraEnkeltdager(normaltimerPerDag, arbeidsdager)
        }
    }
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
