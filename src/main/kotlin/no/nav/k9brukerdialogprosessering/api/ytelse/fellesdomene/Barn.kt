package no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9brukerdialogprosessering.oppslag.barn.BarnOppslag
import no.nav.k9brukerdialogprosessering.utils.krever
import java.time.LocalDate
import no.nav.k9.søknad.felles.personopplysninger.Barn as K9Barn

data class Barn(
    @field:Size(min = 11, max = 11)
    @field:Pattern(regexp = "^\\d+$", message = "'\${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    var norskIdentifikator: String? = null,

    @JsonFormat(pattern = "yyyy-MM-dd")
    @field:jakarta.validation.constraints.PastOrPresent(message = "Kan ikke være i fremtiden")
    val fødselsdato: LocalDate? = null,

    @field:NotBlank(message = "Kan ikke være tomt eller blankt") val navn: String,

    val aktørId: String? = null,
) {

    fun leggTilIdentifikatorHvisMangler(barnFraOppslag: List<BarnOppslag>) {
        if (manglerIdentifikator()) norskIdentifikator =
            barnFraOppslag.find { it.aktørId == this.aktørId }?.identitetsnummer
    }

    fun manglerIdentifikator(): Boolean = norskIdentifikator.isNullOrBlank()

    fun somK9Barn(): K9Barn = K9Barn().medNorskIdentitetsnummer(NorskIdentitetsnummer.of(norskIdentifikator))

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        krever(navn.isNotBlank(), "$felt.navn kan ikke være tomt eller blank.")
        krever(!norskIdentifikator.isNullOrBlank(), "$felt.norskIdentifikator kan ikke være null eller blank.")
    }

    override fun toString() = "Barn(aktoerId=${aktørId}, navn=${navn}, fodselsdato=${fødselsdato}"

    override fun equals(other: Any?) = this === other || (other is Barn && this.equals(other))
    private fun equals(other: Barn) =
        this.aktørId == other.aktørId && this.norskIdentifikator == other.norskIdentifikator
}
