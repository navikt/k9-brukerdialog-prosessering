package no.nav.brukerdialog.ytelse.omsorgpengerutbetalingsnf.kafka

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
import no.nav.brukerdialog.meldinger.omsorgpengerutbetalingsnf.domene.OMPUtbetalingSNFSoknadMottatt
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingsnf.kafka.domene.OMPUtbetalingSNFSoknadPreprosessert
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.KafkaStreamsConfiguration
import org.springframework.kafka.config.StreamsBuilderFactoryBean

@Configuration
class OMPUtbetalingSNFTopologyConfiguration(
    private val objectMapper: ObjectMapper,
    private val meterRegistry: MeterRegistry,
    private val kafkaProperties: KafkaProperties,
) {
    companion object {
        const val OMP_UTB_SNF_PREPROSESSERING_STREAMS_BUILDER_NAME = "ompUtbetalingSnfPreprosesseringStreamsBuilder"
        const val OMP_UTB_SNF_JOURNALFØRING_STREAMS_BUILDER_NAME = "ompUtbetalingSnfJournalføringStreamsBuilder"
        const val OMP_UTB_SNF_CLEANUP_STREAMS_BUILDER_NAME = "ompUtbetalingSnfCleanupStreamsBuilder"

        const val OMP_UTB_SNF_MOTTATT_TOPIC = "dusseldorf.omp-utbetaling-snf-soknad-mottatt"
        const val OMP_UTB_SNF_PREPROSESSERT_TOPIC = "dusseldorf.omp-utbetaling-snf-soknad-preprosessert"
        const val OMP_UTB_SNF_CLEANUP_TOPIC = "dusseldorf.omp-utbetaling-snf-soknad-cleanup"
    }

    @Bean(name = [OMP_UTB_SNF_PREPROSESSERING_STREAMS_BUILDER_NAME])
    fun preprosesseringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.OMP_UTB_SNF_PREPROSESSERING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [OMP_UTB_SNF_JOURNALFØRING_STREAMS_BUILDER_NAME])
    fun journalføringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.OMP_UTB_SNF_JOURNALFØRING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [OMP_UTB_SNF_CLEANUP_STREAMS_BUILDER_NAME])
    fun cleanupStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.OMP_UTB_SNF_CLEANUP
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean
    fun ompUtbetalingSnfMottattTopic(): Topic<TopicEntry<OMPUtbetalingSNFSoknadMottatt>> {
        return Topic(
            name = OMP_UTB_SNF_MOTTATT_TOPIC,
            serDes = OMPUtbetalingSNFMottattSøknadSerdes(objectMapper)
        )
    }

    @Bean
    fun ompUtbetalingSnfPreprosessertTopic(): Topic<TopicEntry<OMPUtbetalingSNFSoknadPreprosessert>> {
        return Topic(
            name = OMP_UTB_SNF_PREPROSESSERT_TOPIC,
            serDes = OMPUtbetalingSNFPreprosessertSøknadSerdes(objectMapper)
        )
    }

    @Bean
    fun ompUtbetalingSnfCleanupTopic(): Topic<TopicEntry<Cleanup<OMPUtbetalingSNFSoknadPreprosessert>>> {
        return Topic(
            name = OMP_UTB_SNF_CLEANUP_TOPIC,
            serDes = OMPUtbetalingSNFCleanupSøknadSerdes(objectMapper)
        )
    }
}

class OMPUtbetalingSNFMottattSøknadSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<OMPUtbetalingSNFSoknadMottatt>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<OMPUtbetalingSNFSoknadMottatt> {
        return objectMapper.readValue(data)
    }
}

class OMPUtbetalingSNFPreprosessertSøknadSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<OMPUtbetalingSNFSoknadPreprosessert>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<OMPUtbetalingSNFSoknadPreprosessert> {
        return objectMapper.readValue(data)
    }
}

class OMPUtbetalingSNFCleanupSøknadSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<Cleanup<OMPUtbetalingSNFSoknadPreprosessert>>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<Cleanup<OMPUtbetalingSNFSoknadPreprosessert>> {
        return objectMapper.readValue(data)
    }
}
