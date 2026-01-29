package no.nav.brukerdialog.ytelse.opplæringspenger.kafka

import no.nav.brukerdialog.domenetjenester.mottak.CleanupService
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.processors.LoggingToMDCProcessor
import no.nav.brukerdialog.kafka.processors.process
import no.nav.brukerdialog.kafka.types.Cleanup
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.utils.HealthIndicatorUtils
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.OLPTopologyConfiguration.Companion.OLP_CLEANUP_STREAMS_BUILDER_NAME
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.OLPPreprosessertSøknad
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.processor.api.ProcessorSupplier
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.HealthIndicator
import org.springframework.context.annotation.Bean
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Component

@Component
class OLPSøknadCleanup(
    private val cleanupService: CleanupService<OLPPreprosessertSøknad>,
    private val olpCleanupTopic: Topic<TopicEntry<Cleanup<OLPPreprosessertSøknad>>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(OLP_CLEANUP_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(OLP_CLEANUP_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean
): HealthIndicator {

    private companion object {
        private val STREAM_NAME = KafkaStreamName.OPPLÆRINGSPENGER_SØKNAD_CLEANUP
        private val logger = LoggerFactory.getLogger(OLPSøknadCleanup::class.java)
    }

    @Bean
    fun opplæringspengerCleanupStream(): KStream<String, TopicEntry<Cleanup<OLPPreprosessertSøknad>>> {
        val stream: KStream<String, TopicEntry<Cleanup<OLPPreprosessertSøknad>>> = streamsBuilder.stream(olpCleanupTopic.name, olpCleanupTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .mapValues { _: String, value: TopicEntry<Cleanup<OLPPreprosessertSøknad>> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    cleanupService.cleanup(value.data)
                }
            }

        return stream
    }

    override fun health(): Health = HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
