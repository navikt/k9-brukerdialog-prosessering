package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.k9Format

import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import java.time.Duration
import java.time.LocalDate

/**
 * Slår sammen to ArbeidstidInfo over en felles totalperiode ved å summere
 * per-dag-verdiene, og deretter komprimere til optimale Periode-nøkler.
 *
 * @param førsteArbeidstidInfo Den første ArbeidstidInfo som skal slås sammen.
 * @param andreArbeidstidInfo Den andre ArbeidstidInfo som skal slås sammen.
 * @param totalPeriode Den totale perioden som dekker begge ArbeidstidInfoene.
 *
 * @return En ny ArbeidstidInfo som inneholder de sammenslåtte periodene.
 *
 * Eksempel:
 * |-------------------------------Søknadsperiode-------------------------------------------------|
 * |-----------------Første ArbeidstidInfo------------------|
 *                   |----------------Andre ArbeidstidInfo----------------|
 *
 *  ArbeidstidInfo1: 2025-04-07(mandag) til 2025-04-09(onsdag), normalt 8 timer, faktisk 2 timer
 *  ArbeidstidInfo2: 2025-04-08(tirsdag) til 2025-04-10(torsdag), normalt 4 timer, faktisk 4 timer
 *
 *  Resultat:
 *  |-------------------------------Søknadsperiode-------------------------------------------------|
 *  |---Mandag---|--------------------------Tirsdag-Torsdag---------------------------|---Fredag---|
 *  | Normalt: 4 |                         Normalt 8 (4+4)                            | Normalt 4  |
 *  | Faktisk: 2 |                         Faktisk 6 (2+4)                            | Faktisk 4  |
 *
 *  Første periode: mandag til mandag, normalt 4 timer, faktisk 2 timer
 *  Overlappende periode: tirsdag til torsdag, normalt 8 timer, faktisk 6 timer
 *  Siste periode: fredag til fredag, normalt 4 timer, faktisk 4 timer
 *
 */
class ArbeidstidInfoUtleder(
    val førsteArbeidstidInfo: ArbeidstidInfo,
    val andreArbeidstidInfo: ArbeidstidInfo? = null,
    val totalPeriode: Periode
) {

    fun utled(): ArbeidstidInfo {
        if (andreArbeidstidInfo == null) {
            return førsteArbeidstidInfo
        }
        val resultat = slåSammen()
        return resultat
    }

    private fun slåSammen(): ArbeidstidInfo {
        checkNotNull(andreArbeidstidInfo) { "andreArbeidstidInfo kan ikke være null for sammenslåing" }

        val førsteArbeidstidInfoDagkart = førsteArbeidstidInfo.tilDagkart()
        val andreArbeidstidInfoDagkart = andreArbeidstidInfo.tilDagkart()

        val datoerIPerioden = totalPeriode.dager().toList()
        val arbeidstidPeriodeListe: List<ArbeidstidPeriodeInfo> = datoerIPerioden.map { dag ->
            (førsteArbeidstidInfoDagkart[dag] ?: nullTimer()) + (andreArbeidstidInfoDagkart[dag] ?: nullTimer())
        }

        return arbeidstidPeriodeListe
            .mapIndexed { idx: Int, periodeInfo: ArbeidstidPeriodeInfo -> datoerIPerioden[idx] to periodeInfo }
            .tilPerioder()
            .fold(ArbeidstidInfo()) { acc: ArbeidstidInfo, (periode: Periode, info: ArbeidstidPeriodeInfo) ->
                acc.leggeTilPeriode(periode, info)
            }
    }

    fun Periode.dager(): Sequence<LocalDate> =
        generateSequence(fraOgMed) { d -> if (d < tilOgMed) d.plusDays(1) else null }

    // Flater ut ArbeidstidInfo.perioder til et Map<LocalDate, ArbeidstidPeriodeInfo>
    private fun ArbeidstidInfo.tilDagkart(): Map<LocalDate, ArbeidstidPeriodeInfo> =
        this.perioder.entries
            .flatMap { (periode: Periode, info: ArbeidstidPeriodeInfo) ->
                periode.dager().map { dato -> dato to info }
            }
            .toMap()

    private fun nullTimer() = ArbeidstidPeriodeInfo()
        .medFaktiskArbeidTimerPerDag(Duration.ZERO)
        .medJobberNormaltTimerPerDag(Duration.ZERO)

    private operator fun ArbeidstidPeriodeInfo.plus(other: ArbeidstidPeriodeInfo) =
        ArbeidstidPeriodeInfo()
            .medFaktiskArbeidTimerPerDag(this.faktiskArbeidTimerPerDag + other.faktiskArbeidTimerPerDag)
            .medJobberNormaltTimerPerDag(this.jobberNormaltTimerPerDag + other.jobberNormaltTimerPerDag)

    private fun <T> List<Pair<LocalDate, T>>.tilPerioder(): List<Pair<Periode, T>> {
        if (isEmpty()) return emptyList()

        val result = mutableListOf<Pair<Periode, T>>()
        // startdato og info fra første element
        var startDato = this[0].first
        var prevDato = startDato
        var prevInfo = this[0].second

        for ((dato: LocalDate, info: T) in drop(1)) {
            // bryt hvis ikke samme info elle ikke påfølgende dag
            if (info != prevInfo || dato != prevDato.plusDays(1)) {
                result += Periode(startDato, prevDato) to prevInfo
                startDato = dato
                prevInfo = info
            }
            prevDato = dato
        }
        // siste periode
        result += Periode(startDato, prevDato) to prevInfo

        return result
    }
}
