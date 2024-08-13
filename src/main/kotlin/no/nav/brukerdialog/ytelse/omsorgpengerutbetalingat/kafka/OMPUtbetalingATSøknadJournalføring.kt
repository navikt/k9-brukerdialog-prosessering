package no.nav.brukerdialog.ytelse.omsorgpengerutbetalingat.kafka

import no.nav.brukerdialog.domenetjenester.mottak.JournalføringsService
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.processors.LoggingToMDCProcessor
import no.nav.brukerdialog.kafka.processors.process
import no.nav.brukerdialog.kafka.types.Cleanup
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingat.kafka.OMPUtbetalingATTopologyConfiguration.Companion.OMP_UTB_AT_JOURNALFØRING_STREAMS_BUILDER_NAME
import no.nav.brukerdialog.meldinger.omsorgpengerutbetalingat.domene.OMPUtbetalingATSoknadPreprosessert
import no.nav.brukerdialog.utils.HealthIndicatorUtils
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
class OMPUtbetalingATSøknadJournalføring(
    private val journalføringsService: JournalføringsService,
    private val ompUtbetalingAtPreprosessertTopic: Topic<TopicEntry<OMPUtbetalingATSoknadPreprosessert>>,
    private val ompUtbetalingAtCleanupTopic: Topic<TopicEntry<Cleanup<OMPUtbetalingATSoknadPreprosessert>>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(OMP_UTB_AT_JOURNALFØRING_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(OMP_UTB_AT_JOURNALFØRING_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
): HealthIndicator {

    private companion object {
        private val STREAM_NAME = KafkaStreamName.OMP_UTB_AT_JOURNALFØRING
        private val logger = LoggerFactory.getLogger(OMPUtbetalingATSøknadJournalføring::class.java)
    }

    @Bean
    fun omsorgspengerUtbetalingArbeidstakerJournalføringsStream(): KStream<String, TopicEntry<OMPUtbetalingATSoknadPreprosessert>> {
        val stream = streamsBuilder
            .stream(ompUtbetalingAtPreprosessertTopic.name, ompUtbetalingAtPreprosessertTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .mapValues { _: String, value: TopicEntry<OMPUtbetalingATSoknadPreprosessert> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val preprosessertSøknad = value.data
                    val journalførSøknad = journalføringsService.journalfør(preprosessertSøknad)
                    Cleanup(value.metadata, preprosessertSøknad, journalførSøknad)
                }
            }
            .to(ompUtbetalingAtCleanupTopic.name, ompUtbetalingAtCleanupTopic.producedWith)

        return stream
    }

    override fun health(): Health = HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
