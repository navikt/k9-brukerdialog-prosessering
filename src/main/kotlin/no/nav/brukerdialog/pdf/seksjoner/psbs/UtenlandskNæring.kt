package no.nav.brukerdialog.pdf.seksjoner.psbs

import no.nav.brukerdialog.common.Constants.DATE_FORMATTER
import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.UtenlandskNæring
import no.nav.brukerdialog.pdf.SpørsmålOgSvar
import no.nav.brukerdialog.pdf.lagVerdiElement
import no.nav.brukerdialog.pdf.tilSpørsmålOgSvar

data class Næring(
    val næringNavn: String,
    val næringLand: SpørsmålOgSvar? = null,
    val næringOrganisasjonsnummer: SpørsmålOgSvar? = null,
    val næringType: SpørsmålOgSvar? = null,
)

data class UtenlandskNæringSpørsmålOgSvar(
    val næringer: List<Næring>? = emptyList(),
)

fun strukturerUtenlandskNæringSeksjon(søknadSvarUtenlandskNæring: List<UtenlandskNæring>): VerdilisteElement? {
    val utenlandskNæring = mapUtenlandskNæringTilSpørsmålOgSvar(søknadSvarUtenlandskNæring)
    return VerdilisteElement(
        label = "Utenlandsk næring",
        visningsVariant = "TABELL",
        verdiliste =
            utenlandskNæring.næringer?.map { næring ->
                VerdilisteElement(
                    label = næring.næringNavn,
                    verdiliste =
                        listOfNotNull(
                            lagVerdiElement(næring.næringLand),
                            lagVerdiElement(næring.næringOrganisasjonsnummer),
                            lagVerdiElement(næring.næringType),
                        ),
                )
            },
    )
}

private fun mapUtenlandskNæringTilSpørsmålOgSvar(utenlandskNæring: List<UtenlandskNæring>): UtenlandskNæringSpørsmålOgSvar =
    UtenlandskNæringSpørsmålOgSvar(
        utenlandskNæring.takeIf { it.isNotEmpty() }?.let {
            utenlandskNæring.map {
                Næring(
                    næringNavn = "${it.navnPåVirksomheten} (${
                        DATE_FORMATTER.format(
                            it.fraOgMed,
                        )} - ${it.tilOgMed?.let { DATE_FORMATTER.format(it) } ?: ""})}",
                    næringLand = tilSpørsmålOgSvar("Land:", "${it.land.landnavn} ${it.land.landkode}"),
                    næringOrganisasjonsnummer = tilSpørsmålOgSvar("Organisasjonsnummer:", it.organisasjonsnummer),
                    næringType = tilSpørsmålOgSvar("Næringstype:", it.næringstype.beskrivelse),
                )
            }
        },
    )
