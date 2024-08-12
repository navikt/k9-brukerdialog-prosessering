package no.nav.brukerdialog.ytelse.omsorgspengermidlertidigalene.kafka

import no.nav.brukerdialog.innsending.JournalføringsService
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.processors.LoggingToMDCProcessor
import no.nav.brukerdialog.kafka.processors.process
import no.nav.brukerdialog.kafka.types.Cleanup
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.ytelse.omsorgspengermidlertidigalene.kafka.OMPMidlertidigAleneTopologyConfiguration.Companion.OMP_MA_JOURNALFØRING_STREAMS_BUILDER_NAME
import no.nav.brukerdialog.meldinger.omsorgspengermidlertidigalene.domene.OMPMidlertidigAleneSoknadPreprosessert
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
class OMPMidlertidigAleneSoknadJournalforing(
    private val journalføringsService: JournalføringsService,
    private val ompMidlertidigAlenePreprosessertTopic: Topic<TopicEntry<OMPMidlertidigAleneSoknadPreprosessert>>,
    private val ompMidlertidigAleneCleanupTopic: Topic<TopicEntry<Cleanup<OMPMidlertidigAleneSoknadPreprosessert>>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(OMP_MA_JOURNALFØRING_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(OMP_MA_JOURNALFØRING_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
): HealthIndicator {

    private companion object {
        private val STREAM_NAME = KafkaStreamName.OMP_MA_JOURNALFØRING
        private val logger = LoggerFactory.getLogger(OMPMidlertidigAleneSoknadJournalforing::class.java)
    }

    @Bean
    fun omsorgspengerMidlertidigAleneJournalføringsStream(): KStream<String, TopicEntry<OMPMidlertidigAleneSoknadPreprosessert>> {
        val stream = streamsBuilder
            .stream(ompMidlertidigAlenePreprosessertTopic.name, ompMidlertidigAlenePreprosessertTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .mapValues { _: String, value: TopicEntry<OMPMidlertidigAleneSoknadPreprosessert> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val preprosessertSøknad = value.data
                    val journalførSøknad = journalføringsService.journalfør(preprosessertSøknad)
                    Cleanup(value.metadata, preprosessertSøknad, journalførSøknad)
                }
            }
            .to(ompMidlertidigAleneCleanupTopic.name, ompMidlertidigAleneCleanupTopic.producedWith)

        return stream
    }

    override fun health(): Health = HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
