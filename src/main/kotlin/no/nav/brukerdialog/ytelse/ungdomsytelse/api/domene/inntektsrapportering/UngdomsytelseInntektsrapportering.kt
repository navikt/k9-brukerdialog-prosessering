package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.inntektsrapportering

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import org.hibernate.validator.constraints.UUID
import java.time.ZoneOffset
import java.time.ZonedDateTime

data class UngdomsytelseInntektsrapportering(
    @field:UUID(message = "Forventet gyldig UUID, men var '\${validatedValue}'")
    val oppgaveReferanse: String,

    @Schema(hidden = true)
    val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),

    @field:Valid val oppgittInntekt: OppgittInntekt,

    @field:AssertTrue(message = "Inntektsopplysningene må bekreftes for å kunne rapportere")
    val harBekreftetInntekt: Boolean
)
