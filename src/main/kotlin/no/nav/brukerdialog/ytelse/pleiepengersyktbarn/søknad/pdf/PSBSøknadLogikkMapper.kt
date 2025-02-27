package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf

import no.nav.brukerdialog.pdf.SpørsmålOgSvar
import no.nav.brukerdialog.pdf.somMapPerMnd
import no.nav.brukerdialog.pdf.tilSpørsmålOgSvar
import no.nav.helse.felles.Omsorgstilbud

data class MånedData(
    val noe: String? = null,
)

data class OmsorgstilbudSpørsmålOgSvar(
    val harVærtIOmsorgstilbud: SpørsmålOgSvar? = null,
    val skalVæreIOmsorgstilbud: SpørsmålOgSvar? = null,
    val erLiktHverUke: SpørsmålOgSvar? = null,
    val harIkkeOmsorgstilbud: SpørsmålOgSvar? = null,
    val tidIOmsorgstilbud: List<MånedData>? = emptyList(),
)

fun mapOmsorgstilbudTilSpørsmålOgSvarSeksjon(omsorgstilbud: Omsorgstilbud?): OmsorgstilbudSpørsmålOgSvar {
    omsorgstilbud?.let {
        return OmsorgstilbudSpørsmålOgSvar(
            harVærtIOmsorgstilbud = tilSpørsmålOgSvar("Har barnet vært fast og regelmessig i et omsorgstilbud?", omsorgstilbud.svarFortid),
            skalVæreIOmsorgstilbud =
                tilSpørsmålOgSvar(
                    "Skal barnet være fast og regelmessig i et omsorgstilbud?",
                    omsorgstilbud.svarFremtid,
                ),
            erLiktHverUke = tilSpørsmålOgSvar("Er tiden i omsorgstilbudet lik hver uke?", omsorgstilbud.erLiktHverUke),
            tidIOmsorgstilbud =
                omsorgstilbud.enkeltdager?.let {
                    it.somMapPerMnd().map { måned ->
                        MånedData()
                    }
                },
        )
    }
        ?: return OmsorgstilbudSpørsmålOgSvar(
            harIkkeOmsorgstilbud = tilSpørsmålOgSvar("Har barnet vært fast og regelmessig i et omsorgstilbud?", "Nei"),
        )
}
