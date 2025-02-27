package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner

import no.nav.brukerdialog.common.Constants.DATE_FORMATTER
import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.OpptjeningIUtlandet
import no.nav.brukerdialog.pdf.SpørsmålOgSvar
import no.nav.brukerdialog.pdf.lagVerdiElement3
import no.nav.brukerdialog.pdf.tilSpørsmålOgSvar

data class AndreLandSpørsmålOgSvar(
    val opptjeningIAnnetLand: SpørsmålOgSvar? = null,
)

data class OpptjeningIUtlandetSpørsmålOgSvar(
    val andreLand: List<AndreLandSpørsmålOgSvar>? = emptyList(),
    val ikkeOpptjeningIUtlandet: SpørsmålOgSvar? = null,
)

fun strukturerOpptjeningIUtlandetSeksjon(søknadSvarOpptjeningUtland: List<OpptjeningIUtlandet>): VerdilisteElement {
    val opptjeningUtland = mapOpptjeningIUtlandetTilSpørsmålOgSvar(søknadSvarOpptjeningUtland)
    return VerdilisteElement(
        label = "Jobbet i annet EØS-land",
        verdiliste =
            (opptjeningUtland.andreLand?.mapNotNull { lagVerdiElement3(it.opptjeningIAnnetLand) } ?: emptyList())
                .plus(
                    listOfNotNull(
                        lagVerdiElement3(opptjeningUtland.ikkeOpptjeningIUtlandet),
                    ),
                ),
    )
}

fun mapOpptjeningIUtlandetTilSpørsmålOgSvar(opptjeningIUtlandet: List<OpptjeningIUtlandet>): OpptjeningIUtlandetSpørsmålOgSvar =
    OpptjeningIUtlandetSpørsmålOgSvar(
        andreLand =
            opptjeningIUtlandet.takeIf { it.isNotEmpty() }?.let {
                opptjeningIUtlandet.map { opptjeningIUtland ->
                    AndreLandSpørsmålOgSvar(
                        opptjeningIAnnetLand =
                            tilSpørsmålOgSvar(
                                opptjeningIUtland.land.landnavn,
                                "Jobbet i ${opptjeningIUtland.land.landnavn} som ${opptjeningIUtland.opptjeningType.pdfTekst} hos " +
                                    "${opptjeningIUtland.navn}  ${DATE_FORMATTER.format(opptjeningIUtland.fraOgMed)} " +
                                    "- ${DATE_FORMATTER.format(opptjeningIUtland.tilOgMed)}",
                            ),
                    )
                }
            },
        ikkeOpptjeningIUtlandet =
            opptjeningIUtlandet.takeIf { it.isEmpty() }?.let {
                tilSpørsmålOgSvar(
                    "Har du jobbet i annet EØS land?",
                    "Nei",
                )
            },
    )
