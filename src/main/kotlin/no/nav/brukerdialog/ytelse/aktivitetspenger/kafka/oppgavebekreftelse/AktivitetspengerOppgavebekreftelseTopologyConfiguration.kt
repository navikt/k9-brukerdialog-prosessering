package no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.oppgavebekreftelse

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
import no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.oppgavebekreftelse.domene.AktivitetspengerOppgavebekreftelseMottatt
import no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.oppgavebekreftelse.domene.AktivitetspengerOppgavebekreftelsePreprosessert
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.KafkaStreamsConfiguration
import org.springframework.kafka.config.StreamsBuilderFactoryBean

@Configuration
class AktivitetspengerOppgavebekreftelseTopologyConfiguration(
    private val objectMapper: ObjectMapper,
    private val meterRegistry: MeterRegistry,
    private val kafkaProperties: KafkaProperties,
) {
    companion object {
        const val AKTIVITETSPENGER_OPPGAVEBEKREFTELSE_PREPROSESSERING_STREAMS_BUILDER_NAME =
            "aktivitetspengerOppgavebekreftelsePreprosesseringStreamsBuilder"
        const val AKTIVITETSPENGER_OPPGAVEBEKREFTELSE_JOURNALFØRING_STREAMS_BUILDER_NAME =
            "aktivitetspengerOppgavebekreftelseJournalføringStreamsBuilder"
        const val AKTIVITETSPENGER_OPPGAVEBEKREFTELSE_CLEANUP_STREAMS_BUILDER_NAME =
            "aktivitetspengerOppgavebekreftelseCleanupStreamsBuilder"

        const val AKTIVITETSPENGER_OPPGAVEBEKREFTELSE_MOTTATT_TOPIC =
            "dusseldorf.aktivitetspenger-oppgavebekreftelse-mottatt"
        const val AKTIVITETSPENGER_OPPGAVEBEKREFTELSE_PREPROSESSERT_TOPIC =
            "dusseldorf.aktivitetspenger-oppgavebekreftelse-preprosessert"
        const val AKTIVITETSPENGER_OPPGAVEBEKREFTELSE_CLEANUP_TOPIC =
            "dusseldorf.aktivitetspenger-oppgavebekreftelse-cleanup"
    }

    @Bean(name = [AKTIVITETSPENGER_OPPGAVEBEKREFTELSE_PREPROSESSERING_STREAMS_BUILDER_NAME])
    fun preprosesseringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.AKTIVITETSPENGER_OPPGAVEBEKREFTELSE_PREPROSESSERING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [AKTIVITETSPENGER_OPPGAVEBEKREFTELSE_JOURNALFØRING_STREAMS_BUILDER_NAME])
    fun journalføringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.AKTIVITETSPENGER_OPPGAVEBEKREFTELSE_JOURNALFØRING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [AKTIVITETSPENGER_OPPGAVEBEKREFTELSE_CLEANUP_STREAMS_BUILDER_NAME])
    fun cleanupStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.AKTIVITETSPENGER_OPPGAVEBEKREFTELSE_CLEANUP
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean
    fun aktivitetspengerOppgavebekreftelseMottattTopic(): Topic<TopicEntry<AktivitetspengerOppgavebekreftelseMottatt>> {
        return Topic(
            name = AKTIVITETSPENGER_OPPGAVEBEKREFTELSE_MOTTATT_TOPIC,
            serDes = AktivitetspengerMottattOppgavebekreftelseSerdes(objectMapper),
        )
    }

    @Bean
    fun aktivitetspengerOppgavebekreftelsePreprosessertTopic(): Topic<TopicEntry<AktivitetspengerOppgavebekreftelsePreprosessert>> {
        return Topic(
            name = AKTIVITETSPENGER_OPPGAVEBEKREFTELSE_PREPROSESSERT_TOPIC,
            serDes = AktivitetspengerPreprosessertOppgavebekreftelseSerdes(objectMapper),
        )
    }

    @Bean
    fun aktivitetspengerOppgavebekreftelseCleanupTopic(): Topic<TopicEntry<Cleanup<AktivitetspengerOppgavebekreftelsePreprosessert>>> {
        return Topic(
            name = AKTIVITETSPENGER_OPPGAVEBEKREFTELSE_CLEANUP_TOPIC,
            serDes = AktivitetspengerCleanupOppgavebekreftelseSerdes(objectMapper),
        )
    }
}

class AktivitetspengerMottattOppgavebekreftelseSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<AktivitetspengerOppgavebekreftelseMottatt>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<AktivitetspengerOppgavebekreftelseMottatt> {
        return objectMapper.readValue(data)
    }
}

class AktivitetspengerPreprosessertOppgavebekreftelseSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<AktivitetspengerOppgavebekreftelsePreprosessert>>(objectMapper) {
    override fun deserialize(
        topic: String,
        data: ByteArray,
    ): TopicEntry<AktivitetspengerOppgavebekreftelsePreprosessert> {
        return objectMapper.readValue(data)
    }
}

class AktivitetspengerCleanupOppgavebekreftelseSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<Cleanup<AktivitetspengerOppgavebekreftelsePreprosessert>>>(objectMapper) {
    override fun deserialize(
        topic: String,
        data: ByteArray,
    ): TopicEntry<Cleanup<AktivitetspengerOppgavebekreftelsePreprosessert>> {
        return objectMapper.readValue(data)
    }
}
