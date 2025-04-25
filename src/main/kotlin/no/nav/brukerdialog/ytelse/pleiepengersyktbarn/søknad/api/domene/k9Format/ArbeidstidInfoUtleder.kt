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

        val fra = totalPeriode.fraOgMed
        val til = totalPeriode.tilOgMed

        // Generer liste over alle datoer i perioden
        val alleDatoer = generateSequence(fra) { it.plusDays(1) }
            .takeWhile { !it.isAfter(til) }
            .toList()

        // For hver dato: slå sammen denne og annen ArbeidstidInfo
        val daglig: List<Pair<LocalDate, ArbeidstidPeriodeInfo>> = alleDatoer.map { dato ->
            val dennePeriodeInfo = finnPeriodeInfo(dato, førsteArbeidstidInfo)
            val annenPeriodeInfo = finnPeriodeInfo(dato, andreArbeidstidInfo)

            val sumFaktiskArbeidstid = dennePeriodeInfo.faktiskArbeidTimerPerDag.plus(annenPeriodeInfo.faktiskArbeidTimerPerDag)
            val sumNormaltArbeidstid = dennePeriodeInfo.jobberNormaltTimerPerDag.plus(annenPeriodeInfo.jobberNormaltTimerPerDag)

            dato to ArbeidstidPeriodeInfo()
                .medFaktiskArbeidTimerPerDag(sumFaktiskArbeidstid)
                .medJobberNormaltTimerPerDag(sumNormaltArbeidstid)
        }

        // Slå sammen påfølgende dager med samme ArbeidstidPeriodeInfo til én Periode
        val resultat = ArbeidstidInfo()
        if (daglig.isEmpty()) return resultat

        var startdato = daglig.first().first
        var forrigeDato = startdato
        var forrigeInfo = daglig.first().second

        for ((dato, info) in daglig.drop(1)) {
            val sammenhengende = info == forrigeInfo && dato == forrigeDato.plusDays(1)
            if (!sammenhengende) {
                resultat.leggeTilPeriode(Periode(startdato, forrigeDato), forrigeInfo)
                startdato = dato
                forrigeInfo = info
            }
            forrigeDato = dato
        }

        // siste periode
        resultat.leggeTilPeriode(Periode(startdato, forrigeDato), forrigeInfo)

        return resultat
    }

    /**
     * Finn periodeinfo for den aktuelle datoen, eller default ZERO
     */
    fun finnPeriodeInfo(dato: LocalDate, info: ArbeidstidInfo) =
        info.perioder.entries
            .firstOrNull { it.key.inneholder(Periode(dato, dato)) }
            ?.value
            ?: ArbeidstidPeriodeInfo()
                .medFaktiskArbeidTimerPerDag(Duration.ZERO)
                .medJobberNormaltTimerPerDag(Duration.ZERO)
}
