package no.nav.brukerdialog.ytelse.ungdomsytelse.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.micrometer.core.instrument.MeterRegistry
import no.nav.brukerdialog.kafka.config.KafkaProperties
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.config.KafkaStreamsConfigUtils
import no.nav.brukerdialog.kafka.config.KafkaStreamsConfigUtils.configure
import no.nav.brukerdialog.kafka.config.SerDes
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.types.Cleanup
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.domene.UngdomsytelsesøknadMottatt
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.domene.UngdomsytelsesøknadPreprosessertSøknad
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.KafkaStreamsConfiguration
import org.springframework.kafka.config.StreamsBuilderFactoryBean

@Configuration
class UngdomsytelsesøknadTopologyConfiguration(
    private val objectMapper: ObjectMapper,
    private val meterRegistry: MeterRegistry,
    private val kafkaProperties: KafkaProperties,
) {
    companion object {
        const val UNGDOMSYTELSE_SØKNAD_PREPROSESSERING_STREAMS_BUILDER_NAME =
            "ungdomsytelseSøknadPreprosesseringStreamsBuilder"
        const val UNGDOMSYTELSE_SØKNAD_JOURNALFØRING_STREAMS_BUILDER_NAME =
            "ungdomsytelseSøknadJournalføringStreamsBuilder"
        const val UNGDOMSYTELSE_SØKNAD_CLEANUP_STREAMS_BUILDER_NAME = "ungdomsytelseSøknadCleanupStreamsBuilder"

        const val UNGDOMSYTELSE_SØKNAD_MOTTATT_TOPIC = "dusseldorf.ungdomsytelse-soknad-mottatt"
        const val UNGDOMSYTELSE_SØKNAD_PREPROSESSERT_TOPIC = "dusseldorf.ungdomsytelse-soknad-preprosessert"
        const val UNGDOMSYTELSE_SØKNAD_CLEANUP_TOPIC = "dusseldorf.ungdomsytelse-soknad-cleanup"
    }

    @Bean(name = [UNGDOMSYTELSE_SØKNAD_PREPROSESSERING_STREAMS_BUILDER_NAME])
    fun preprosesseringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.UNGDOMSYTELSE_SØKNAD_PREPROSESSERING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [UNGDOMSYTELSE_SØKNAD_JOURNALFØRING_STREAMS_BUILDER_NAME])
    fun journalføringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.UNGDOMSYTELSE_SØKNAD_JOURNALFØRING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [UNGDOMSYTELSE_SØKNAD_CLEANUP_STREAMS_BUILDER_NAME])
    fun cleanupStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.UNGDOMSYTELSE_SØKNAD_CLEANUP
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean
    fun ungdomsytelsesøknadMottattTopic(): Topic<TopicEntry<UngdomsytelsesøknadMottatt>> {
        return Topic(
            name = UNGDOMSYTELSE_SØKNAD_MOTTATT_TOPIC,
            serDes = UngdomsytelseMottattSøknadSerdes(objectMapper)
        )
    }

    @Bean
    fun ungdomsytelsesøknadPreprosessertTopic(): Topic<TopicEntry<UngdomsytelsesøknadPreprosessertSøknad>> {
        return Topic(
            name = UNGDOMSYTELSE_SØKNAD_PREPROSESSERT_TOPIC,
            serDes = UngdomsytelsePreprosessertSøknadSerdes(objectMapper)
        )
    }

    @Bean
    fun ungdomsytelsesøknadCleanupTopic(): Topic<TopicEntry<Cleanup<UngdomsytelsesøknadPreprosessertSøknad>>> {
        return Topic(
            name = UNGDOMSYTELSE_SØKNAD_CLEANUP_TOPIC,
            serDes = UngdomsytelseCleanupSøknadSerdes(objectMapper)
        )
    }
}

class UngdomsytelseMottattSøknadSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<UngdomsytelsesøknadMottatt>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<UngdomsytelsesøknadMottatt> {
        return objectMapper.readValue(data)
    }
}

class UngdomsytelsePreprosessertSøknadSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<UngdomsytelsesøknadPreprosessertSøknad>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<UngdomsytelsesøknadPreprosessertSøknad> {
        return objectMapper.readValue(data)
    }
}

class UngdomsytelseCleanupSøknadSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<Cleanup<UngdomsytelsesøknadPreprosessertSøknad>>>(objectMapper) {
    override fun deserialize(
        topic: String,
        data: ByteArray,
    ): TopicEntry<Cleanup<UngdomsytelsesøknadPreprosessertSøknad>> {
        return objectMapper.readValue(data)
    }
}
