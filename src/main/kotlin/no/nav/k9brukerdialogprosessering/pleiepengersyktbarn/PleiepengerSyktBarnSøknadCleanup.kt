package no.nav.k9brukerdialogprosessering.pleiepengersyktbarn

import no.nav.k9brukerdialogprosessering.innsending.CleanupService
import no.nav.k9brukerdialogprosessering.kafka.config.KafkaStreamName
import no.nav.k9brukerdialogprosessering.kafka.config.Topic
import no.nav.k9brukerdialogprosessering.kafka.processors.LoggingToMDCProcessor
import no.nav.k9brukerdialogprosessering.kafka.processors.process
import no.nav.k9brukerdialogprosessering.kafka.types.Cleanup
import no.nav.k9brukerdialogprosessering.kafka.types.TopicEntry
import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.PSBTopologyConfiguration.Companion.PSB_CLEANUP_STREAMS_BUILDER_NAME
import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene.PSBPreprosessertSøknad
import no.nav.k9brukerdialogprosessering.utils.HealthIndicatorUtils
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.processor.api.ProcessorSupplier
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.context.annotation.Bean
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Component

@Component
class PleiepengerSyktBarnSøknadCleanup(
    private val cleanupService: CleanupService<PSBPreprosessertSøknad>,
    private val psbCleanupTopic: Topic<TopicEntry<Cleanup<PSBPreprosessertSøknad>>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(PSB_CLEANUP_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(PSB_CLEANUP_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean
): HealthIndicator {

    private companion object {
        private val STREAM_NAME = KafkaStreamName.PSB_SØKNAD_CLEANUP
        private val logger = LoggerFactory.getLogger(PleiepengerSyktBarnSøknadCleanup::class.java)
    }

    @Bean
    fun pleiepengerSyktBarnCleanupStream(): KStream<String, TopicEntry<Cleanup<PSBPreprosessertSøknad>>> {
        val stream: KStream<String, TopicEntry<Cleanup<PSBPreprosessertSøknad>>> = streamsBuilder.stream(psbCleanupTopic.name, psbCleanupTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .mapValues { _: String, value: TopicEntry<Cleanup<PSBPreprosessertSøknad>> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    cleanupService.cleanup(value.data)
                }
            }

        return stream
    }

    override fun health(): Health = HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
