package no.nav.brukerdialog.api.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.brukerdialog.api.validering.alder.ValidAlder
import no.nav.brukerdialog.oppslag.barn.BarnOppslag
import java.time.LocalDate
import no.nav.k9.søknad.felles.personopplysninger.Barn as K9Barn

class Barn(
    @field:Size(min = 11, max = 11)
    @field:Pattern(regexp = "^\\d+$", message = "'\${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private var identitetsnummer: String? = null,
    private val aktørId: String? = null,

    @field:PastOrPresent(message = "Kan ikke være i fremtiden")
    @field:ValidAlder(alder = 19, message = "Kan ikke være eldre enn 19 år")
    @JsonFormat(pattern = "yyyy-MM-dd") private val fødselsdato: LocalDate,

    @field:NotBlank(message = "Kan ikke være tomt eller blankt") private val navn: String,

    private val type: TypeBarn,
) {
    companion object {
        internal fun List<Barn>.somK9BarnListe() = kunFosterbarn().map { it.somK9Barn() }
        private fun List<Barn>.kunFosterbarn() = this.filter { it.type == TypeBarn.FOSTERBARN }
    }

    internal fun leggTilIdentifikatorHvisMangler(barnFraOppslag: List<BarnOppslag>) {
        if (identitetsnummer == null) identitetsnummer =
            barnFraOppslag.find { it.aktørId == this.aktørId }?.identitetsnummer
    }

    internal fun somK9Barn(): K9Barn {
        val barn = K9Barn()
        if (identitetsnummer != null) {
            barn.medNorskIdentitetsnummer(NorskIdentitetsnummer.of(identitetsnummer));
        } else {
            barn.medFødselsdato(fødselsdato)
        }
        return barn
    }

}

enum class TypeBarn {
    FRA_OPPSLAG,
    FOSTERBARN,
    ANNET
}

