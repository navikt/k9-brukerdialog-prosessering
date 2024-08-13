package no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.kafka

import no.nav.brukerdialog.domenetjenester.mottak.JournalføringsService
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.processors.LoggingToMDCProcessor
import no.nav.brukerdialog.kafka.processors.process
import no.nav.brukerdialog.kafka.types.Cleanup
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.kafka.PILSTopologyConfiguration.Companion.PILS_JOURNALFØRING_STREAMS_BUILDER_NAME
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.PilsPreprosessertSøknad
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
class PleiepengerILivetsSluttfaseSøknadJournalføring(
    private val journalføringsService: JournalføringsService,
    private val pilsPreprosesertTopic: Topic<TopicEntry<PilsPreprosessertSøknad>>,
    private val pilsCleanupTopic: Topic<TopicEntry<Cleanup<PilsPreprosessertSøknad>>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(PILS_JOURNALFØRING_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(PILS_JOURNALFØRING_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
): HealthIndicator {

    private companion object {
        private val STREAM_NAME = KafkaStreamName.PILS_SØKNAD_JOURNALFØRING
        private val logger = LoggerFactory.getLogger(PleiepengerILivetsSluttfaseSøknadJournalføring::class.java)
    }

    @Bean
    fun pleiepengerILIvetsSluttfaseJournalføringsStream(): KStream<String, TopicEntry<PilsPreprosessertSøknad>> {
        val stream = streamsBuilder
            .stream(pilsPreprosesertTopic.name, pilsPreprosesertTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .mapValues { _: String, value: TopicEntry<PilsPreprosessertSøknad> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val preprosessertSøknad = value.data
                    val journalførSøknad = journalføringsService.journalfør(preprosessertSøknad)
                    Cleanup(value.metadata, preprosessertSøknad, journalførSøknad)
                }
            }
            .to(pilsCleanupTopic.name, pilsCleanupTopic.producedWith)

        return stream
    }

    override fun health(): Health = HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
