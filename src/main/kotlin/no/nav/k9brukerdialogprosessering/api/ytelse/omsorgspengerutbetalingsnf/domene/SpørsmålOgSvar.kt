package no.nav.k9brukerdialogprosessering.api.ytelse.omsorgspengerutbetalingsnf.domene

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class SpørsmålOgSvar(
    @field:NotBlank(message = "Kan ikke være tomt eller blankt")
    @field:Size(max = 1000, message = "Kan ikke være mer enn 1000 tegn")
    val spørsmål: String,

    @field:NotNull(message = "Kan ikke være null") val svar: Boolean?,
)
