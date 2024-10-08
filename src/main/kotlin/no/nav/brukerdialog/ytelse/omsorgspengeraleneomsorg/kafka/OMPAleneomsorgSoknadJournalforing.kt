package no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.kafka

import no.nav.brukerdialog.domenetjenester.mottak.JournalføringsService
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.processors.LoggingToMDCProcessor
import no.nav.brukerdialog.kafka.processors.process
import no.nav.brukerdialog.kafka.types.Cleanup
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.kafka.OMPAleneomsorgTopologyConfiguration.Companion.OMP_AO_JOURNALFØRING_STREAMS_BUILDER_NAME
import no.nav.brukerdialog.meldinger.omsorgspengeraleneomsorg.domene.OMPAleneomsorgSoknadPreprosessert
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
class OMPAleneomsorgSoknadJournalforing(
    private val journalføringsService: JournalføringsService,
    private val ompAleneomsorgPreprosessertTopic: Topic<TopicEntry<OMPAleneomsorgSoknadPreprosessert>>,
    private val ompAleneomsorgCleanupTopic: Topic<TopicEntry<Cleanup<OMPAleneomsorgSoknadPreprosessert>>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(OMP_AO_JOURNALFØRING_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(OMP_AO_JOURNALFØRING_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
): HealthIndicator {

    private companion object {
        private val STREAM_NAME = KafkaStreamName.OMP_AO_JOURNALFØRING
        private val logger = LoggerFactory.getLogger(OMPAleneomsorgSoknadJournalforing::class.java)
    }

    @Bean
    fun omsorgspengerAleneomsorgJournalføringsStream(): KStream<String, TopicEntry<OMPAleneomsorgSoknadPreprosessert>> {
        val stream = streamsBuilder
            .stream(ompAleneomsorgPreprosessertTopic.name, ompAleneomsorgPreprosessertTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .mapValues { _: String, value: TopicEntry<OMPAleneomsorgSoknadPreprosessert> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val preprosessertSøknad = value.data
                    val journalførSøknad = journalføringsService.journalfør(preprosessertSøknad)
                    Cleanup(value.metadata, preprosessertSøknad, journalførSøknad)
                }
            }
            .to(ompAleneomsorgCleanupTopic.name, ompAleneomsorgCleanupTopic.producedWith)

        return stream
    }

    override fun health(): Health = HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
