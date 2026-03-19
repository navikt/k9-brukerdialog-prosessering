package no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.soknad

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

data class Aktivitetspengersøknad(
    @field:Schema(hidden = true)
    val søknadId: String = UUID.randomUUID().toString(),
    val forutgåendeMedlemskap: ForutgåendeMedlemskap,
    val språk: String,
    @field:Schema(hidden = true)
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
