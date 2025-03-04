package no.nav.brukerdialog.pdf.seksjoner.psbs

import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.pdf.SpørsmålOgSvar
import no.nav.brukerdialog.pdf.lagVerdiElement
import no.nav.brukerdialog.pdf.tilSpørsmålOgSvar

data class VernepliktSpørsmålOgSvar(
    val utøvdeVerneplikt: SpørsmålOgSvar? = null,
)

fun strukturerVernepliktSeksjon(søknadSvarVerneplikt: Boolean?): VerdilisteElement? {
    val verneplikt = mapVernepliktTilSpørsmålOgSvar(søknadSvarVerneplikt)
    return verneplikt?.let {
        VerdilisteElement(
            label = "Verneplikt",
            verdiliste =
                listOfNotNull(
                    lagVerdiElement(verneplikt.utøvdeVerneplikt),
                ),
        )
    }
}

fun mapVernepliktTilSpørsmålOgSvar(verneplikt: Boolean?): VernepliktSpørsmålOgSvar? =
    verneplikt?.let {
        VernepliktSpørsmålOgSvar(
            utøvdeVerneplikt = tilSpørsmålOgSvar("Utøvde du verneplikt på tidspunktene du søker pleiepenger fra?", verneplikt),
        )
    }
