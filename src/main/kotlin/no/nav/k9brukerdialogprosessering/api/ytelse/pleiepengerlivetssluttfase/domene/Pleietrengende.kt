package no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase.domene

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import java.time.LocalDate
import no.nav.k9.søknad.ytelse.pls.v1.Pleietrengende as K9Pleietrengende

class Pleietrengende(
    @field:Size(max = 11)
    @field:Pattern(regexp = "^\\d+$", message = "'\${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private val norskIdentitetsnummer: String? = null,

    @field:PastOrPresent(message = "Kan ikke være i fremtiden")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private val fødselsdato: LocalDate? = null,

    @field:NotBlank(message = "Kan ikke være tomt eller blankt") private val navn: String,

    private val årsakManglerIdentitetsnummer: ÅrsakManglerIdentitetsnummer? = null,
) {
    internal fun somK9Pleietrengende(): K9Pleietrengende = when {
        norskIdentitetsnummer != null -> K9Pleietrengende().medNorskIdentitetsnummer(
            NorskIdentitetsnummer.of(
                norskIdentitetsnummer
            )
        )

        fødselsdato != null -> K9Pleietrengende().medFødselsdato(fødselsdato)
        else -> K9Pleietrengende()
    }

    @AssertTrue(message = "'Fødselsdato' må være satt dersom 'norskIdentitetsnummer' er null")
    fun isFødselsdato(): Boolean {
        if (norskIdentitetsnummer == null) {
            return fødselsdato != null
        }
        return true
    }

    @AssertTrue(message = "'ÅrsakManglerIdentitetsnummer' må være satt dersom 'norskIdentitetsnummer' er null")
    fun isÅrsakManglerIdentitetsnummer(): Boolean {
        if (norskIdentitetsnummer == null) {
            return årsakManglerIdentitetsnummer != null
        }
        return true
    }
}

enum class ÅrsakManglerIdentitetsnummer { BOR_I_UTLANDET, ANNET }
