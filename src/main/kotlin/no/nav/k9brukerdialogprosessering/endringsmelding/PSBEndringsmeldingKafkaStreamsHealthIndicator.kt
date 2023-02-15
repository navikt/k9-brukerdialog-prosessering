package no.nav.k9brukerdialogprosessering.endringsmelding

import no.nav.k9brukerdialogprosessering.kafka.config.KafkaStreamsConfig.Companion.PSB_ENDRINGSMELDING_STREAMS_BUILDER_BEAN_NAME
import org.apache.kafka.streams.KafkaStreams
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import org.springframework.stereotype.Component

@Component
class PSBEndringsmeldingKafkaStreamsHealthIndicator(
    @Qualifier(PSB_ENDRINGSMELDING_STREAMS_BUILDER_BEAN_NAME) private val psbEndringsmeldingKafkaStreamsBuilder: StreamsBuilderFactoryBean,
) : HealthIndicator {
    private companion object {
        private val logger = LoggerFactory.getLogger(PSBEndringsmeldingKafkaStreamsHealthIndicator::class.java)
        val PSB_ENDRINGSMELDING_KAFKA_STREAMS = "PSBEndringsmeldingKafkaStreams"
    }

    override fun health(): Health {
        return when (val kafkaStreams: KafkaStreams? = psbEndringsmeldingKafkaStreamsBuilder.kafkaStreams) {
            null -> Health.down().withDetail(PSB_ENDRINGSMELDING_KAFKA_STREAMS, "$PSB_ENDRINGSMELDING_KAFKA_STREAMS is null").build()
            else -> when (val state = kafkaStreams.state()) {
                KafkaStreams.State.RUNNING, KafkaStreams.State.REBALANCING -> Health.up().withDetail(PSB_ENDRINGSMELDING_KAFKA_STREAMS, "$PSB_ENDRINGSMELDING_KAFKA_STREAMS is in $state state").build()
                KafkaStreams.State.ERROR -> Health.down().withDetail(PSB_ENDRINGSMELDING_KAFKA_STREAMS, "$PSB_ENDRINGSMELDING_KAFKA_STREAMS is in $state state").build()
                else -> Health.unknown().withDetail(PSB_ENDRINGSMELDING_KAFKA_STREAMS, "$PSB_ENDRINGSMELDING_KAFKA_STREAMS is in $state state").build()
            }
        }
    }
}
