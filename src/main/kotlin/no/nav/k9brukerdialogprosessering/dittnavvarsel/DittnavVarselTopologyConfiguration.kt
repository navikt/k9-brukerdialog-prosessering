package no.nav.k9brukerdialogprosessering.dittnavvarsel

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.k9brukerdialogprosessering.kafka.config.SerDes
import no.nav.k9brukerdialogprosessering.kafka.config.Topic
import no.nav.k9brukerdialogprosessering.kafka.types.TopicEntry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DittnavVarselTopologyConfiguration(
    private val objectMapper: ObjectMapper
) {
    companion object {
        const val K9_DITTNAV_VARSEL = "dusseldorf.privat-k9-dittnav-varsel-beskjed"
    }

    @Bean
    fun k9DittnavVarselTopic(): Topic<TopicEntry<K9Beskjed>> {
        return Topic(
            name = K9_DITTNAV_VARSEL,
            serDes = K9DittNavVarselSerdes(objectMapper)
        )
    }
}

class K9DittNavVarselSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<K9Beskjed>>(objectMapper) {
    override fun serialize(topic: String, entry: TopicEntry<K9Beskjed>): ByteArray{
        return objectMapper.writeValueAsBytes(entry)
    }

    override fun deserialize(topic: String?, data: ByteArray?): TopicEntry<K9Beskjed> {
        TODO("Not yet implemented")
    }
}
