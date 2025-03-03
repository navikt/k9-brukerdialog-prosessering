package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner

import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.pdf.SpørsmålOgSvar
import no.nav.brukerdialog.pdf.lagVerdiElement
import no.nav.brukerdialog.pdf.tilSpørsmålOgSvar

data class SamtykkeSpørsmålOgSvar(
    val harForståttRettigheterOgPlikter: SpørsmålOgSvar? = null,
    val harBekreftetOpplysninger: SpørsmålOgSvar? = null,
)

fun strukturerSamtykkeSeksjon(
    harForståttRettigheterOgPlikter: Boolean,
    harBekreftetOpplysninger: Boolean,
): VerdilisteElement {
    val samtykke = mapSamtykkeTilSpørsmålOgSvar(harForståttRettigheterOgPlikter, harBekreftetOpplysninger)
    return VerdilisteElement(
        label = "Samtykke fra deg",
        verdiliste =
            listOfNotNull(
                lagVerdiElement(samtykke.harForståttRettigheterOgPlikter),
                lagVerdiElement(samtykke.harBekreftetOpplysninger),
            ),
    )
}

fun mapSamtykkeTilSpørsmålOgSvar(
    harForståttRettigheterOgPlikter: Boolean,
    harBekreftetOpplysninger: Boolean,
): SamtykkeSpørsmålOgSvar =
    SamtykkeSpørsmålOgSvar(
        harForståttRettigheterOgPlikter =
            tilSpørsmålOgSvar(
                "Har du forstått dine rettigheter og plikter?",
                harForståttRettigheterOgPlikter,
            ),
        harBekreftetOpplysninger =
            tilSpørsmålOgSvar(
                "Har du bekreftet at opplysningene du har gitt er riktige?",
                harBekreftetOpplysninger,
            ),
    )
