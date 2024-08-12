package no.nav.brukerdialog.meldinger.omsorgpengerutbetalingat

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
import no.nav.brukerdialog.meldinger.omsorgpengerutbetalingat.domene.OMPUtbetalingATSoknadMottatt
import no.nav.brukerdialog.meldinger.omsorgpengerutbetalingat.domene.OMPUtbetalingATSoknadPreprosessert
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.KafkaStreamsConfiguration
import org.springframework.kafka.config.StreamsBuilderFactoryBean

@Configuration
class OMPUtbetalingATTopologyConfiguration(
    private val objectMapper: ObjectMapper,
    private val meterRegistry: MeterRegistry,
    private val kafkaProperties: KafkaProperties,
) {
    companion object {
        const val OMP_UTB_AT_PREPROSESSERING_STREAMS_BUILDER_NAME = "ompUtbetalingAtPreprosesseringStreamsBuilder"
        const val OMP_UTB_AT_JOURNALFØRING_STREAMS_BUILDER_NAME = "ompUtbetalingAtJournalføringStreamsBuilder"
        const val OMP_UTB_AT_CLEANUP_STREAMS_BUILDER_NAME = "ompUtbetalingAtCleanupStreamsBuilder"

        const val OMP_UTB_AT_MOTTATT_TOPIC = "dusseldorf.omp-utbetaling-at-soknad-mottatt"
        const val OMP_UTB_AT_PREPROSESSERT_TOPIC = "dusseldorf.omp-utbetaling-at-soknad-preprosessert"
        const val OMP_UTB_AT_CLEANUP_TOPIC = "dusseldorf.omp-utbetaling-at-soknad-cleanup"
    }

    @Bean(name = [OMP_UTB_AT_PREPROSESSERING_STREAMS_BUILDER_NAME])
    fun preprosesseringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.OMP_UTB_AT_PREPROSESSERING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [OMP_UTB_AT_JOURNALFØRING_STREAMS_BUILDER_NAME])
    fun journalføringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.OMP_UTB_AT_JOURNALFØRING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [OMP_UTB_AT_CLEANUP_STREAMS_BUILDER_NAME])
    fun cleanupStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.OMP_UTB_AT_CLEANUP
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean
    fun ompUtbetalingAtMottattTopic(): Topic<TopicEntry<OMPUtbetalingATSoknadMottatt>> {
        return Topic(
            name = OMP_UTB_AT_MOTTATT_TOPIC,
            serDes = OMPUtbetalingATMottattSøknadSerdes(objectMapper)
        )
    }

    @Bean
    fun ompUtbetalingAtPreprosessertTopic(): Topic<TopicEntry<OMPUtbetalingATSoknadPreprosessert>> {
        return Topic(
            name = OMP_UTB_AT_PREPROSESSERT_TOPIC,
            serDes = OMPUtbetalingATPreprosessertSøknadSerdes(objectMapper)
        )
    }

    @Bean
    fun ompUtbetalingAtCleanupTopic(): Topic<TopicEntry<Cleanup<OMPUtbetalingATSoknadPreprosessert>>> {
        return Topic(
            name = OMP_UTB_AT_CLEANUP_TOPIC,
            serDes = OMPUtbetalingATCleanupSøknadSerdes(objectMapper)
        )
    }
}

class OMPUtbetalingATMottattSøknadSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<OMPUtbetalingATSoknadMottatt>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<OMPUtbetalingATSoknadMottatt> {
        return objectMapper.readValue(data)
    }
}

class OMPUtbetalingATPreprosessertSøknadSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<OMPUtbetalingATSoknadPreprosessert>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<OMPUtbetalingATSoknadPreprosessert> {
        return objectMapper.readValue(data)
    }
}

class OMPUtbetalingATCleanupSøknadSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<Cleanup<OMPUtbetalingATSoknadPreprosessert>>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<Cleanup<OMPUtbetalingATSoknadPreprosessert>> {
        return objectMapper.readValue(data)
    }
}
