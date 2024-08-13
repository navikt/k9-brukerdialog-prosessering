package no.nav.brukerdialog.ytelse.omsorgspengerkronisksyktbarn.kafka

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
import no.nav.brukerdialog.meldinger.omsorgspengerkronisksyktbarn.domene.OMPUTVKroniskSyktBarnSøknadMottatt
import no.nav.brukerdialog.meldinger.omsorgspengerkronisksyktbarn.domene.OMPUTVKroniskSyktBarnSøknadPreprosesssert
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.KafkaStreamsConfiguration
import org.springframework.kafka.config.StreamsBuilderFactoryBean

@Configuration
class OMPKSTopologyConfiguration(
    private val objectMapper: ObjectMapper,
    private val meterRegistry: MeterRegistry,
    private val kafkaProperties: KafkaProperties,
) {
    companion object {
        const val OMP_UTV_KS_PREPROSESSERING_STREAMS_BUILDER_NAME = "ompUtvKsPreprosesseringStreamsBuilder"
        const val OMP_UTV_KS_JOURNALFØRING_STREAMS_BUILDER_NAME = "ompUtvKsJournalføringStreamsBuilder"
        const val OMP_UTV_KS_CLEANUP_STREAMS_BUILDER_NAME = "ompUtvKsCleanupStreamsBuilder"

        const val OMP_UTV_KS_SØKNAD_MOTTATT_TOPIC = "dusseldorf.omp-utv-kronisk-sykt-barn-soknad-mottatt"
        const val OMP_UTV_KS_SØKNAD_PREPROSESSERT_TOPIC = "dusseldorf.omp-utv-kronisk-sykt-barn-soknad-preprosessert"
        const val OMP_UTV_KS_SØKNAD_CLEANUP_TOPIC = "dusseldorf.omp-utv-kronisk-sykt-barn-soknad-cleanup"
    }

    @Bean(name = [OMP_UTV_KS_PREPROSESSERING_STREAMS_BUILDER_NAME])
    fun preprosesseringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.OMP_UTV_KS_PREPROSESSERING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [OMP_UTV_KS_JOURNALFØRING_STREAMS_BUILDER_NAME])
    fun journalføringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.OMP_UTV_KS_JOURNALFØRING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [OMP_UTV_KS_CLEANUP_STREAMS_BUILDER_NAME])
    fun cleanupStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.OMP_UTV_KS_CLEANUP
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean
    fun ompUtvKsMottattTopic(): Topic<TopicEntry<OMPUTVKroniskSyktBarnSøknadMottatt>> {
        return Topic(
            name = OMP_UTV_KS_SØKNAD_MOTTATT_TOPIC,
            serDes = OmpUtvKsMottattSerdes(objectMapper)
        )
    }

    @Bean
    fun ompUtvKsPreprosessertTopic(): Topic<TopicEntry<OMPUTVKroniskSyktBarnSøknadPreprosesssert>> {
        return Topic(
            name = OMP_UTV_KS_SØKNAD_PREPROSESSERT_TOPIC,
            serDes = OmpUtvKsPreprosessertSerdes(objectMapper)
        )
    }

    @Bean
    fun ompUtvKsCleanupTopic(): Topic<TopicEntry<Cleanup<OMPUTVKroniskSyktBarnSøknadPreprosesssert>>> {
        return Topic(
            name = OMP_UTV_KS_SØKNAD_CLEANUP_TOPIC,
            serDes = CleanupMeldingSerdes(objectMapper)
        )
    }
}

class OmpUtvKsMottattSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<OMPUTVKroniskSyktBarnSøknadMottatt>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<OMPUTVKroniskSyktBarnSøknadMottatt> {
        return objectMapper.readValue(data)
    }
}

class OmpUtvKsPreprosessertSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<OMPUTVKroniskSyktBarnSøknadPreprosesssert>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<OMPUTVKroniskSyktBarnSøknadPreprosesssert> {
        return objectMapper.readValue(data)
    }
}

class CleanupMeldingSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<Cleanup<OMPUTVKroniskSyktBarnSøknadPreprosesssert>>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<Cleanup<OMPUTVKroniskSyktBarnSøknadPreprosesssert>> {
        return objectMapper.readValue(data)
    }
}
