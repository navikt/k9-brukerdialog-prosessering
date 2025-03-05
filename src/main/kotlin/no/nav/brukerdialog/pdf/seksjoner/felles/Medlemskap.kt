package no.nav.brukerdialog.pdf.seksjoner.felles

import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Medlemskap
import no.nav.brukerdialog.pdf.SpørsmålOgSvar
import no.nav.brukerdialog.pdf.lagVerdiElement
import no.nav.brukerdialog.pdf.tilSpørsmålOgSvar

data class Utenlandsopphold(
    val opphold: SpørsmålOgSvar? = null,
)

data class MedlemskapSpørsmålOgSvar(
    val harBoddIUtlandetSiste12Måneder: SpørsmålOgSvar? = null,
    val utenlandsOppholdSiste12Måneder: List<Utenlandsopphold>? = emptyList(),
    val skalBoIUtlandetNeste12Måneder: SpørsmålOgSvar? = null,
    val utenlandsOppholdNeste12Måneder: List<Utenlandsopphold>? = emptyList(),
)

fun strukturerMedlemskapSeksjon(søknadSvarMedlemskap: Medlemskap): VerdilisteElement {
    val medlemskap = mapMedlemskapTilSpørsmålOgSvar(søknadSvarMedlemskap)

    return VerdilisteElement(
        label = "Medlemskap i folketrygden",
        verdiliste =
            listOfNotNull(
                VerdilisteElement(
                    label = "Har du bodd i utlandet de siste 12 månedene?",
                    verdiliste =
                        listOfNotNull(
                            lagVerdiElement(medlemskap.harBoddIUtlandetSiste12Måneder),
                        ).plus(medlemskap.utenlandsOppholdSiste12Måneder?.mapNotNull { lagVerdiElement(it.opphold) } ?: emptyList()),
                ),
                VerdilisteElement(
                    label = "Skal du bo i utlandet de neste 12 månedene?",
                    verdiliste =
                        listOfNotNull(
                            lagVerdiElement(medlemskap.skalBoIUtlandetNeste12Måneder),
                        ).plus(
                            medlemskap.utenlandsOppholdNeste12Måneder?.mapNotNull { lagVerdiElement(it.opphold) } ?: emptyList(),
                        ),
                ),
            ),
    )
}

private fun mapMedlemskapTilSpørsmålOgSvar(medlemskap: Medlemskap): MedlemskapSpørsmålOgSvar =
    MedlemskapSpørsmålOgSvar(
        harBoddIUtlandetSiste12Måneder =
            medlemskap.harBoddIUtlandetSiste12Mnd.takeIf { !it }?.let {
                tilSpørsmålOgSvar(
                    "Har du bodd i utlandet de siste 12 månedene?",
                    medlemskap.harBoddIUtlandetSiste12Mnd,
                )
            },
        utenlandsOppholdSiste12Måneder =
            medlemskap.utenlandsoppholdSiste12Mnd
                .takeIf { it.isNotEmpty() }
                ?.map { Utenlandsopphold(opphold = tilSpørsmålOgSvar(it.landnavn, "${it.fraOgMed} - ${it.tilOgMed}")) },
        skalBoIUtlandetNeste12Måneder =
            medlemskap.skalBoIUtlandetNeste12Mnd.takeIf { !it }?.let {
                tilSpørsmålOgSvar(
                    "Skal du bo i utlandet de neste 12 månedene?",
                    medlemskap.skalBoIUtlandetNeste12Mnd,
                )
            },
        utenlandsOppholdNeste12Måneder =
            medlemskap.utenlandsoppholdNeste12Mnd
                .takeIf { it.isNotEmpty() }
                ?.map { Utenlandsopphold(opphold = tilSpørsmålOgSvar(it.landnavn, "${it.fraOgMed} - ${it.tilOgMed}")) },
    )
