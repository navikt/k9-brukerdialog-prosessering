package no.nav.k9brukerdialogprosessering.utils

import java.time.LocalDate

internal fun MutableList<String>.krever(resultat: Boolean?, feilmelding: String) {
    if (resultat != true) this.add(feilmelding)
}

internal fun MutableList<String>.kreverIkkeNull(verdi: Any?, feilmelding: String = "") {
    if (verdi == null) this.add(feilmelding)
}

internal fun LocalDate.erLikEllerEtter(tilOgMedDato: LocalDate) = this >= tilOgMedDato
internal fun LocalDate.erFørEllerLik(tilOgMedDato: LocalDate) = this <= tilOgMedDato
