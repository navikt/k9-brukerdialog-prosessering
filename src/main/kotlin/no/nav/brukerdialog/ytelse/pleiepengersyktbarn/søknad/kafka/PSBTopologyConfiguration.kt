package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.kafka

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
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.PSBMottattSøknad
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.PSBPreprosessertSøknad
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.KafkaStreamsConfiguration
import org.springframework.kafka.config.StreamsBuilderFactoryBean

@Configuration
class PSBTopologyConfiguration(
    private val objectMapper: ObjectMapper,
    private val meterRegistry: MeterRegistry,
    private val kafkaProperties: KafkaProperties,
) {
    companion object {
        const val PSB_PREPROSESSERING_STREAMS_BUILDER_NAME = "psbPreprosesseringStreamsBuilder"
        const val PSB_JOURNALFØRING_STREAMS_BUILDER_NAME = "psbJournalføringStreamsBuilder"
        const val PSB_CLEANUP_STREAMS_BUILDER_NAME = "psbCleanupStreamsBuilder"

        const val PSB_MOTTATT_TOPIC = "dusseldorf.pp-sykt-barn-soknad-mottatt"
        const val PSB_PREPROSESSERT_TOPIC = "dusseldorf.pp-sykt-barn-soknad-preprosessert"
        const val PSB_CLEANUP_TOPIC = "dusseldorf.pp-sykt-barn-soknad-cleanup"
    }

    @Bean(name = [PSB_PREPROSESSERING_STREAMS_BUILDER_NAME])
    fun preprosesseringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.PSB_SØKNAD_PREPROSESSERING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [PSB_JOURNALFØRING_STREAMS_BUILDER_NAME])
    fun journalføringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.PSB_SØKNAD_JOURNALFØRING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [PSB_CLEANUP_STREAMS_BUILDER_NAME])
    fun cleanupStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.PSB_SØKNAD_CLEANUP
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean
    fun psbMottattTopic(): Topic<TopicEntry<PSBMottattSøknad>> {
        return Topic(
            name = PSB_MOTTATT_TOPIC,
            serDes = PSBMottattSøknadSerdes(objectMapper)
        )
    }

    @Bean
    fun psbPreprosessertTopic(): Topic<TopicEntry<PSBPreprosessertSøknad>> {
        return Topic(
            name = PSB_PREPROSESSERT_TOPIC,
            serDes = PSBMPreprosessertSøknadSerdes(objectMapper)
        )
    }

    @Bean
    fun psbCleanupTopic(): Topic<TopicEntry<Cleanup<PSBPreprosessertSøknad>>> {
        return Topic(
            name = PSB_CLEANUP_TOPIC,
            serDes = CleanupSøknadSerdes(objectMapper)
        )
    }
}

class PSBMottattSøknadSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<PSBMottattSøknad>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<PSBMottattSøknad> {
        return objectMapper.readValue(data)
    }
}

class PSBMPreprosessertSøknadSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<PSBPreprosessertSøknad>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<PSBPreprosessertSøknad> {
        return objectMapper.readValue(data)
    }
}

class CleanupSøknadSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<Cleanup<PSBPreprosessertSøknad>>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<Cleanup<PSBPreprosessertSøknad>> {
        return objectMapper.readValue(data)
    }
}
