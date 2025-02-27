package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner

import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Barn
import no.nav.brukerdialog.pdf.SpørsmålOgSvar
import no.nav.brukerdialog.pdf.lagVerdiElement3
import no.nav.brukerdialog.pdf.tilSpørsmålOgSvar
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.PdfTekster

data class SøkerSpørsmålOgSvar(
    val navnSøker: SpørsmålOgSvar?,
    val fødselsnummerSøker: SpørsmålOgSvar?,
    val navnBarn: SpørsmålOgSvar?,
    val fødselsnummerBarn: SpørsmålOgSvar?,
)

internal fun strukturerSøkerSeksjon(
    søknadSvarSøker: Søker,
    søknadSvarBarn: Barn,
): VerdilisteElement {
    val søker = mapSøkerTilSpørsmålOgSvar(søknadSvarSøker, søknadSvarBarn)
    return VerdilisteElement(
        label = PdfTekster.getValue("søker.tittel"),
        verdiliste =
            listOfNotNull(
                lagVerdiElement3(søker.navnSøker),
                lagVerdiElement3(søker.fødselsnummerSøker),
                lagVerdiElement3(søker.navnBarn),
                lagVerdiElement3(søker.fødselsnummerBarn),
            ),
    )
}

fun mapSøkerTilSpørsmålOgSvar(
    søker: Søker,
    barn: Barn,
): SøkerSpørsmålOgSvar =
    SøkerSpørsmålOgSvar(
        navnSøker = tilSpørsmålOgSvar("Navn", søker.formatertNavn()),
        fødselsnummerSøker = tilSpørsmålOgSvar("Fødselsnummer", søker.fødselsnummer),
        navnBarn = tilSpørsmålOgSvar("Navn på barn", barn.navn),
        fødselsnummerBarn = tilSpørsmålOgSvar("Fødselsnummer på barn", barn.fødselsnummer),
    )
