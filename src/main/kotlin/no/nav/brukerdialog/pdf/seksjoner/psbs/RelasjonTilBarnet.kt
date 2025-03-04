package no.nav.brukerdialog.pdf.seksjoner.psbs

import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.BarnRelasjon
import no.nav.brukerdialog.pdf.SpørsmålOgSvar
import no.nav.brukerdialog.pdf.lagVerdiElement
import no.nav.brukerdialog.pdf.tilSpørsmålOgSvar
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.PdfTekster

data class RelasjonTilBarnetSpørsmålOgSvar(
    val relasjon: SpørsmålOgSvar? = null,
    val relasjonBeskrivelse: SpørsmålOgSvar? = null,
)

internal fun strukturerRelasjonTilBarnetSeksjon(
    søknadSvarBarnrelasjon: BarnRelasjon?,
    søknadSvarBeskrivelse: String?,
): VerdilisteElement? {
    val barnrelasjon = mapRelasjonTilBarnetTilSpørsmålOgSvar(søknadSvarBarnrelasjon, søknadSvarBeskrivelse)

    return barnrelasjon.relasjon?.let {
        VerdilisteElement(
            label = PdfTekster.getValue("barnrelasjon.relasjon"),
            verdiliste =
                listOfNotNull(
                    lagVerdiElement(barnrelasjon.relasjon),
                    lagVerdiElement(barnrelasjon.relasjonBeskrivelse),
                ),
        )
    }
}

fun mapRelasjonTilBarnetTilSpørsmålOgSvar(
    barnrelasjon: BarnRelasjon?,
    barnRelasjonBeskrivelse: String?,
): RelasjonTilBarnetSpørsmålOgSvar =
    RelasjonTilBarnetSpørsmålOgSvar(
        relasjon = barnrelasjon?.let { tilSpørsmålOgSvar("Hvilken relasjon har du til barnet?", it) },
        relasjonBeskrivelse =
            barnRelasjonBeskrivelse?.let {
                tilSpørsmålOgSvar("Din beskrivelse av relasjon og tilsynsrolle for barnet:", barnRelasjonBeskrivelse)
            },
    )
