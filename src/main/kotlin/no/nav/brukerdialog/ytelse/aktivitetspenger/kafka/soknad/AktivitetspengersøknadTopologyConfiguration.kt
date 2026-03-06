package no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.soknad

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.micrometer.core.instrument.MeterRegistry
import no.nav.brukerdialog.kafka.config.*
import no.nav.brukerdialog.kafka.config.KafkaStreamsConfigUtils.configure
import no.nav.brukerdialog.kafka.types.Cleanup
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.soknad.domene.AktivitetspengersøknadMottatt
import no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.soknad.domene.AktivitetspengersøknadPreprosessertSøknad
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.KafkaStreamsConfiguration
import org.springframework.kafka.config.StreamsBuilderFactoryBean

@Configuration
class AktivitetspengersøknadTopologyConfiguration(
    private val objectMapper: ObjectMapper,
    private val meterRegistry: MeterRegistry,
    private val kafkaProperties: KafkaProperties,
) {
    companion object {
        const val AKTIVITETSPENGER_SØKNAD_PREPROSESSERING_STREAMS_BUILDER_NAME =
            "aktivitetspengerSøknadPreprosesseringStreamsBuilder"
        const val AKTIVITETSPENGER_SØKNAD_JOURNALFØRING_STREAMS_BUILDER_NAME =
            "aktivitetspengerSøknadJournalføringStreamsBuilder"
        const val AKTIVITETSPENGER_SØKNAD_CLEANUP_STREAMS_BUILDER_NAME = "aktivitetspengerSøknadCleanupStreamsBuilder"

        const val AKTIVITETSPENGER_SØKNAD_MOTTATT_TOPIC = "dusseldorf.aktivitetspenger-soknad-mottatt"
        const val AKTIVITETSPENGER_SØKNAD_PREPROSESSERT_TOPIC = "dusseldorf.aktivitetspenger-soknad-preprosessert"
        const val AKTIVITETSPENGER_SØKNAD_CLEANUP_TOPIC = "dusseldorf.aktivitetspenger-soknad-cleanup"
    }

    @Bean(name = [AKTIVITETSPENGER_SØKNAD_PREPROSESSERING_STREAMS_BUILDER_NAME])
    fun preprosesseringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.AKTIVITETSPENGER_SØKNAD_PREPROSESSERING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [AKTIVITETSPENGER_SØKNAD_JOURNALFØRING_STREAMS_BUILDER_NAME])
    fun journalføringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.AKTIVITETSPENGER_SØKNAD_JOURNALFØRING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [AKTIVITETSPENGER_SØKNAD_CLEANUP_STREAMS_BUILDER_NAME])
    fun cleanupStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.AKTIVITETSPENGER_SØKNAD_CLEANUP
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean
    fun aktivitetspengersøknadMottattTopic(): Topic<TopicEntry<AktivitetspengersøknadMottatt>> {
        return Topic(
            name = AKTIVITETSPENGER_SØKNAD_MOTTATT_TOPIC,
            serDes = AktivitetspengerMottattSøknadSerdes(objectMapper)
        )
    }

    @Bean
    fun aktivitetspengersøknadPreprosessertTopic(): Topic<TopicEntry<AktivitetspengersøknadPreprosessertSøknad>> {
        return Topic(
            name = AKTIVITETSPENGER_SØKNAD_PREPROSESSERT_TOPIC,
            serDes = AktivitetspengerPreprosessertSøknadSerdes(objectMapper)
        )
    }

    @Bean
    fun aktivitetspengersøknadCleanupTopic(): Topic<TopicEntry<Cleanup<AktivitetspengersøknadPreprosessertSøknad>>> {
        return Topic(
            name = AKTIVITETSPENGER_SØKNAD_CLEANUP_TOPIC,
            serDes = AktivitetspengerCleanupSøknadSerdes(objectMapper)
        )
    }
}

class AktivitetspengerMottattSøknadSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<AktivitetspengersøknadMottatt>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<AktivitetspengersøknadMottatt> {
        return objectMapper.readValue(data)
    }
}

class AktivitetspengerPreprosessertSøknadSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<AktivitetspengersøknadPreprosessertSøknad>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<AktivitetspengersøknadPreprosessertSøknad> {
        return objectMapper.readValue(data)
    }
}

class AktivitetspengerCleanupSøknadSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<Cleanup<AktivitetspengersøknadPreprosessertSøknad>>>(objectMapper) {
    override fun deserialize(
        topic: String,
        data: ByteArray,
    ): TopicEntry<Cleanup<AktivitetspengersøknadPreprosessertSøknad>> {
        return objectMapper.readValue(data)
    }
}
