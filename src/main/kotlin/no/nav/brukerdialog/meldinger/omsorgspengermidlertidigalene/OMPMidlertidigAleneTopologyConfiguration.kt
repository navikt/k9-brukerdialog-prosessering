package no.nav.brukerdialog.meldinger.omsorgspengermidlertidigalene

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
import no.nav.brukerdialog.meldinger.omsorgspengermidlertidigalene.domene.OMPMidlertidigAleneSoknadMottatt
import no.nav.brukerdialog.meldinger.omsorgspengermidlertidigalene.domene.OMPMidlertidigAleneSoknadPreprosessert
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.KafkaStreamsConfiguration
import org.springframework.kafka.config.StreamsBuilderFactoryBean

@Configuration
class OMPMidlertidigAleneTopologyConfiguration(
    private val objectMapper: ObjectMapper,
    private val meterRegistry: MeterRegistry,
    private val kafkaProperties: KafkaProperties,
) {
    companion object {
        const val OMP_MA_PREPROSESSERING_STREAMS_BUILDER_NAME = "ompMidlertidigAlenePreprosesseringStreamsBuilder"
        const val OMP_MA_JOURNALFØRING_STREAMS_BUILDER_NAME = "ompMidlertidigAleneJournalføringStreamsBuilder"
        const val OMP_MA_CLEANUP_STREAMS_BUILDER_NAME = "ompMidlertidigAleneCleanupStreamsBuilder"

        const val OMP_MA_MOTTATT_TOPIC = "dusseldorf.omp-ma-soknad-mottatt"
        const val OMP_MA_PREPROSESSERT_TOPIC = "dusseldorf.omp-ma-soknad-preprosessert"
        const val OMP_MA_CLEANUP_TOPIC = "dusseldorf.omp-ma-soknad-cleanup"
    }

    @Bean(name = [OMP_MA_PREPROSESSERING_STREAMS_BUILDER_NAME])
    fun preprosesseringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.OMP_MA_PREPROSESSERING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [OMP_MA_JOURNALFØRING_STREAMS_BUILDER_NAME])
    fun journalføringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.OMP_MA_JOURNALFØRING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [OMP_MA_CLEANUP_STREAMS_BUILDER_NAME])
    fun cleanupStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.OMP_MA_CLEANUP
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean
    fun ompMidlertidigAleneMottattTopic(): Topic<TopicEntry<OMPMidlertidigAleneSoknadMottatt>> {
        return Topic(
            name = OMP_MA_MOTTATT_TOPIC,
            serDes = OMPMidlertidigAleneMottattSøknadSerdes(objectMapper)
        )
    }

    @Bean
    fun ompMidlertidigAlenePreprosessertTopic(): Topic<TopicEntry<OMPMidlertidigAleneSoknadPreprosessert>> {
        return Topic(
            name = OMP_MA_PREPROSESSERT_TOPIC,
            serDes = OMPMidlertidigAlenePreprosessertSøknadSerdes(objectMapper)
        )
    }

    @Bean
    fun ompMidlertidigAleneCleanupTopic(): Topic<TopicEntry<Cleanup<OMPMidlertidigAleneSoknadPreprosessert>>> {
        return Topic(
            name = OMP_MA_CLEANUP_TOPIC,
            serDes = OMPMidlertidigAleneCleanupSøknadSerdes(objectMapper)
        )
    }
}

class OMPMidlertidigAleneMottattSøknadSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<OMPMidlertidigAleneSoknadMottatt>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<OMPMidlertidigAleneSoknadMottatt> {
        return objectMapper.readValue(data)
    }
}

class OMPMidlertidigAlenePreprosessertSøknadSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<OMPMidlertidigAleneSoknadPreprosessert>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<OMPMidlertidigAleneSoknadPreprosessert> {
        return objectMapper.readValue(data)
    }
}

class OMPMidlertidigAleneCleanupSøknadSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<Cleanup<OMPMidlertidigAleneSoknadPreprosessert>>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<Cleanup<OMPMidlertidigAleneSoknadPreprosessert>> {
        return objectMapper.readValue(data)
    }
}
