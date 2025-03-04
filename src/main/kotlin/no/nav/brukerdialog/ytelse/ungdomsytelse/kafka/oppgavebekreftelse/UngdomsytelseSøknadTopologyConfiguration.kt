package no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.oppgavebekreftelse

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
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.oppgavebekreftelse.domene.UngdomsytelseOppgavebekreftelseMottatt
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.oppgavebekreftelse.domene.UngdomsytelseOppgavebekreftelsePreprosessert
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.KafkaStreamsConfiguration
import org.springframework.kafka.config.StreamsBuilderFactoryBean

@Configuration
class UngdomsytelseOppgavebekreftelseTopologyConfiguration(
    private val objectMapper: ObjectMapper,
    private val meterRegistry: MeterRegistry,
    private val kafkaProperties: KafkaProperties,
) {
    companion object {
        const val UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_PREPROSESSERING_STREAMS_BUILDER_NAME =
            "ungdomsytelseOppgavebekreftelsePreprosesseringStreamsBuilder"
        const val UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_JOURNALFØRING_STREAMS_BUILDER_NAME =
            "ungdomsytelseOppgavebekreftelseJournalføringStreamsBuilder"
        const val UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_CLEANUP_STREAMS_BUILDER_NAME =
            "ungdomsytelseOppgavebekreftelseCleanupStreamsBuilder"

        const val UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_MOTTATT_TOPIC = "dusseldorf.ungdomsytelse-oppgavebekreftelse-mottatt"
        const val UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_PREPROSESSERT_TOPIC =
            "dusseldorf.ungdomsytelse-oppgavebekreftelse-preprosessert"
        const val UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_CLEANUP_TOPIC = "dusseldorf.ungdomsytelse-oppgavebekreftelse-cleanup"
    }

    @Bean(name = [UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_PREPROSESSERING_STREAMS_BUILDER_NAME])
    fun preprosesseringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_PREPROSESSERING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_JOURNALFØRING_STREAMS_BUILDER_NAME])
    fun journalføringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_JOURNALFØRING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_CLEANUP_STREAMS_BUILDER_NAME])
    fun cleanupStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_CLEANUP
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean
    fun ungdomsytelseOppgavebekreftelseMottattTopic(): Topic<TopicEntry<UngdomsytelseOppgavebekreftelseMottatt>> {
        return Topic(
            name = UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_MOTTATT_TOPIC,
            serDes = UngdomsytelseMottattOppgavebekreftelseSerdes(objectMapper)
        )
    }

    @Bean
    fun ungdomsytelseOppgavebekreftelsePreprosessertTopic(): Topic<TopicEntry<UngdomsytelseOppgavebekreftelsePreprosessert>> {
        return Topic(
            name = UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_PREPROSESSERT_TOPIC,
            serDes = UngdomsytelsePreprosessertOppgavebekreftelseSerdes(objectMapper)
        )
    }

    @Bean
    fun ungdomsytelseOppgavebekreftelseCleanupTopic(): Topic<TopicEntry<Cleanup<UngdomsytelseOppgavebekreftelsePreprosessert>>> {
        return Topic(
            name = UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_CLEANUP_TOPIC,
            serDes = UngdomsytelseCleanupOppgavebekreftelseSerdes(objectMapper)
        )
    }
}

class UngdomsytelseMottattOppgavebekreftelseSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<UngdomsytelseOppgavebekreftelseMottatt>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<UngdomsytelseOppgavebekreftelseMottatt> {
        return objectMapper.readValue(data)
    }
}

class UngdomsytelsePreprosessertOppgavebekreftelseSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<UngdomsytelseOppgavebekreftelsePreprosessert>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<UngdomsytelseOppgavebekreftelsePreprosessert> {
        return objectMapper.readValue(data)
    }
}

class UngdomsytelseCleanupOppgavebekreftelseSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<Cleanup<UngdomsytelseOppgavebekreftelsePreprosessert>>>(objectMapper) {
    override fun deserialize(
        topic: String,
        data: ByteArray,
    ): TopicEntry<Cleanup<UngdomsytelseOppgavebekreftelsePreprosessert>> {
        return objectMapper.readValue(data)
    }
}
