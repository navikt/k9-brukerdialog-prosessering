package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner

import no.nav.brukerdialog.common.Constants.DATE_FORMATTER
import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.pdf.SpørsmålOgSvar
import no.nav.brukerdialog.pdf.lagVerdiElement
import no.nav.brukerdialog.pdf.tilSpørsmålOgSvar
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.PdfTekster
import java.time.LocalDate

data class PerioderSpørsmålOgSvar(
    val hvilkePeriode: SpørsmålOgSvar?,
)

internal fun strukturerPerioderSeksjon(
    søknadSvarFraOgMed: LocalDate,
    søknadSvarTilOgMed: LocalDate,
): VerdilisteElement {
    val periode = mapPerioderTilSpørsmålOgSvar(søknadSvarFraOgMed, søknadSvarTilOgMed)
    return VerdilisteElement(
        label = PdfTekster.getValue("perioder.tittel"),
        verdiliste = (
            listOfNotNull(lagVerdiElement(periode.hvilkePeriode))
        ),
    )
}

fun mapPerioderTilSpørsmålOgSvar(
    fraOgMed: LocalDate,
    tilOgMed: LocalDate,
): PerioderSpørsmålOgSvar =
    PerioderSpørsmålOgSvar(
        hvilkePeriode =
            tilSpørsmålOgSvar(
                "Hvilke periode har du søkt om pleiepenger?",
                "${DATE_FORMATTER.format(fraOgMed)} - ${DATE_FORMATTER.format(tilOgMed)}",
            ),
    )
