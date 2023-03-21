package no.nav.k9brukerdialogprosessering.meldinger.ettersendelse

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.micrometer.core.instrument.MeterRegistry
import no.nav.k9brukerdialogprosessering.kafka.config.KafkaProperties
import no.nav.k9brukerdialogprosessering.kafka.config.KafkaStreamName
import no.nav.k9brukerdialogprosessering.kafka.config.KafkaStreamsConfigUtils
import no.nav.k9brukerdialogprosessering.kafka.config.KafkaStreamsConfigUtils.configure
import no.nav.k9brukerdialogprosessering.kafka.config.SerDes
import no.nav.k9brukerdialogprosessering.kafka.config.Topic
import no.nav.k9brukerdialogprosessering.kafka.types.Cleanup
import no.nav.k9brukerdialogprosessering.kafka.types.TopicEntry
import no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.domene.Ettersendelse
import no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.domene.PreprosessertEttersendelse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.KafkaStreamsConfiguration
import org.springframework.kafka.config.StreamsBuilderFactoryBean

@Configuration
class EttersendelseTopologyConfiguration(
    private val objectMapper: ObjectMapper,
    private val meterRegistry: MeterRegistry,
    private val kafkaProperties: KafkaProperties,
) {
    companion object {
        const val ETTERSENDELSE_PREPROSESSERING_STREAMS_BUILDER_NAME = "ettersendelsePreprosesseringStreamsBuilder"
        const val ETTERSENDELSE_JOURNALFØRING_STREAMS_BUILDER_NAME = "ettersendelseJournalføringStreamsBuilder"
        const val ETTERSENDELSE_CLEANUP_STREAMS_BUILDER_NAME = "ettersendelseCleanupStreamsBuilder"

        const val ETTERSENDELSE_MOTTATT_TOPIC = "dusseldorf.ettersendelse-mottatt"
        const val ETTERSENDELSE_PREPROSESSERT_TOPIC = "dusseldorf.ettersendelse-preprosessert"
        const val ETTERSENDELSE_CLEANUP_TOPIC = "dusseldorf.ettersendelse-cleanup"
    }

    @Bean(name = [ETTERSENDELSE_PREPROSESSERING_STREAMS_BUILDER_NAME])
    fun preprosesseringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.ETTERSENDELSE_PREPROSESSERING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [ETTERSENDELSE_JOURNALFØRING_STREAMS_BUILDER_NAME])
    fun journalføringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.ETTERSENDELSE_JOURNALFØRING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [ETTERSENDELSE_CLEANUP_STREAMS_BUILDER_NAME])
    fun cleanupStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.ETTERSENDELSE_CLEANUP
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean
    fun ettersendelseMottattTopic(): Topic<TopicEntry<Ettersendelse>> {
        return Topic(
            name = ETTERSENDELSE_MOTTATT_TOPIC,
            serDes = EttersendelseMottattSerdes(objectMapper)
        )
    }

    @Bean
    fun ettersendelsePreprosessertTopic(): Topic<TopicEntry<PreprosessertEttersendelse>> {
        return Topic(
            name = ETTERSENDELSE_PREPROSESSERT_TOPIC,
            serDes = EttersendelseMPreprosessertSerdes(objectMapper)
        )
    }

    @Bean
    fun ettersendelseCleanupTopic(): Topic<TopicEntry<Cleanup<PreprosessertEttersendelse>>> {
        return Topic(
            name = ETTERSENDELSE_CLEANUP_TOPIC,
            serDes = EttersendelseCleanupSerdes(objectMapper)
        )
    }
}

class EttersendelseMottattSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<Ettersendelse>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<Ettersendelse> {
        return objectMapper.readValue(data)
    }
}

class EttersendelseMPreprosessertSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<PreprosessertEttersendelse>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<PreprosessertEttersendelse> {
        return objectMapper.readValue(data)
    }
}

class EttersendelseCleanupSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<Cleanup<PreprosessertEttersendelse>>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<Cleanup<PreprosessertEttersendelse>> {
        return objectMapper.readValue(data)
    }
}
