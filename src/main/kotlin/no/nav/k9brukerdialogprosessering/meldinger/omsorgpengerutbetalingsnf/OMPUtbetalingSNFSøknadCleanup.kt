package no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf

import no.nav.k9brukerdialogprosessering.dittnavvarsel.K9Beskjed
import no.nav.k9brukerdialogprosessering.innsending.CleanupService
import no.nav.k9brukerdialogprosessering.kafka.config.KafkaStreamName
import no.nav.k9brukerdialogprosessering.kafka.config.Topic
import no.nav.k9brukerdialogprosessering.kafka.processors.LoggingToMDCProcessor
import no.nav.k9brukerdialogprosessering.kafka.processors.process
import no.nav.k9brukerdialogprosessering.kafka.types.Cleanup
import no.nav.k9brukerdialogprosessering.kafka.types.TopicEntry
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.OMPUtbetalingSNFTopologyConfiguration.Companion.OMP_UTB_SNF_CLEANUP_STREAMS_BUILDER_NAME
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.domene.OMPUtbetalingSNFSoknadPreprosessert
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
class OMPUtbetalingSNFSøknadCleanup(
    private val cleanupService: CleanupService<OMPUtbetalingSNFSoknadPreprosessert>,
    private val ompUtbetalingSnfCleanupTopic: Topic<TopicEntry<Cleanup<OMPUtbetalingSNFSoknadPreprosessert>>>,
    private val k9DittnavVarselTopic: Topic<TopicEntry<K9Beskjed>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(OMP_UTB_SNF_CLEANUP_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(OMP_UTB_SNF_CLEANUP_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean
): HealthIndicator {

    private companion object {
        private val STREAM_NAME = KafkaStreamName.OMP_UTB_SNF_CLEANUP
        private val logger = LoggerFactory.getLogger(OMPUtbetalingSNFSøknadCleanup::class.java)
    }

    @Bean
    fun omsorgspengerUtbetalingSnfCleanupStream(): KStream<String, TopicEntry<Cleanup<OMPUtbetalingSNFSoknadPreprosessert>>> {
        val stream: KStream<String, TopicEntry<Cleanup<OMPUtbetalingSNFSoknadPreprosessert>>> = streamsBuilder.stream(ompUtbetalingSnfCleanupTopic.name, ompUtbetalingSnfCleanupTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .filter { _, value -> "0bca2f81-5fc1-4dd5-a362-d68a92947662" != value.metadata.correlationId }
            .mapValues { _: String, value: TopicEntry<Cleanup<OMPUtbetalingSNFSoknadPreprosessert>> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val cleanup = cleanupService.cleanup(value.data)

                    logger.info("Sender K9Beskjed viderer til ${k9DittnavVarselTopic.name}")
                    cleanup.melding.tilK9DittnavVarsel(value.metadata)
                }
            }
            .to(k9DittnavVarselTopic.name, k9DittnavVarselTopic.producedWith)

        return stream
    }

    override fun health(): Health = HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
