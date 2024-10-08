package no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate


data class Pleietrengende(
    val norskIdentitetsnummer: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd") val fødselsdato: LocalDate? = null,
    val årsakManglerIdentitetsnummer: ÅrsakManglerIdentitetsnummer? = null,
    val navn: String
)

enum class ÅrsakManglerIdentitetsnummer(val pdfTekst: String) {
    BOR_I_UTLANDET ("Pleietrengende bor i utlandet"),
    ANNET ("Annet")
}
