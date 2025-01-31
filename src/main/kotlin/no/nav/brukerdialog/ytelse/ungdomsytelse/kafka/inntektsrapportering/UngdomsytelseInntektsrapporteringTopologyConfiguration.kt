package no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.inntektsrapportering

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
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.inntektsrapportering.domene.UngdomsytelseInntektsrapporteringMottatt
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.inntektsrapportering.domene.UngdomsytelseInntektsrapporteringPreprosessert
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.KafkaStreamsConfiguration
import org.springframework.kafka.config.StreamsBuilderFactoryBean

@Configuration
class UngdomsytelseInntektsrapporteringTopologyConfiguration(
    private val objectMapper: ObjectMapper,
    private val meterRegistry: MeterRegistry,
    private val kafkaProperties: KafkaProperties,
) {
    companion object {
        const val UNGDOMSYTELSE_INNTEKTSRAPPORTERING_PREPROSESSERING_STREAMS_BUILDER_NAME =
            "ungdomsytelseInntektsrapporteringPreprosesseringStreamsBuilder"
        const val UNGDOMSYTELSE_INNTEKTSRAPPORTERING_JOURNALFØRING_STREAMS_BUILDER_NAME =
            "ungdomsytelseInntektsrapporteringJournalføringStreamsBuilder"
        const val UNGDOMSYTELSE_INNTEKTSRAPPORTERING_CLEANUP_STREAMS_BUILDER_NAME = "ungdomsytelseInntektsrapporteringCleanupStreamsBuilder"

        const val UNGDOMSYTELSE_INNTEKTSRAPPORTERING_MOTTATT_TOPIC = "dusseldorf.ungdomsytelse-inntektsrapportering-mottatt"
        const val UNGDOMSYTELSE_INNTEKTSRAPPORTERING_PREPROSESSERT_TOPIC = "dusseldorf.ungdomsytelse-inntektsrapportering-preprosessert"
        const val UNGDOMSYTELSE_INNTEKTSRAPPORTERING_CLEANUP_TOPIC = "dusseldorf.ungdomsytelse-inntektsrapportering-cleanup"
    }

    @Bean(name = [UNGDOMSYTELSE_INNTEKTSRAPPORTERING_PREPROSESSERING_STREAMS_BUILDER_NAME])
    fun preprosesseringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.UNGDOMSYTELSE_INNTEKTSRAPPORTERING_PREPROSESSERING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [UNGDOMSYTELSE_INNTEKTSRAPPORTERING_JOURNALFØRING_STREAMS_BUILDER_NAME])
    fun journalføringStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.UNGDOMSYTELSE_INNTEKTSRAPPORTERING_JOURNALFØRING
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean(name = [UNGDOMSYTELSE_INNTEKTSRAPPORTERING_CLEANUP_STREAMS_BUILDER_NAME])
    fun cleanupStreamBuilder(): StreamsBuilderFactoryBean {
        val streamPropertyKey = KafkaStreamName.UNGDOMSYTELSE_INNTEKTSRAPPORTERING_CLEANUP
        val props = KafkaStreamsConfigUtils.commonStreamsConfigProperties(kafkaProperties, streamPropertyKey)
        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure(streamPropertyKey, meterRegistry)
        return streamsBuilderFactoryBean
    }

    @Bean
    fun ungdomsytelseInntektsrapporteringMottattTopic(): Topic<TopicEntry<UngdomsytelseInntektsrapporteringMottatt>> {
        return Topic(
            name = UNGDOMSYTELSE_INNTEKTSRAPPORTERING_MOTTATT_TOPIC,
            serDes = UngdomsytelseMottattInntektsrapporteringSerdes(objectMapper)
        )
    }

    @Bean
    fun ungdomsytelseInntektsrapporteringPreprosessertTopic(): Topic<TopicEntry<UngdomsytelseInntektsrapporteringPreprosessert>> {
        return Topic(
            name = UNGDOMSYTELSE_INNTEKTSRAPPORTERING_PREPROSESSERT_TOPIC,
            serDes = UngdomsytelsePreprosessertInntektsrapporteringSerdes(objectMapper)
        )
    }

    @Bean
    fun ungdomsytelseInntektsrapporteringCleanupTopic(): Topic<TopicEntry<Cleanup<UngdomsytelseInntektsrapporteringPreprosessert>>> {
        return Topic(
            name = UNGDOMSYTELSE_INNTEKTSRAPPORTERING_CLEANUP_TOPIC,
            serDes = UngdomsytelseCleanupInntektsrapporteringSerdes(objectMapper)
        )
    }
}

class UngdomsytelseMottattInntektsrapporteringSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<UngdomsytelseInntektsrapporteringMottatt>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<UngdomsytelseInntektsrapporteringMottatt> {
        return objectMapper.readValue(data)
    }
}

class UngdomsytelsePreprosessertInntektsrapporteringSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<UngdomsytelseInntektsrapporteringPreprosessert>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<UngdomsytelseInntektsrapporteringPreprosessert> {
        return objectMapper.readValue(data)
    }
}

class UngdomsytelseCleanupInntektsrapporteringSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<Cleanup<UngdomsytelseInntektsrapporteringPreprosessert>>>(objectMapper) {
    override fun deserialize(
        topic: String,
        data: ByteArray,
    ): TopicEntry<Cleanup<UngdomsytelseInntektsrapporteringPreprosessert>> {
        return objectMapper.readValue(data)
    }
}
