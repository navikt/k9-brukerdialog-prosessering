package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.arbeid.Arbeidsforhold
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.arbeid.Arbeidsforhold.Companion.k9ArbeidstidInfoMedNullTimer
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import java.time.LocalDate

data class Arbeidsgiver(
    @field:Size(max = 20)
    @field:Pattern(regexp = "^\\d+$", message = "'\${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    val organisasjonsnummer: String,

    @field:NotBlank(message = "navn kan ikke være tomt eller blankt")
    val navn: String,
    val erAnsatt: Boolean,
    val sluttetFørSøknadsperiode: Boolean? = null,

    @field:Valid val arbeidsforhold: Arbeidsforhold? = null,
) {
    fun k9ArbeidstidInfo(fraOgMed: LocalDate, tilOgMed: LocalDate): ArbeidstidInfo {
        return arbeidsforhold?.tilK9ArbeidstidInfo(fraOgMed, tilOgMed)
            ?: k9ArbeidstidInfoMedNullTimer(fraOgMed, tilOgMed)
    }
}
