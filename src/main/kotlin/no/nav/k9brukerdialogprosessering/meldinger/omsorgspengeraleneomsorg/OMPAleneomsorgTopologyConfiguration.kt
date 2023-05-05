package no.nav.k9brukerdialogprosessering.meldinger.omsorgspengeraleneomsorg

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
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengeraleneomsorg.domene.OMPAleneomsorgSoknadMottatt
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengeraleneomsorg.domene.OMPAleneomsorgSoknadPreprosessert
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.KafkaStreamsConfiguration
import org.springframework.kafka.config.StreamsBuilderFactoryBean

@Configuration
class OMPAleneomsorgTopologyConfiguration(
    private val objectMapper: ObjectMapper,
    private val meterRegistry: MeterRegistry,
    private val kafkaProperties: KafkaProperties,
) {
    companion object {
        const val OMP_AO_PREPROSESSERING_STREAMS_BUILDER_NAME = "ompAleneomsorgPreprosesseringStreamsBuilder"
        const val OMP_AO_JOURNALFØRING_STREAMS_BUILDER_NAME = "ompAleneomsorgJournalføringStreamsBuilder"
        const val OMP_AO_CLEANUP_STREAMS_BUILDER_NAME = "ompAleneomsorgCleanupStreamsBuilder"

        const val OMP_AO_MOTTATT_TOPIC = "dusseldorf.omp-ao-soknad-mottatt"
        const val OMP_AO_PREPROSESSERT_TOPIC = "dusseldorf.omp-ao-soknad-preprosessert"
        const val OMP_AO_CLEANUP_TOPIC = "dusseldorf.omp-ao-soknad-cleanup"
    }

    @Bean(name = [OMP_AO_PREPROSESSERING_STREAMS_BUILDER_NAME])
    fun preprosesseringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.OMP_AO_PREPROSESSERING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [OMP_AO_JOURNALFØRING_STREAMS_BUILDER_NAME])
    fun journalføringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.OMP_AO_JOURNALFØRING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [OMP_AO_CLEANUP_STREAMS_BUILDER_NAME])
    fun cleanupStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.OMP_AO_CLEANUP
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean
    fun ompAleneomsorgMottattTopic(): Topic<TopicEntry<OMPAleneomsorgSoknadMottatt>> {
        return Topic(
            name = OMP_AO_MOTTATT_TOPIC,
            serDes = OMPAleneomsorgMottattSøknadSerdes(objectMapper)
        )
    }

    @Bean
    fun ompAleneomsorgPreprosessertTopic(): Topic<TopicEntry<OMPAleneomsorgSoknadPreprosessert>> {
        return Topic(
            name = OMP_AO_PREPROSESSERT_TOPIC,
            serDes = OMPAleneomsorgPreprosessertSøknadSerdes(objectMapper)
        )
    }

    @Bean
    fun ompAleneomsorgCleanupTopic(): Topic<TopicEntry<Cleanup<OMPAleneomsorgSoknadPreprosessert>>> {
        return Topic(
            name = OMP_AO_CLEANUP_TOPIC,
            serDes = OMPAleneomsorgCleanupSøknadSerdes(objectMapper)
        )
    }
}

class OMPAleneomsorgMottattSøknadSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<OMPAleneomsorgSoknadMottatt>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<OMPAleneomsorgSoknadMottatt> {
        return objectMapper.readValue(data)
    }
}

class OMPAleneomsorgPreprosessertSøknadSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<OMPAleneomsorgSoknadPreprosessert>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<OMPAleneomsorgSoknadPreprosessert> {
        return objectMapper.readValue(data)
    }
}

class OMPAleneomsorgCleanupSøknadSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<Cleanup<OMPAleneomsorgSoknadPreprosessert>>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<Cleanup<OMPAleneomsorgSoknadPreprosessert>> {
        return objectMapper.readValue(data)
    }
}
