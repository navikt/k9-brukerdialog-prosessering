package no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.soknad

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.AssertTrue
import org.hibernate.validator.constraints.UUID
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime

data class Aktivitetspengersøknad(
    @field:UUID(message = "Forventet gyldig UUID, men var '\${validatedValue}'")
    val søknadId: String,
    val språk: String,
    @field:Schema(hidden = true)
    val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    val startdato: LocalDate,
    val søkerNorskIdent: String,
    val barnErRiktig: Boolean,
    @field:AssertTrue(message = "Opplysningene må bekreftes for å sende inn søknad")
    val harBekreftetOpplysninger: Boolean,
    @field:AssertTrue(message = "Må ha forstått rettigheter og plikter for å sende inn søknad")
    val harForståttRettigheterOgPlikter: Boolean,
    )
