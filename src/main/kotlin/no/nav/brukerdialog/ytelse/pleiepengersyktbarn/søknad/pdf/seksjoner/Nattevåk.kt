package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner

import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Nattevåk
import no.nav.brukerdialog.pdf.SpørsmålOgSvar
import no.nav.brukerdialog.pdf.lagVerdiElement
import no.nav.brukerdialog.pdf.tilSpørsmålOgSvar

data class NattevåkSpørsmålOgSvar(
    val harNattevåk: SpørsmålOgSvar? = null,
    val nattevåkBeskrivelse: SpørsmålOgSvar? = null,
)

fun strukturerNattevåkSeksjon(søknadSvarNattevåk: Nattevåk?): VerdilisteElement? {
    val nattevåk = mapNattevåkTilSpørsmålOgSvar(søknadSvarNattevåk)
    return VerdilisteElement(
        label = "Nattevåk",
        verdiliste =
            listOfNotNull(
                lagVerdiElement(nattevåk?.harNattevåk),
                lagVerdiElement(nattevåk?.nattevåkBeskrivelse),
            ),
    )
}

fun mapNattevåkTilSpørsmålOgSvar(nattevåk: Nattevåk?): NattevåkSpørsmålOgSvar? =
    nattevåk?.let {
        NattevåkSpørsmålOgSvar(
            harNattevåk =
                tilSpørsmålOgSvar(
                    "Må du være våken om natten for å pleie barnet, og derfor må være borte fra jobb dagen etter?",
                    nattevåk.harNattevåk,
                ),
            nattevåkBeskrivelse = tilSpørsmålOgSvar("Dine tilleggsopplysninger:", nattevåk.tilleggsinformasjon),
        )
    }
