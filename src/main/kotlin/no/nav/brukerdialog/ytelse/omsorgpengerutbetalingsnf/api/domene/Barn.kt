package no.nav.brukerdialog.ytelse.omsorgpengerutbetalingsnf.api.domene

import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingsnf.api.domene.TypeBarn.FOSTERBARN
import no.nav.brukerdialog.oppslag.barn.BarnOppslag
import java.time.LocalDate
import no.nav.k9.søknad.felles.personopplysninger.Barn as K9Barn

class Barn(
    @field:Size(min = 11, max = 11)
    @field:Pattern(regexp = "^\\d+$", message = "'\${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private var identitetsnummer: String? = null,

    @field:NotBlank(message = "Kan ikke være tomt eller blankt") private val navn: String,

    @field:PastOrPresent(message = "Kan ikke være i fremtiden")
    private val fødselsdato: LocalDate,

    private val type: TypeBarn,
    private val aktørId: String? = null,
) {
    companion object {
        internal fun List<Barn>.somK9BarnListe() = kunFosterbarn().map { it.somK9Barn() }
        fun List<Barn>.kunFosterbarn() = this.filter { it.type == FOSTERBARN }
    }

    @Hidden
    @AssertTrue(message = "Kan ikke være null når 'type' er annet enn 'FRA_OPPSLAG'")
    fun isIdentitetsnummer(): Boolean {
        if (type != TypeBarn.FRA_OPPSLAG) {
            return identitetsnummer != null
        }
        return true
    }

    internal fun leggTilIdentifikatorHvisMangler(barnFraOppslag: List<BarnOppslag>) {
        if (identitetsnummer == null) identitetsnummer =
            barnFraOppslag.find { it.aktørId == this.aktørId }?.identitetsnummer
    }

    internal fun somK9Barn() = K9Barn().medNorskIdentitetsnummer(NorskIdentitetsnummer.of(identitetsnummer))
}

enum class TypeBarn {
    FOSTERBARN,
    ANNET,
    FRA_OPPSLAG
}
