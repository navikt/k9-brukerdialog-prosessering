package no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.api.domene

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.constraints.*
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import java.time.LocalDate
import no.nav.k9.søknad.ytelse.pls.v1.Pleietrengende as K9Pleietrengende

data class Pleietrengende(
    @field:Size(min = 11, max = 11)
    @field:Pattern(regexp = "^\\d+$", message = "'\${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    val norskIdentitetsnummer: String? = null,

    @field:PastOrPresent(message = "Kan ikke være i fremtiden")
    @JsonFormat(pattern = "yyyy-MM-dd")
    val fødselsdato: LocalDate? = null,

    @field:NotBlank(message = "Kan ikke være tomt eller blankt") val navn: String,

    @get:JsonProperty("årsakManglerIdentitetsnummer")
    val årsakManglerIdentitetsnummer: ÅrsakManglerIdentitetsnummer? = null,
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

    @Hidden
    @AssertTrue(message = "'Fødselsdato' må være satt dersom 'norskIdentitetsnummer' er null")
    fun isFødselsdato(): Boolean {
        if (norskIdentitetsnummer == null) {
            return fødselsdato != null
        }
        return true
    }

    @Hidden
    @AssertTrue(message = "'ÅrsakManglerIdentitetsnummer' må være satt dersom 'norskIdentitetsnummer' er null")
    fun isÅrsakManglerIdentitetsnummer(): Boolean {
        if (norskIdentitetsnummer == null) {
            return årsakManglerIdentitetsnummer != null
        }
        return true
    }
}

enum class ÅrsakManglerIdentitetsnummer { BOR_I_UTLANDET, ANNET }
