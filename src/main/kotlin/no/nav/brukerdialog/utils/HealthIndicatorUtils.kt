package no.nav.brukerdialog.utils

import no.nav.brukerdialog.kafka.config.KafkaStreamName
import org.apache.kafka.streams.KafkaStreams
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.Status
import org.springframework.kafka.config.StreamsBuilderFactoryBean

object HealthIndicatorUtils {
    private val logger = LoggerFactory.getLogger(HealthIndicatorUtils::class.java)
    fun resolveKafkaStreamHealth(
        streamName: KafkaStreamName,
        streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
    ): Health {
        val health = when (val kafkaStreams: KafkaStreams? = streamsBuilderFactoryBean.kafkaStreams) {
            null -> Health
                .unknown()
                .withDetail(streamName.value, "${streamName.value} has not yet started")
                .build()

            else -> when (val state = kafkaStreams.state()) {
                KafkaStreams.State.CREATED, KafkaStreams.State.RUNNING, KafkaStreams.State.REBALANCING -> Health
                    .up()
                    .withDetail(streamName.value, "${streamName.value} is in $state state")
                    .build()

                KafkaStreams.State.PENDING_ERROR, KafkaStreams.State.ERROR, KafkaStreams.State.NOT_RUNNING -> Health
                    .down()
                    .withDetail(streamName.value, "${streamName.value} is in $state state")
                    .build()

                else -> Health
                    .unknown()
                    .withDetail(streamName.value, "${streamName.value} is in $state state")
                    .build()
            }
        }
        if (health.status != Status.UP) logger.error("Kafka stream health is {}", health)
        return health
    }
}
