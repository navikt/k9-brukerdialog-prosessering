package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.k9Format

import no.nav.fpsak.tidsserie.LocalDateSegment
import no.nav.fpsak.tidsserie.LocalDateSegmentCombinator
import no.nav.fpsak.tidsserie.LocalDateTimeline
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import java.time.Duration

/**
 * Slår sammen to ArbeidstidInfo over en felles totalperiode ved å summere
 * per-dag-verdiene, og deretter komprimere til optimale Periode-nøkler.
 *
 * @param arbeidstidInfoer Liste med ArbeidstidInfoer som skal slås sammen.
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
 *  | Normalt: 8 |                         Normalt 12 (8+4)                           | Normalt 4  |
 *  | Faktisk: 2 |                         Faktisk 6 (2+4)                            | Faktisk 4  |
 *
 *  Første periode: mandag til mandag, normalt 4 timer, faktisk 2 timer
 *  Overlappende periode: tirsdag til torsdag, normalt 8 timer, faktisk 6 timer
 *  Siste periode: fredag til fredag, normalt 4 timer, faktisk 4 timer
 *
 */
class ArbeidstidInfoSammenslåer(
    private val arbeidstidInfoer: List<ArbeidstidInfo>,
    val totalPeriode: Periode,
) {

    fun slåSammen(): ArbeidstidInfo {
        if (arbeidstidInfoer.size == 1) {
            // bare én kilde → ingen sammenslåing
            return arbeidstidInfoer.first()
        }

        return arbeidstidInfoer
            .map { it.somLocalDateTimeLine() }
            .reduce { initiellPeriodeInfo: LocalDateTimeline<ArbeidstidPeriodeInfo>, nestePeriodeInfo: LocalDateTimeline<ArbeidstidPeriodeInfo> ->
                initiellPeriodeInfo.crossJoin(
                    nestePeriodeInfo,
                    arbeidstidPeriodeInfoKombinator()
                )
            }.tilArbeidstidInfo()
    }

    private fun arbeidstidPeriodeInfoKombinator() =
        LocalDateSegmentCombinator { interval, førsteArbeidstidPeriodeInfo, andreArbeidstidPeriodeInfo: LocalDateSegment<ArbeidstidPeriodeInfo>? ->
            // førsteArbeidstidPeriodeInfo eller andreArbeidstidPeriodeInfo kan være null hvis en side ikke har segment i dette intervallet.
            val venstreSide = førsteArbeidstidPeriodeInfo?.value ?: nullTimer()
            val høyreSide = andreArbeidstidPeriodeInfo?.value ?: nullTimer()

            // Summer arbeidstidPeriodeInfo for disse to segmentene og returner nytt segment for dette intervallet.
            LocalDateSegment(interval, venstreSide.plus(høyreSide))
        }

    private fun nullTimer() = ArbeidstidPeriodeInfo()
        .medFaktiskArbeidTimerPerDag(Duration.ZERO)
        .medJobberNormaltTimerPerDag(Duration.ZERO)

    private operator fun ArbeidstidPeriodeInfo.plus(other: ArbeidstidPeriodeInfo) =
        ArbeidstidPeriodeInfo()
            .medFaktiskArbeidTimerPerDag(this.faktiskArbeidTimerPerDag + other.faktiskArbeidTimerPerDag)
            .medJobberNormaltTimerPerDag(this.jobberNormaltTimerPerDag + other.jobberNormaltTimerPerDag)

    private fun ArbeidstidInfo.somLocalDateTimeLine(): LocalDateTimeline<ArbeidstidPeriodeInfo> = perioder.entries
        .map { (periode: Periode, info: ArbeidstidPeriodeInfo) ->
            LocalDateSegment(periode.fraOgMed, periode.tilOgMed, info)
        }
        .let { LocalDateTimeline(it) }

    private fun LocalDateTimeline<ArbeidstidPeriodeInfo>.tilArbeidstidInfo() = ArbeidstidInfo().apply {
        this@tilArbeidstidInfo.toSegments().forEach { seg ->
            val p = Periode(seg.fom, seg.tom)
            leggeTilPeriode(p, seg.value)
        }
    }
}
