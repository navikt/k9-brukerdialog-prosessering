package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner

import no.nav.brukerdialog.common.Constants.DATE_FORMATTER
import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.FerieuttakIPerioden
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.UtenlandsoppholdIPerioden
import no.nav.brukerdialog.pdf.SpørsmålOgSvar
import no.nav.brukerdialog.pdf.lagVerdiElement3
import no.nav.brukerdialog.pdf.tilSpørsmålOgSvar

data class Opphold(
    val land: String,
    val erSammenMedBarnet: SpørsmålOgSvar? = null,
    val erBarnetInnlagt: SpørsmålOgSvar? = null,
    val perioder: SpørsmålOgSvar? = null,
    val årsak: SpørsmålOgSvar? = null,
)

data class UtenlandsoppholdSpørsmålOgSvar(
    val utenlandsreiseIPeriode: SpørsmålOgSvar? = null,
    val utenlandsoppholdIPerioden: List<Opphold>? = emptyList(),
    val ferieuttakIPerioden: SpørsmålOgSvar? = null,
)

fun strukturerUtenlandsoppholdSeksjon(
    søknadSvarUtenlandsopphold: UtenlandsoppholdIPerioden,
    søknadsvarFerieuttakIperioden: FerieuttakIPerioden?,
): VerdilisteElement? {
    val utenlandsopphold = mapUtenlandsoppholdTilSpørsmålOgSvar(søknadSvarUtenlandsopphold, søknadsvarFerieuttakIperioden)
    return utenlandsopphold?.let {
        VerdilisteElement(
            label = "Perioder med utenlandsopphold og ferie",
            verdiliste =
                listOfNotNull(
                    lagVerdiElement3(utenlandsopphold.utenlandsreiseIPeriode),
                    utenlandsopphold.utenlandsoppholdIPerioden?.takeIf { it.isNotEmpty() }?.let {
                        VerdilisteElement(
                            label = "Utenlandsopphold",
                            visningsVariant = "TABELL",
                            verdiliste =
                                utenlandsopphold.utenlandsoppholdIPerioden.map { opphold ->
                                    VerdilisteElement(
                                        label = opphold.land,
                                        verdiliste =
                                            listOfNotNull(
                                                lagVerdiElement3(opphold.erSammenMedBarnet),
                                                lagVerdiElement3(opphold.erBarnetInnlagt),
                                                lagVerdiElement3(opphold.perioder),
                                                lagVerdiElement3(opphold.årsak),
                                            ),
                                    )
                                },
                        )
                    },
                    lagVerdiElement3(utenlandsopphold.ferieuttakIPerioden),
                ),
        )
    }
}

private fun mapUtenlandsoppholdTilSpørsmålOgSvar(
    utenlandsopphold: UtenlandsoppholdIPerioden,
    ferieuttakIPerioden: FerieuttakIPerioden?,
): UtenlandsoppholdSpørsmålOgSvar =
    UtenlandsoppholdSpørsmålOgSvar(
        utenlandsreiseIPeriode =
            tilSpørsmålOgSvar(
                "Skal du reise til utlandet i perioden du søker om pleiepenger",
                utenlandsopphold.skalOppholdeSegIUtlandetIPerioden,
            ),
        utenlandsoppholdIPerioden =
            utenlandsopphold.opphold.takeIf { it.isNotEmpty() }?.let {
                it.map {
                    Opphold(
                        land =
                            "${it.landnavn}${if (it.erUtenforEøs == true) " (utenfor EØS)" else ""}: " +
                                "${DATE_FORMATTER.format(it.fraOgMed)} - ${DATE_FORMATTER.format(it.tilOgMed)}",
                        erSammenMedBarnet = tilSpørsmålOgSvar("Er barnet sammen med deg?", it.erSammenMedBarnet),
                        erBarnetInnlagt = tilSpørsmålOgSvar("Er barnet innlagt?", it.erBarnetInnlagt),
                        perioder =
                            tilSpørsmålOgSvar(
                                "Perioder:",
                                it.perioderBarnetErInnlagt.joinToString(", ") {
                                    "${DATE_FORMATTER.format(it.fraOgMed)} - ${DATE_FORMATTER.format(it.tilOgMed)}"
                                },
                            ),
                        årsak = tilSpørsmålOgSvar("Årsak", it.årsak?.beskrivelse),
                    )
                }
            },
        ferieuttakIPerioden =
            ferieuttakIPerioden?.let {
                tilSpørsmålOgSvar(
                    "Skal du ha ferie i perioden du søker om pleiepenger?",
                    "Du opplyser at du skal ha ferie \n - ${ferieuttakIPerioden.ferieuttak.joinToString(
                        "\n - ",
                    ) {"${DATE_FORMATTER.format(it.fraOgMed)} - ${DATE_FORMATTER.format(it.tilOgMed)}" }}",
                )
            } ?: tilSpørsmålOgSvar("Skal du ha ferie i perioden du søker om pleiepenger?", "Nei"),
    )
