package no.nav.k9brukerdialogprosessering.pleiepengersyktbarn

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.k9brukerdialogprosessering.kafka.config.SerDes
import no.nav.k9brukerdialogprosessering.kafka.config.Topic
import no.nav.k9brukerdialogprosessering.kafka.types.Cleanup
import no.nav.k9brukerdialogprosessering.kafka.types.TopicEntry
import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene.PSBMottattSøknad
import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene.PSBPreprosessertSøknad
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PSBTopologyConfiguration(
    private val objectMapper: ObjectMapper,
) {
    companion object {
        const val PSB_MOTTATT_TOPIC = "dusseldorf.pp-sykt-barn-soknad-mottatt"
        const val PSB_PREPROSESSERT_TOPIC = "dusseldorf.pp-sykt-barn-soknad-preprosessert"
        const val PSB_CLEANUP_TOPIC = "dusseldorf.pp-sykt-barn-soknad-cleanup"
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
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<PSBMottattSøknad>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<PSBMottattSøknad> {
        return objectMapper.readValue(data)
    }
}

class PSBMPreprosessertSøknadSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<PSBPreprosessertSøknad>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<PSBPreprosessertSøknad> {
        return objectMapper.readValue(data)
    }
}

class CleanupSøknadSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<Cleanup<PSBPreprosessertSøknad>>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<Cleanup<PSBPreprosessertSøknad>> {
        return objectMapper.readValue(data)
    }
}
