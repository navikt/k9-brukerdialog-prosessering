package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.tilgang

import java.time.LocalDate
import java.time.temporal.WeekFields

/**
 * Lukket datoperiode der både [fom] og [tom] er inkludert.
 * Alle sammenligninger i tilgangskontrollen gjøres på dagsnivå, derfor LocalDate.
 */
data class Datoperiode(val fom: LocalDate, val tom: LocalDate) {
    init {
        // Fast melding uten datoer: meldinger fra exceptions havner i responsen via ExceptionHandler.
        require(!tom.isBefore(fom)) { "Ugyldig datoperiode: tom er før fom." }
    }

    /** Overlapp på én felles dag regnes som overlapp. */
    fun overlapper(annen: Datoperiode): Boolean = !fom.isAfter(annen.tom) && !annen.fom.isAfter(tom)

    /**
     * Snittet av to perioder: senest fom og tidligst tom.
     * Returnerer null når snittet er tomt, det vil si når periodene ikke overlapper.
     */
    fun snitt(annen: Datoperiode): Datoperiode? {
        val fom = maxOf(fom, annen.fom)
        val tom = minOf(tom, annen.tom)
        return if (tom.isBefore(fom)) null else Datoperiode(fom, tom)
    }
}

/** Ansettelsesperiode fra AA-registeret. Både fom og tom kan mangle (åpen periode). */
data class Ansettelsesperiode(val fom: LocalDate?, val tom: LocalDate?) {
    /**
     * Fyller inn åpne ender med datoene fra [utfylling].
     *
     * Returnerer null hvis resultatet blir en ugyldig periode (tom før fom). Det skjer når bare den ene
     * enden er åpen og den andre ligger utenfor [utfylling], for eksempel fom = null og tom før utfylling.fom.
     * En slik periode kan ikke overlappe [utfylling], og kalleren kan derfor se bort fra den.
     */
    fun fyllÅpneEnder(utfylling: Datoperiode): Datoperiode? {
        val fom = fom ?: utfylling.fom
        val tom = tom ?: utfylling.tom
        return if (tom.isBefore(fom)) null else Datoperiode(fom, tom)
    }
}

/** Isouke: mandag–søndag, og uke 1 er uka med årets første torsdag. */
fun LocalDate.erSammeIsoukeSom(annen: LocalDate): Boolean =
    get(WeekFields.ISO.weekBasedYear()) == annen.get(WeekFields.ISO.weekBasedYear()) &&
        get(WeekFields.ISO.weekOfWeekBasedYear()) == annen.get(WeekFields.ISO.weekOfWeekBasedYear())
