package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner

import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Beredskap
import no.nav.brukerdialog.pdf.SpørsmålOgSvar
import no.nav.brukerdialog.pdf.lagVerdiElement
import no.nav.brukerdialog.pdf.tilSpørsmålOgSvar

data class BeredskapSpørsmålOgSvar(
    val iBeredskap: SpørsmålOgSvar? = null,
    val beredskapBeskrivelse: SpørsmålOgSvar? = null,
)

fun strukturerBeredskapSeksjon(søknadSvarBeredskap: Beredskap?): VerdilisteElement {
    val beredskap = mapBeredskapTilSpørsmålOgSvar(søknadSvarBeredskap)
    return VerdilisteElement(
        label = "Beredskap",
        verdiliste =
            listOfNotNull(
                lagVerdiElement(beredskap?.iBeredskap),
                lagVerdiElement(beredskap?.beredskapBeskrivelse),
            ),
    )
}

fun mapBeredskapTilSpørsmålOgSvar(beredskap: Beredskap?): BeredskapSpørsmålOgSvar? =
    beredskap?.let {
        BeredskapSpørsmålOgSvar(
            iBeredskap = tilSpørsmålOgSvar("Må du være i beredskap også når barnet er i et omsorgstilbud?", beredskap.beredskap),
            beredskapBeskrivelse = tilSpørsmålOgSvar("Dine tilleggsopplysninger:", beredskap.tilleggsinformasjon),
        )
    }
