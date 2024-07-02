package no.nav.k9brukerdialogprosessering.api.ytelse.ettersending.domene

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import java.time.LocalDate
import no.nav.k9.ettersendelse.Pleietrengende as K9Pleietrengende

data class Pleietrengende(
    @Size(max = 11)
    @Pattern(regexp = "^\\d+$", message = "'\${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    var norskIdentitetsnummer: String?,

    val aktørId: String? = null,
    val navn: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd") val fødselsdato: LocalDate? = null,
) {
    override fun toString(): String {
        return "Pleietrengende(norskIdentitetsnummer=***, aktørId=***, navn=***, fodselsdato=***"
    }

    fun manglerIdentitetsnummer(): Boolean = norskIdentitetsnummer.isNullOrEmpty()

    infix fun oppdaterFødselsnummer(fødselsnummer: String?) {
        this.norskIdentitetsnummer = fødselsnummer
    }

    fun tilK9Pleietrengende(): K9Pleietrengende = when {
        norskIdentitetsnummer != null -> K9Pleietrengende(NorskIdentitetsnummer.of(norskIdentitetsnummer))
        else -> error("Mangler identitetsnummer")
    }
}
