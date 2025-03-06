package no.nav.brukerdialog.ytelse.opplæringspenger.api.domene

import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import no.nav.k9.søknad.felles.type.Periode
import java.time.LocalDate
import no.nav.k9.søknad.ytelse.olp.v1.kurs.Kursholder as K9Kursholder
import no.nav.k9.søknad.ytelse.olp.v1.kurs.Kurs as K9Kurs
import no.nav.k9.søknad.ytelse.olp.v1.kurs.Reise as K9Reise

data class Kurs(
    @field:NotBlank(message = "Kan ikke være tom") val kursholder: String,
    @field:NotEmpty(message = "Kan ikke være tom liste") val kursperioder: List<Periode>,
    @field:Valid val reise: Reise
) {
    fun tilK9Format(): K9Kurs {
        return K9Kurs(
            K9Kursholder(kursholder, null),
            kursperioder,
            reise.tilK9Format()
        )
    }
}

data class Reise(
    @field:NotNull(message = "Kan ikke være null") val reiserUtenforKursdager: Boolean,
    val reisedager: List<LocalDate>? = null,
    val reisedagerBeskrivelse: String? = null
){
    @Hidden
    @AssertTrue(message = "Dersom 'reiserUtenforKursdager' er true, kan ikke 'reisedager' være tom liste")
    fun isReisedagerMedDager(): Boolean {
        if (reiserUtenforKursdager) {
            return !reisedager.isNullOrEmpty()
        }
        return true
    }

    @Hidden
    @AssertTrue(message = "Dersom 'reiserUtenforKursdager' er true, må man sende med beskrivelse")
    fun isReisedagerMedBeskrivelse(): Boolean {
        if (reiserUtenforKursdager) {
            return !reisedagerBeskrivelse.isNullOrEmpty()
        }
        return true
    }

    fun tilK9Format(): K9Reise {
        return K9Reise(
            reiserUtenforKursdager,
            reisedager,
            reisedagerBeskrivelse
        )
    }
}
