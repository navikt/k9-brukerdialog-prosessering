package no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.felles

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import no.nav.k9.søknad.felles.type.Periode
import java.time.LocalDate
import java.util.*

data class Kurs(
    val kursholder: KursholderType,
    val kursperioder: List<Periode>,
    val reise: Reise
)

@JsonDeserialize(using = KursholderTypeDeserializer::class)
sealed class KursholderType {
    data class Kursholder(val uuid: UUID? = null, val navn: String) : KursholderType()
    data class KursholderNavn(val navn: String) : KursholderType()
}

data class Reise(
    val reiserUtenforKursdager: Boolean,
    val reisedager: List<LocalDate>? = null,
    val reisedagerBeskrivelse: String? = null
)

class KursholderTypeDeserializer : JsonDeserializer<KursholderType>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): KursholderType {
        val node: JsonNode = p.codec.readTree(p)
        return if (node.has("uuid")) {
            KursholderType.Kursholder(
                uuid = node.get("uuid")?.let { UUID.fromString(it.asText()) },
                navn = node.get("navn").asText()
            )
        } else {
            KursholderType.KursholderNavn(
                navn = node.get("navn").asText()
            )
        }
    }
}