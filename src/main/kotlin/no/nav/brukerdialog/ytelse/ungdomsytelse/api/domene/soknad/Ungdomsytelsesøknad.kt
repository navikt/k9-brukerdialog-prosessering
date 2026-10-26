package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.soknad

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import org.hibernate.validator.constraints.UUID
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime

data class Ungdomsytelsesøknad(
    @field:UUID(message = "Forventet gyldig UUID, men var '\${validatedValue}'")
    val oppgaveReferanse: String,

    @field:UUID(message = "Forventet gyldig UUID, men var '\${validatedValue}'")
    val deltakelseId: String,

    val språk: String,

    @JsonIgnore
    val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),

    val startdato: LocalDate,
    val søkerNorskIdent: String,

    val barnErRiktig: Boolean,

    @field:Valid val kontonummerInfo: KontonummerInfo,

    @field:AssertTrue(message = "Opplysningene må bekreftes for å sende inn søknad")
    val harBekreftetOpplysninger: Boolean,
    @field:AssertTrue(message = "Må ha forstått rettigheter og plikter for å sende inn søknad")
    val harForståttRettigheterOgPlikter: Boolean,
    )
