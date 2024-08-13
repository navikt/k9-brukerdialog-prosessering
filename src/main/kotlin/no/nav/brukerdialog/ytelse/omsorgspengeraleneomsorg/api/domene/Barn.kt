package no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.api.domene

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.api.domene.TidspunktForAleneomsorg.SISTE_2_ÅRENE
import no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.api.domene.TidspunktForAleneomsorg.TIDLIGERE
import no.nav.brukerdialog.oppslag.barn.BarnOppslag
import java.time.LocalDate
import no.nav.k9.søknad.felles.personopplysninger.Barn as K9Barn

enum class TypeBarn {
    FRA_OPPSLAG,
    FOSTERBARN,
    ANNET
}

data class Barn(
    @field:Size(min = 11, max = 11)
    @field:Pattern(regexp = "^\\d+$", message = "'\${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    var identitetsnummer: String? = null,

    @field:NotBlank(message = "Kan ikke være tomt eller blankt")
    @field:Size(max = 100, message = "Kan ikke være mer enn 100 tegn")
    val navn: String,

    val type: TypeBarn,
    val aktørId: String? = null,
    val tidspunktForAleneomsorg: TidspunktForAleneomsorg,
    val dato: LocalDate? = null,

    @field:PastOrPresent(message = "Kan ikke være i fremtiden") val fødselsdato: LocalDate? = null,
) {
    internal fun manglerIdentifikator() = identitetsnummer.isNullOrBlank()
    internal fun somK9Barn() = K9Barn().medNorskIdentitetsnummer(NorskIdentitetsnummer.of(identitetsnummer))

    internal fun leggTilIdentifikatorHvisMangler(barnFraOppslag: List<BarnOppslag>) {
        if (manglerIdentifikator()) identitetsnummer =
            barnFraOppslag.find { it.aktørId == this.aktørId }?.identitetsnummer
    }

    internal fun k9PeriodeFraOgMed() = when (tidspunktForAleneomsorg) {
        SISTE_2_ÅRENE -> dato
        TIDLIGERE -> LocalDate.now().minusYears(1).startenAvÅret()
    }

    private fun LocalDate.startenAvÅret() = LocalDate.parse("${year}-01-01")


    @AssertTrue(message = "Må være satt når 'type' er annet enn 'FRA_OPPSLAG'")
    fun isFødselsdato(): Boolean {
        if (type != TypeBarn.FRA_OPPSLAG) {
            return fødselsdato != null
        }
        return true
    }

    @AssertTrue(message = "Må være satt når 'tidspunktForAleneomsorg' er 'SISTE_2_ÅRENE'")
    fun isDato(): Boolean {
        if (tidspunktForAleneomsorg == SISTE_2_ÅRENE) {
            return dato != null
        }
        return true
    }
}

enum class TidspunktForAleneomsorg {
    SISTE_2_ÅRENE,
    TIDLIGERE
}
