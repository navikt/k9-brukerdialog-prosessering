package no.nav.k9brukerdialogprosessering.pleiepengersyktbarn

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import no.nav.k9brukerdialogprosessering.innsending.Preprosessert
import no.nav.k9brukerdialogprosessering.kafka.config.SerDes
import no.nav.k9brukerdialogprosessering.kafka.config.Topic
import no.nav.k9brukerdialogprosessering.kafka.types.Cleanup
import no.nav.k9brukerdialogprosessering.kafka.types.TopicEntry
import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene.PSBMottattSøknad
import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene.PSBPreprosessertSøknad
import org.apache.kafka.common.serialization.Serdes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@Configuration
class PSBTopologyConfiguration(
    private val objectMapper: ObjectMapper
) {
    companion object {
        const val PSB_MOTTATT_TOPIC = "dusseldorf.privat-pleiepengesoknad-mottatt-v2"
        const val PSB_PREPROSESSERT_TOPIC = "dusseldorf.privat-pleiepengesoknad-preprosessert"
        const val PSB_CLEANUP_TOPIC = "dusseldorf.privat-pleiepengesoknad-cleanup"
    }
    @Bean
    fun psbMottattTopic(): Topic<TopicEntry<PSBMottattSøknad>> {
        return Topic(
            name = PSB_MOTTATT_TOPIC,
            serDes = PSBMottattSøknadSerdes(objectMapper)
        )
    }
    @Bean
    fun psbPreprosessertTopic(): Topic<TopicEntry<PSBPreprosessertSøknad>> {
        return Topic(
            name = PSB_PREPROSESSERT_TOPIC,
            serDes = PSBMPreprosessertSøknadSerdes(objectMapper)
        )
    }
    @Bean
    fun psbCleanupTopic(): Topic<TopicEntry<Cleanup<PSBPreprosessertSøknad>>> {
        return Topic(
            name = PSB_CLEANUP_TOPIC,
            serDes = CleanupSøknadSerdes(objectMapper)
        )
    }
}

class PSBMottattSøknadSerdes(
    private val objectMapper: ObjectMapper
) : SerDes<TopicEntry<PSBMottattSøknad>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<PSBMottattSøknad> {
        return objectMapper.readValue(data, jacksonTypeRef<TopicEntry<PSBMottattSøknad>>())
    }
}

@Component
class PSBMPreprosessertSøknadSerdes(
    private val objectMapper: ObjectMapper
) : SerDes<TopicEntry<PSBPreprosessertSøknad>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<PSBPreprosessertSøknad> {
        return objectMapper.readValue(data, jacksonTypeRef())
    }
}

@Component
class CleanupSøknadSerdes<V: Preprosessert>(
    private val objectMapper: ObjectMapper
) : SerDes<TopicEntry<Cleanup<V>>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<Cleanup<V>> {
        return objectMapper.readValue(data, jacksonTypeRef<TopicEntry<Cleanup<V>>>())
    }
}
