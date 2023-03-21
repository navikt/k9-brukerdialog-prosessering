package no.nav.k9brukerdialogprosessering.meldinger.endringsmelding

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.micrometer.core.instrument.MeterRegistry
import no.nav.k9brukerdialogprosessering.meldinger.endringsmelding.domene.PSBEndringsmeldingMottatt
import no.nav.k9brukerdialogprosessering.meldinger.endringsmelding.domene.PSBPreprossesertEndringsmelding
import no.nav.k9brukerdialogprosessering.kafka.config.KafkaProperties
import no.nav.k9brukerdialogprosessering.kafka.config.KafkaStreamsConfigUtils.commonStreamsConfigProperties
import no.nav.k9brukerdialogprosessering.kafka.config.KafkaStreamsConfigUtils.configure
import no.nav.k9brukerdialogprosessering.kafka.config.SerDes
import no.nav.k9brukerdialogprosessering.kafka.config.KafkaStreamName
import no.nav.k9brukerdialogprosessering.kafka.config.Topic
import no.nav.k9brukerdialogprosessering.kafka.types.Cleanup
import no.nav.k9brukerdialogprosessering.kafka.types.TopicEntry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.KafkaStreamsConfiguration
import org.springframework.kafka.config.StreamsBuilderFactoryBean

@Configuration
class PSBEndringsmeldingTopologyConfiguration(
    private val objectMapper: ObjectMapper,
    private val meterRegistry: MeterRegistry,
    private val kafkaProperties: KafkaProperties,
) {
    companion object {
        const val PSB_ENDRINGSMELDING_PREPROSESSERING_STREAMS_BUILDER_NAME = "psbEndringsmeldingPreprosesseringStreamsBuilder"
        const val PSB_ENDRINGSMELDING_JOURNALFØRING_STREAMS_BUILDER_NAME = "psbEndringsmeldingJournalføringStreamsBuilder"
        const val PSB_ENDRINGSMELDING_CLEANUP_STREAMS_BUILDER_NAME = "psbEndringsmeldingCleanupStreamsBuilder"

        const val PSB_ENDRINGSMELDING_MOTTATT_TOPIC = "dusseldorf.privat-endringsmelding-pleiepenger-sykt-barn-mottatt"
        const val PSB_ENDRINGSMELDING_PREPROSESSERT_TOPIC = "dusseldorf.privat-endringsmelding-pleiepenger-sykt-barn-preprosessert"
        const val PSB_ENDRINGSMELDING_CLEANUP_TOPIC = "dusseldorf.privat-endringsmelding-pleiepenger-sykt-barn-cleanup"
    }

    @Bean(name = [PSB_ENDRINGSMELDING_PREPROSESSERING_STREAMS_BUILDER_NAME])
    fun preprosesseringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.PSB_ENDRINGSMELDING_PREPROSESSERING
        val props = commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [PSB_ENDRINGSMELDING_JOURNALFØRING_STREAMS_BUILDER_NAME])
    fun journalføringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.PSB_ENDRINGSMELDING_JOURNALFØRING
        val props = commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [PSB_ENDRINGSMELDING_CLEANUP_STREAMS_BUILDER_NAME])
    fun cleanupStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.PSB_ENDRINGSMELDING_CLEANUP
        val props = commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean
    fun psbEndringsmeldingMottattTopic(): Topic<TopicEntry<PSBEndringsmeldingMottatt>> {
        return Topic(
            name = PSB_ENDRINGSMELDING_MOTTATT_TOPIC,
            serDes = PSBEndringsmeldingMottattSerdes(objectMapper)
        )
    }

    @Bean
    fun psbEndringsmeldingPreprosessertTopic(): Topic<TopicEntry<PSBPreprossesertEndringsmelding>> {
        return Topic(
            name = PSB_ENDRINGSMELDING_PREPROSESSERT_TOPIC,
            serDes = PSBEndringsmeldingPreprosessertSerdes(objectMapper)
        )
    }

    @Bean
    fun psbEndringsmeldingCleanupTopic(): Topic<TopicEntry<Cleanup<PSBPreprossesertEndringsmelding>>> {
        return Topic(
            name = PSB_ENDRINGSMELDING_CLEANUP_TOPIC,
            serDes = CleanupPSBEndringsmeldingSerdes(objectMapper)
        )
    }
}

class PSBEndringsmeldingMottattSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<PSBEndringsmeldingMottatt>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<PSBEndringsmeldingMottatt> {
        return objectMapper.readValue(data)
    }
}

class PSBEndringsmeldingPreprosessertSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<PSBPreprossesertEndringsmelding>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<PSBPreprossesertEndringsmelding> {
        return objectMapper.readValue(data)
    }
}

class CleanupPSBEndringsmeldingSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<Cleanup<PSBPreprossesertEndringsmelding>>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<Cleanup<PSBPreprossesertEndringsmelding>> {
        return objectMapper.readValue(data)
    }
}
