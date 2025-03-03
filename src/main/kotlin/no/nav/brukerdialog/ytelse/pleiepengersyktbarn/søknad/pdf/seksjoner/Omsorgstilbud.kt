package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner

import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.pdf.SpørsmålOgSvar
import no.nav.brukerdialog.pdf.lagVerdiElement
import no.nav.brukerdialog.pdf.lagVerdiElement3
import no.nav.brukerdialog.pdf.somMapPerMnd
import no.nav.brukerdialog.pdf.tilSpørsmålOgSvar
import no.nav.brukerdialog.utils.DurationUtils.somTekst
import no.nav.helse.felles.Omsorgstilbud

data class FastedagerIOmsorgstilbud(
    val mandag: SpørsmålOgSvar? = null,
    val tirsdag: SpørsmålOgSvar? = null,
    val onsdag: SpørsmålOgSvar? = null,
    val torsdag: SpørsmålOgSvar? = null,
    val fredag: SpørsmålOgSvar? = null,
)

data class Enkeltdag(
    val tidspunktIOmsorgstilbud: SpørsmålOgSvar? = null,
)

data class Uker(
    val ukeTekst: String? = null,
    val enkeltDager: List<Enkeltdag>? = emptyList(),
)

data class Tidsperiode(
    val månedOgÅr: String? = null,
    val uker: List<Uker>? = emptyList(),
)

data class OmsorgstilbudSpørsmålOgSvar(
    val harVærtIOmsorgstilbud: SpørsmålOgSvar? = null,
    val skalVæreIOmsorgstilbud: SpørsmålOgSvar? = null,
    val erLiktHverUke: SpørsmålOgSvar? = null,
    val harIkkeOmsorgstilbud: SpørsmålOgSvar? = null,
    val tidIOmsorgstilbud: List<Tidsperiode>? = emptyList(),
    val fasteDagerIOmsorgstilbud: FastedagerIOmsorgstilbud? = null,
)

fun strukturerOmsorgstilbudSeksjon(omsorgstilbud: Omsorgstilbud?): VerdilisteElement {
    val test = mapOmsorgstilbudTilSpørsmålOgSvarSeksjon(omsorgstilbud)
    return VerdilisteElement(
        label = "Omsorgstilbud",
        verdiliste =
            if (omsorgstilbud != null) {
                listOfNotNull(
                    lagVerdiElement3(test.harVærtIOmsorgstilbud),
                    lagVerdiElement3(test.skalVæreIOmsorgstilbud),
                    lagVerdiElement3(test.erLiktHverUke),
                ).plus(
                    VerdilisteElement(
                        label = "Tid barnet er i omsorgstilbud:",
                        verdiliste =
                            test.tidIOmsorgstilbud?.map { tidsperiode ->
                                VerdilisteElement(
                                    label = tidsperiode.månedOgÅr ?: "",
                                    visningsVariant = "TABELL",
                                    verdiliste =
                                        tidsperiode.uker?.map { uke ->
                                            VerdilisteElement(
                                                label = "${uke.ukeTekst} (${tidsperiode.månedOgÅr})",
                                                verdiliste =
                                                    uke.enkeltDager?.mapNotNull { dag ->
                                                        lagVerdiElement3(dag.tidspunktIOmsorgstilbud)
                                                    },
                                            )
                                        },
                                )
                            } ?: emptyList(),
                    ),
                ).plus(
                    VerdilisteElement(
                        label = "Faste dager barnet er i omsorgstilbud",
                        verdiliste =
                            test.fasteDagerIOmsorgstilbud?.let { uke ->
                                listOfNotNull(
                                    lagVerdiElement3(uke.mandag),
                                    lagVerdiElement3(uke.tirsdag),
                                    lagVerdiElement3(uke.onsdag),
                                    lagVerdiElement3(uke.torsdag),
                                    lagVerdiElement3(uke.fredag),
                                )
                            } ?: emptyList(),
                    ),
                )
            } else {
                listOfNotNull(
                    lagVerdiElement("Har barnet vært fast og regelmessig i et omsorgstilbud?", "Nei"),
                )
            },
    )
}

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
                        Tidsperiode(
                            månedOgÅr = "${måned.navnPåMåned} ${måned.år}",
                            uker =
                                måned.uker.map { uke ->
                                    Uker(
                                        ukeTekst = "Uke ${uke.uke}",
                                        enkeltDager =
                                            uke.dager.map { dag ->
                                                Enkeltdag(tidspunktIOmsorgstilbud = tilSpørsmålOgSvar(dag.dato, dag.tid))
                                            },
                                    )
                                },
                        )
                    }
                },
            fasteDagerIOmsorgstilbud =
                omsorgstilbud.ukedager?.let { ukedag ->
                    FastedagerIOmsorgstilbud(
                        mandag = tilSpørsmålOgSvar("Mandag", ukedag.mandag?.somTekst(true)),
                        tirsdag = tilSpørsmålOgSvar("Tirsdag", ukedag.tirsdag?.somTekst(true)),
                        onsdag = tilSpørsmålOgSvar("Onsdag", ukedag.onsdag?.somTekst(true)),
                        torsdag = tilSpørsmålOgSvar("Torsdag", ukedag.torsdag?.somTekst(true)),
                        fredag = tilSpørsmålOgSvar("Fredag", ukedag.fredag?.somTekst(true)),
                    )
                },
        )
    }
        ?: return OmsorgstilbudSpørsmålOgSvar(
            harIkkeOmsorgstilbud = tilSpørsmålOgSvar("Har barnet vært fast og regelmessig i et omsorgstilbud?", "Nei"),
        )
}
