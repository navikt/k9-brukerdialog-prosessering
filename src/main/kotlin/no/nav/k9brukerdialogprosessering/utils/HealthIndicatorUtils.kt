package no.nav.k9brukerdialogprosessering.utils

import no.nav.k9brukerdialogprosessering.kafka.config.KafkaStreamName
import org.apache.kafka.streams.KafkaStreams
import org.springframework.boot.actuate.health.Health
import org.springframework.kafka.config.StreamsBuilderFactoryBean

object HealthIndicatorUtils {
    fun resolveKafkaStreamHealth(
        streamName: KafkaStreamName,
        streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
    ) = when (val kafkaStreams: KafkaStreams? = streamsBuilderFactoryBean.kafkaStreams) {
        null -> Health
            .down()
            .withDetail(streamName.value, "${streamName.value} is null")
            .build()

        else -> when (val state = kafkaStreams.state()) {
            KafkaStreams.State.RUNNING, KafkaStreams.State.REBALANCING -> Health
                .up()
                .withDetail(streamName.value, "${streamName.value} is in $state state")
                .build()

            KafkaStreams.State.ERROR -> Health
                .down()
                .withDetail(streamName.value, "${streamName.value} is in $state state")
                .build()

            else -> Health
                .unknown()
                .withDetail(streamName.value, "${streamName.value} is in $state state")
                .build()
        }
    }
}
