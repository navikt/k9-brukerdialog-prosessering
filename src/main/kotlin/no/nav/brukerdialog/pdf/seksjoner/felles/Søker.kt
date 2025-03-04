package no.nav.brukerdialog.pdf.seksjoner.felles

import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Barn
import no.nav.brukerdialog.pdf.SpørsmålOgSvar
import no.nav.brukerdialog.pdf.lagVerdiElement
import no.nav.brukerdialog.pdf.tilSpørsmålOgSvar
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.PdfTekster

data class SøkerSpørsmålOgSvar(
    val navnSøker: SpørsmålOgSvar? = null,
    val fødselsnummerSøker: SpørsmålOgSvar? = null,
    val navnBarn: SpørsmålOgSvar? = null,
    val fødselsnummerBarn: SpørsmålOgSvar? = null,
)

private fun mapSøkerTilSpørsmålOgSvar(
    søker: Søker,
    barn: Barn? = null,
): SøkerSpørsmålOgSvar =
    SøkerSpørsmålOgSvar(
        navnSøker = tilSpørsmålOgSvar("Navn", søker.formatertNavn()),
        fødselsnummerSøker = tilSpørsmålOgSvar("Fødselsnummer", søker.fødselsnummer),
        navnBarn = tilSpørsmålOgSvar("Navn på barn", barn?.navn),
        fødselsnummerBarn = tilSpørsmålOgSvar("Fødselsnummer på barn", barn?.fødselsnummer),
    )

internal fun strukturerSøkerSeksjon(
    søknadSvarSøker: Søker,
    søknadSvarBarn: Barn? = null,
): VerdilisteElement {
    val søker = mapSøkerTilSpørsmålOgSvar(søknadSvarSøker, søknadSvarBarn)
    return VerdilisteElement(
        label = PdfTekster.getValue("søker.tittel"),
        verdiliste =
            listOfNotNull(
                lagVerdiElement(søker.navnSøker),
                lagVerdiElement(søker.fødselsnummerSøker),
                lagVerdiElement(søker.navnBarn),
                lagVerdiElement(søker.fødselsnummerBarn),
            ),
    )
}


