package no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import no.nav.k9.søknad.felles.personopplysninger.Bosteder
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold
import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9brukerdialogprosessering.api.validering.landkode.ValidLandkode
import no.nav.k9brukerdialogprosessering.utils.erLikEllerEtter
import java.time.LocalDate

typealias Opphold = Bosted

class Bosted(
    @JsonFormat(pattern = "yyyy-MM-dd") private val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") private val tilOgMed: LocalDate,

    @field:NotBlank(message = "Kan ikke være tomt eller blankt")
    @field:ValidLandkode private val landkode: String, // TODO: 09/06/2022 Burde bruke klassen Land

    @field:NotBlank(message = "Kan ikke være tomt eller blankt")
    private val landnavn: String,

    @field:NotNull(message = "Kan ikke være null")
    private val erEØSLand: Boolean,
) {
    companion object {
        internal fun List<Bosted>.somK9Bosteder() = Bosteder().medPerioder(this.associate { it.somK9Bosted() })
        internal fun List<Opphold>.somK9Utenlandsopphold() =
            Utenlandsopphold().medPerioder(this.associate { it.somK9Utenlandsopphold() })
    }

    @AssertTrue(message = "'tilOgMed' må være lik eller etter 'fraOgMed'")
    fun isTilOgMed(): Boolean {
        return tilOgMed.erLikEllerEtter(fraOgMed)
    }

    internal fun somK9Bosted() =
        Pair(Periode(fraOgMed, tilOgMed), Bosteder.BostedPeriodeInfo().medLand(Landkode.of(landkode)))

    internal fun somK9Utenlandsopphold() =
        Pair(Periode(fraOgMed, tilOgMed), Utenlandsopphold.UtenlandsoppholdPeriodeInfo().medLand(Landkode.of(landkode)))
}
