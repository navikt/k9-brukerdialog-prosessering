package no.nav.brukerdialog.ytelse.opplæringspenger.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.micrometer.core.instrument.MeterRegistry
import no.nav.brukerdialog.kafka.config.KafkaProperties
import no.nav.brukerdialog.kafka.config.KafkaStreamsConfigUtils
import no.nav.brukerdialog.kafka.config.KafkaStreamsConfigUtils.configure
import no.nav.brukerdialog.kafka.config.SerDes
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.types.Cleanup
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.OLPMottattSøknad
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.OLPPreprosessertSøknad
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.KafkaStreamsConfiguration
import org.springframework.kafka.config.StreamsBuilderFactoryBean

@Configuration
class OLPTopologyConfiguration(
    private val objectMapper: ObjectMapper,
    private val meterRegistry: MeterRegistry,
    private val kafkaProperties: KafkaProperties,
) {
    companion object {
        const val OLP_PREPROSESSERING_STREAMS_BUILDER_NAME = "olpPreprosesseringStreamsBuilder"
        const val OLP_JOURNALFØRING_STREAMS_BUILDER_NAME = "olpJournalføringStreamsBuilder"
        const val OLP_CLEANUP_STREAMS_BUILDER_NAME = "olpCleanupStreamsBuilder"

        const val OLP_MOTTATT_TOPIC = "dusseldorf.olp-soknad-mottatt"
        const val OLP_PREPROSESSERT_TOPIC = "dusseldorf.olp-soknad-preprosessert"
        const val OLP_CLEANUP_TOPIC = "dusseldorf.olp-soknad-cleanup"
    }

    @Bean(name = [OLP_PREPROSESSERING_STREAMS_BUILDER_NAME])
    fun preprosesseringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.OPPLÆRINGSPENGER_SØKNAD_PREPROSESSERING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [OLP_JOURNALFØRING_STREAMS_BUILDER_NAME])
    fun journalføringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.OPPLÆRINGSPENGER_SØKNAD_JOURNALFØRING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [OLP_CLEANUP_STREAMS_BUILDER_NAME])
    fun cleanupStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.OPPLÆRINGSPENGER_SØKNAD_CLEANUP
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean
    fun olpMottattTopic(): Topic<TopicEntry<OLPMottattSøknad>> {
        return Topic(
            name = OLP_MOTTATT_TOPIC,
            serDes = OLPMottattSøknadSerdes(objectMapper)
        )
    }

    @Bean
    fun olpPreprosessertTopic(): Topic<TopicEntry<OLPPreprosessertSøknad>> {
        return Topic(
            name = OLP_PREPROSESSERT_TOPIC,
            serDes = OLPPreprosessertSøknadSerdes(objectMapper)
        )
    }

    @Bean
    fun olpCleanupTopic(): Topic<TopicEntry<Cleanup<OLPPreprosessertSøknad>>> {
        return Topic(
            name = OLP_CLEANUP_TOPIC,
            serDes = CleanupSøknadSerdes(objectMapper)
        )
    }
}

class OLPMottattSøknadSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<OLPMottattSøknad>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<OLPMottattSøknad> {
        return objectMapper.readValue(data)
    }
}

class OLPPreprosessertSøknadSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<OLPPreprosessertSøknad>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<OLPPreprosessertSøknad> {
        return objectMapper.readValue(data)
    }
}

class CleanupSøknadSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<Cleanup<OLPPreprosessertSøknad>>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<Cleanup<OLPPreprosessertSøknad>> {
        return objectMapper.readValue(data)
    }
}
