package no.nav.brukerdialog.pdf.seksjoner.ungdomsytelse

import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.pdf.SpørsmålOgSvar
import no.nav.brukerdialog.pdf.lagVerdiElement
import no.nav.brukerdialog.pdf.tilSpørsmålOgSvar
import java.time.LocalDate

data class DeltarIUngdomsprogramSpørsmålOgSvar(
    val fraOgMed: SpørsmålOgSvar? = null,
)

private fun mapDeltarIUngdomsprogramTilSpørsmålOgSvar(fraOgMed: LocalDate?): DeltarIUngdomsprogramSpørsmålOgSvar =
    DeltarIUngdomsprogramSpørsmålOgSvar(fraOgMed = tilSpørsmålOgSvar("Fra og med: ", fraOgMed))

fun strukturerDeltarIUngdomsprogramSeksjon(fraOgMed: LocalDate?): VerdilisteElement {
    val deltarIUngdomsprogram = mapDeltarIUngdomsprogramTilSpørsmålOgSvar(fraOgMed)

    return VerdilisteElement(
        label = "Deltar i ungdomsprogrammet:",
        verdiliste = listOfNotNull(lagVerdiElement(deltarIUngdomsprogram.fraOgMed)),
    )
}
