package no.nav.brukerdialog.dittnavvarsel

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.brukerdialog.kafka.config.SerDes
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.types.TopicEntry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DittnavVarselTopologyConfiguration(
    private val objectMapper: ObjectMapper
) {
    companion object {
        const val K9_DITTNAV_VARSEL_TOPIC = "dusseldorf.privat-k9-dittnav-varsel-beskjed"
    }

    @Bean
    fun k9DittnavVarselTopic(): Topic<TopicEntry<K9Beskjed>> {
        return Topic(
            name = K9_DITTNAV_VARSEL_TOPIC,
            serDes = K9DittNavVarselSerdes(objectMapper)
        )
    }
}

class K9DittNavVarselSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<K9Beskjed>>(objectMapper) {
    override fun serialize(topic: String, entry: TopicEntry<K9Beskjed>): ByteArray{
        return objectMapper.writeValueAsBytes(entry.data)
    }

    override fun deserialize(topic: String?, data: ByteArray?): TopicEntry<K9Beskjed> {
        TODO("Not yet implemented")
    }
}
