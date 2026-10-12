package no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.inntektsrapportering

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.Valid
import org.hibernate.validator.constraints.UUID
import java.time.ZoneOffset
import java.time.ZonedDateTime

data class AktivitetspengerInntektsrapportering(
    @field:UUID(message = "Forventet gyldig UUID, men var '\${validatedValue}'")
    val oppgaveReferanse: String,

    @JsonIgnore
    val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),

    @field:Valid val oppgittInntekt: OppgittInntekt
)
