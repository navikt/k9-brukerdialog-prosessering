package no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.domene

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class Pleietrengende(
    val norskIdentitetsnummer: String,
    val navn: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd") val fødselsdato: LocalDate? = null,
) {
    override fun toString(): String {
        return "Pleietrengende(norskIdentitetsnummer=***, navn=***, fodselsdato=***"
    }
}
