package no.nav.brukerdialog.meldinger.omsorgspengeraleneomsorg.domene

import no.nav.brukerdialog.utils.DateUtils.NO_LOCALE
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class TypeBarn(val pdfTekst: String?){
    FOSTERBARN("(Fosterbarn)"),
    ANNET("(Annet)"),
    FRA_OPPSLAG(null)
}

data class Barn (
    val navn: String,
    val type: TypeBarn,
    var identitetsnummer: String,
    val tidspunktForAleneomsorg: TidspunktForAleneomsorg,
    val aktørId: String? = null,
    val dato: LocalDate? = null,
    val fødselsdato: LocalDate? = null
) {
    override fun toString(): String {
        return "Barn(navn='$navn', aktørId=*****, identitetsnummer=*****)"
    }
}

enum class TidspunktForAleneomsorg(val pdfUtskrift: String) {
    SISTE_2_ÅRENE(""),
    TIDLIGERE("Du ble alene om omsorgen for over 2 år siden.")
}

internal fun Barn.somMapTilPdf(): Map<String, Any?> {
    return mapOf<String, Any?>(
        "navn" to navn.capitalizeName(),
        "type" to type.pdfTekst,
        "fødselsdato" to fødselsdato,
        "identitetsnummer" to identitetsnummer,
        "tidspunktForAleneomsorgUtskrift" to tidspunktForAleneomsorg.pdfUtskrift,
        "dato" to if(dato!= null) DATE_TIME_FORMATTER.format(dato) else null
    )
}

private val ZONE_ID = ZoneId.of("Europe/Oslo")
private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZONE_ID)
fun String.capitalizeName(): String = split(" ").joinToString(" ") { it.lowercase(NO_LOCALE).capitalize() }
