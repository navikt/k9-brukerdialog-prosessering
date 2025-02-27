package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner

import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Barn
import no.nav.brukerdialog.pdf.SpørsmålOgSvar
import no.nav.brukerdialog.pdf.lagVerdiElement3
import no.nav.brukerdialog.pdf.tilSpørsmålOgSvar

data class VedleggSomSpørsmålOgSvar(
    val legeerklæring: SpørsmålOgSvar? = null,
    val fødselsAttest: SpørsmålOgSvar? = null,
)

fun strukturerVedleggSeksjon(
    vedlegg: List<String>,
    barn: Barn,
    fødselsattestVedleggId: List<String>?,
): VerdilisteElement {
    val vedleggISøknad = mapVedleggTilSpørsmålOgSvar(vedlegg, barn, fødselsattestVedleggId)

    return VerdilisteElement(
        label = "Vedlegg",
        verdiliste =
            listOfNotNull(
                lagVerdiElement3(vedleggISøknad.legeerklæring),
                lagVerdiElement3(vedleggISøknad.fødselsAttest),
            ),
    )
}

fun mapVedleggTilSpørsmålOgSvar(
    vedlegg: List<String>,
    barn: Barn,
    fødselsattestVedleggId: List<String>?,
): VedleggSomSpørsmålOgSvar =
    VedleggSomSpørsmålOgSvar(
        legeerklæring =
            vedlegg.takeIf { it.isEmpty() }?.let {
                tilSpørsmålOgSvar("Legeerklæring", "Ingen vedlegg er lastet opp")
            },
        fødselsAttest =
            barn.fødselsnummer?.let {
                tilSpørsmålOgSvar(
                    "Fødselsattest",
                    if (fødselsattestVedleggId.isNullOrEmpty()) {
                        "Har ikke lastet opp kopi av fødselattest til barnet."
                    } else {
                        "Har lastet opp kopi av fødselsattest til barnet."
                    },
                )
            },
    )
