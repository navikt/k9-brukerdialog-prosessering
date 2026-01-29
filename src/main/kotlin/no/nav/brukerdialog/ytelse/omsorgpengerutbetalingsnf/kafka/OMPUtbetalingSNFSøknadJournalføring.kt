package no.nav.brukerdialog.ytelse.omsorgpengerutbetalingsnf.kafka

import no.nav.brukerdialog.domenetjenester.mottak.JournalføringsService
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.processors.LoggingToMDCProcessor
import no.nav.brukerdialog.kafka.processors.process
import no.nav.brukerdialog.kafka.types.Cleanup
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.utils.HealthIndicatorUtils
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingsnf.kafka.OMPUtbetalingSNFTopologyConfiguration.Companion.OMP_UTB_SNF_JOURNALFØRING_STREAMS_BUILDER_NAME
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingsnf.kafka.domene.OMPUtbetalingSNFSoknadPreprosessert
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
class OMPUtbetalingSNFSøknadJournalføring(
    private val journalføringsService: JournalføringsService,
    private val ompUtbetalingSnfPreprosessertTopic: Topic<TopicEntry<OMPUtbetalingSNFSoknadPreprosessert>>,
    private val ompUtbetalingSnfCleanupTopic: Topic<TopicEntry<Cleanup<OMPUtbetalingSNFSoknadPreprosessert>>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(OMP_UTB_SNF_JOURNALFØRING_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(OMP_UTB_SNF_JOURNALFØRING_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
): HealthIndicator {

    private companion object {
        private val STREAM_NAME = KafkaStreamName.OMP_UTB_SNF_JOURNALFØRING
        private val logger = LoggerFactory.getLogger(OMPUtbetalingSNFSøknadJournalføring::class.java)
    }

    @Bean
    fun omsorgspengerUtbetalingSnfJournalføringsStream(): KStream<String, TopicEntry<OMPUtbetalingSNFSoknadPreprosessert>> {
        val stream = streamsBuilder
            .stream(ompUtbetalingSnfPreprosessertTopic.name, ompUtbetalingSnfPreprosessertTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .mapValues { _: String, value: TopicEntry<OMPUtbetalingSNFSoknadPreprosessert> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val preprosessertSøknad = value.data
                    val journalførSøknad = journalføringsService.journalfør(preprosessertSøknad)
                    Cleanup(value.metadata, preprosessertSøknad, journalførSøknad)
                }
            }
            .to(ompUtbetalingSnfCleanupTopic.name, ompUtbetalingSnfCleanupTopic.producedWith)

        return stream
    }

    override fun health(): Health = HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
