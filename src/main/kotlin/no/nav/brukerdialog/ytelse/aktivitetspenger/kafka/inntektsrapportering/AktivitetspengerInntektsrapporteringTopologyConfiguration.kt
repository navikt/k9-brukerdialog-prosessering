package no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.inntektsrapportering

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
import no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.inntektsrapportering.domene.AktivitetspengerInntektsrapporteringMottatt
import no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.inntektsrapportering.domene.AktivitetspengerInntektsrapporteringPreprosessert
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.KafkaStreamsConfiguration
import org.springframework.kafka.config.StreamsBuilderFactoryBean

@Configuration
class AktivitetspengerInntektsrapporteringTopologyConfiguration(
    private val objectMapper: ObjectMapper,
    private val meterRegistry: MeterRegistry,
    private val kafkaProperties: KafkaProperties,
) {
    companion object {
        const val AKTIVITETSPENGER_INNTEKTSRAPPORTERING_PREPROSESSERING_STREAMS_BUILDER_NAME =
            "aktivitetspengerInntektsrapporteringPreprosesseringStreamsBuilder"
        const val AKTIVITETSPENGER_INNTEKTSRAPPORTERING_JOURNALFØRING_STREAMS_BUILDER_NAME =
            "aktivitetspengerInntektsrapporteringJournalføringStreamsBuilder"
        const val AKTIVITETSPENGER_INNTEKTSRAPPORTERING_CLEANUP_STREAMS_BUILDER_NAME = "aktivitetspengerInntektsrapporteringCleanupStreamsBuilder"

        const val AKTIVITETSPENGER_INNTEKTSRAPPORTERING_MOTTATT_TOPIC = "dusseldorf.aktivitetspenger-inntektsrapportering-mottatt"
        const val AKTIVITETSPENGER_INNTEKTSRAPPORTERING_PREPROSESSERT_TOPIC = "dusseldorf.aktivitetspenger-inntektsrapportering-preprosessert"
        const val AKTIVITETSPENGER_INNTEKTSRAPPORTERING_CLEANUP_TOPIC = "dusseldorf.aktivitetspenger-inntektsrapportering-cleanup"
    }

    @Bean(name = [AKTIVITETSPENGER_INNTEKTSRAPPORTERING_PREPROSESSERING_STREAMS_BUILDER_NAME])
    fun preprosesseringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.AKTIVITETSPENGER_INNTEKTSRAPPORTERING_PREPROSESSERING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [AKTIVITETSPENGER_INNTEKTSRAPPORTERING_JOURNALFØRING_STREAMS_BUILDER_NAME])
    fun journalføringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.AKTIVITETSPENGER_INNTEKTSRAPPORTERING_JOURNALFØRING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [AKTIVITETSPENGER_INNTEKTSRAPPORTERING_CLEANUP_STREAMS_BUILDER_NAME])
    fun cleanupStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.AKTIVITETSPENGER_INNTEKTSRAPPORTERING_CLEANUP
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean
    fun aktivitetspengerInntektsrapporteringMottattTopic(): Topic<TopicEntry<AktivitetspengerInntektsrapporteringMottatt>> {
        return Topic(
            name = AKTIVITETSPENGER_INNTEKTSRAPPORTERING_MOTTATT_TOPIC,
            serDes = AktivitetspengerMottattInntektsrapporteringSerdes(objectMapper)
        )
    }

    @Bean
    fun aktivitetspengerInntektsrapporteringPreprosessertTopic(): Topic<TopicEntry<AktivitetspengerInntektsrapporteringPreprosessert>> {
        return Topic(
            name = AKTIVITETSPENGER_INNTEKTSRAPPORTERING_PREPROSESSERT_TOPIC,
            serDes = AktivitetspengerPreprosessertInntektsrapporteringSerdes(objectMapper)
        )
    }

    @Bean
    fun aktivitetpengerInntektsrapporteringCleanupTopic(): Topic<TopicEntry<Cleanup<AktivitetspengerInntektsrapporteringPreprosessert>>> {
        return Topic(
            name = AKTIVITETSPENGER_INNTEKTSRAPPORTERING_CLEANUP_TOPIC,
            serDes = AktivitetspengerCleanupInntektsrapporteringSerdes(objectMapper)
        )
    }
}

class AktivitetspengerMottattInntektsrapporteringSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<AktivitetspengerInntektsrapporteringMottatt>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<AktivitetspengerInntektsrapporteringMottatt> {
        return objectMapper.readValue(data)
    }
}

class AktivitetspengerPreprosessertInntektsrapporteringSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<AktivitetspengerInntektsrapporteringPreprosessert>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<AktivitetspengerInntektsrapporteringPreprosessert> {
        return objectMapper.readValue(data)
    }
}

class AktivitetspengerCleanupInntektsrapporteringSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<Cleanup<AktivitetspengerInntektsrapporteringPreprosessert>>>(objectMapper) {
    override fun deserialize(
        topic: String,
        data: ByteArray,
    ): TopicEntry<Cleanup<AktivitetspengerInntektsrapporteringPreprosessert>> {
        return objectMapper.readValue(data)
    }
}
