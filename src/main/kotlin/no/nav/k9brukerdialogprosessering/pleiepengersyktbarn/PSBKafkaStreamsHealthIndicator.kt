package no.nav.k9brukerdialogprosessering.pleiepengersyktbarn

import no.nav.k9brukerdialogprosessering.kafka.config.KafkaStreamsConfig.Companion.PSB_STREAMS_BUILDER_BEAN_NAME
import org.apache.kafka.streams.KafkaStreams
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import org.springframework.stereotype.Component

@Component
class PSBKafkaStreamsHealthIndicator(
    @Qualifier(PSB_STREAMS_BUILDER_BEAN_NAME) private val psbKafkaStreamsBuilder: StreamsBuilderFactoryBean,
) : HealthIndicator {
    private companion object {
        private val logger = LoggerFactory.getLogger(PSBKafkaStreamsHealthIndicator::class.java)
        val PSB_KAFKA_STREAMS = "PSBKafkaStreams"
    }

    override fun health(): Health {
        return when (val kafkaStreams: KafkaStreams? = psbKafkaStreamsBuilder.kafkaStreams) {
            null -> Health.down().withDetail(PSB_KAFKA_STREAMS, "$PSB_KAFKA_STREAMS is null").build()
            else -> when (kafkaStreams.state()) {
                KafkaStreams.State.RUNNING -> {
                    val streamsMetrics = kafkaStreams.metrics()
                    logger.info("KafkaStreams metrics: {}", streamsMetrics)
                    Health.up().withDetail("kafkaStreams", "$PSB_KAFKA_STREAMS is running").build()
                }
                else -> {
                    val streamsMetrics = kafkaStreams.metrics()
                    logger.info("KafkaStreams metrics: {}", streamsMetrics)
                    Health.down().withDetail(PSB_KAFKA_STREAMS, kafkaStreams.state().toString()).build()
                }
            }
        }
    }
}
