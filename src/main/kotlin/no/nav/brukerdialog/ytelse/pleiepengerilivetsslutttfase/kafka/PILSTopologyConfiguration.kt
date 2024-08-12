package no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.kafka

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
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.PilsPreprosessertSøknad
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.PilsSøknadMottatt
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.KafkaStreamsConfiguration
import org.springframework.kafka.config.StreamsBuilderFactoryBean

@Configuration
class PILSTopologyConfiguration(
    private val objectMapper: ObjectMapper,
    private val meterRegistry: MeterRegistry,
    private val kafkaProperties: KafkaProperties,
) {
    companion object {
        const val PILS_PREPROSESSERING_STREAMS_BUILDER_NAME = "pilsPreprosesseringStreamsBuilder"
        const val PILS_JOURNALFØRING_STREAMS_BUILDER_NAME = "pilsJournalføringStreamsBuilder"
        const val PILS_CLEANUP_STREAMS_BUILDER_NAME = "pilsCleanupStreamsBuilder"

        const val PILS_MOTTATT_TOPIC = "dusseldorf.pp-i-livets-sluttfase-soknad-mottatt"
        const val PILS_PREPROSESSERT_TOPIC = "dusseldorf.pp-i-livets-sluttfase-soknad-preprosessert"
        const val PILS_CLEANUP_TOPIC = "dusseldorf.pp-i-livets-sluttfase-soknad-cleanup"
    }

    @Bean(name = [PILS_PREPROSESSERING_STREAMS_BUILDER_NAME])
    fun preprosesseringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.PILS_SØKNAD_PREPROSESSERING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [PILS_JOURNALFØRING_STREAMS_BUILDER_NAME])
    fun journalføringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.PILS_SØKNAD_JOURNALFØRING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [PILS_CLEANUP_STREAMS_BUILDER_NAME])
    fun cleanupStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.PILS_SØKNAD_CLEANUP
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean
    fun pilsMottattTopic(): Topic<TopicEntry<PilsSøknadMottatt>> {
        return Topic(
            name = PILS_MOTTATT_TOPIC,
            serDes = PILSMottattSøknadSerdes(objectMapper)
        )
    }

    @Bean
    fun pilsPreprosessertTopic(): Topic<TopicEntry<PilsPreprosessertSøknad>> {
        return Topic(
            name = PILS_PREPROSESSERT_TOPIC,
            serDes = PILSMPreprosessertSøknadSerdes(objectMapper)
        )
    }

    @Bean
    fun pilsCleanupTopic(): Topic<TopicEntry<Cleanup<PilsPreprosessertSøknad>>> {
        return Topic(
            name = PILS_CLEANUP_TOPIC,
            serDes = CleanupSøknadSerdes(objectMapper)
        )
    }
}

class PILSMottattSøknadSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<PilsSøknadMottatt>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<PilsSøknadMottatt> {
        return objectMapper.readValue(data)
    }
}

class PILSMPreprosessertSøknadSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<PilsPreprosessertSøknad>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<PilsPreprosessertSøknad> {
        return objectMapper.readValue(data)
    }
}

class CleanupSøknadSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<Cleanup<PilsPreprosessertSøknad>>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<Cleanup<PilsPreprosessertSøknad>> {
        return objectMapper.readValue(data)
    }
}
