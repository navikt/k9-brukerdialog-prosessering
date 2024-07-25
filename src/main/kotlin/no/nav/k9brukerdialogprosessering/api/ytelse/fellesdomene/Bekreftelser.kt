package no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene

import jakarta.validation.constraints.AssertTrue

class Bekreftelser(
    @field:AssertTrue(message = "Opplysningene må bekreftes for å sende inn søknad")
    val harBekreftetOpplysninger: Boolean,

    @field:AssertTrue(message = "Må ha forstått rettigheter og plikter for å sende inn søknad")
    val harForståttRettigheterOgPlikter: Boolean,
)
