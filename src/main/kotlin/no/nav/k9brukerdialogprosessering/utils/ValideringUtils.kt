package no.nav.k9brukerdialogprosessering.utils

import no.nav.helse.dusseldorf.common.Personidentifikator
import java.time.LocalDate

internal fun MutableList<String>.krever(resultat: Boolean?, feilmelding: String) {
    if (resultat != true) this.add(feilmelding)
}

internal fun MutableList<String>.kreverIkkeNull(verdi: Any?, feilmelding: String = "") {
    if (verdi == null) this.add(feilmelding)
}

internal fun MutableList<String>.validerIdentifikator(identifikator: String?, felt: String){
    if (identifikator.isNullOrBlank()) {
        add("$felt kan ikke være null eller blank.")
    } else {
        runCatching { Personidentifikator(identifikator) }
            .onFailure { add("$felt er ikke gyldig identifikator, '${identifikator.take(6)}*****'. ${it.message}") }
    }
}

internal fun LocalDate.erLikEllerEtter(tilOgMedDato: LocalDate) = this >= tilOgMedDato
internal fun LocalDate.erFørEllerLik(tilOgMedDato: LocalDate) = this <= tilOgMedDato
