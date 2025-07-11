package no.nav.brukerdialog.ytelse.opplæringspenger.api.domene

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
import no.nav.brukerdialog.utils.erFørEllerLik
import no.nav.brukerdialog.validation.landkode.ValidLandkode
import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9.søknad.felles.type.Periode
import java.time.LocalDate
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold as K9Utenlandsopphold

data class Utenlandsopphold(
    @field:JsonFormat(pattern = "yyyy-MM-dd") val fraOgMed: LocalDate,
    @field:JsonFormat(pattern = "yyyy-MM-dd") val tilOgMed: LocalDate,

    @field:NotBlank
    @field:ValidLandkode
    val landkode: String,

    @field:NotBlank
    val landnavn: String,
) {

    @AssertTrue(message = "fraOgMed må være før eller lik tilOgMed.")
    fun isValid(): Boolean = fraOgMed.erFørEllerLik(tilOgMed)

    internal fun k9Periode() = Periode(fraOgMed, tilOgMed)
    internal fun tilK9UtenlandsoppholdPeriodeInfo() =
        K9Utenlandsopphold.UtenlandsoppholdPeriodeInfo().medLand(Landkode.of(landkode))
}
