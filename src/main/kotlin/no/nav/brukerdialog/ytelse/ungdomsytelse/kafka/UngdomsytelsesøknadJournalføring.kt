package no.nav.brukerdialog.ytelse.ungdomsytelse.kafka

import no.nav.brukerdialog.domenetjenester.mottak.JournalføringsService
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.processors.LoggingToMDCProcessor
import no.nav.brukerdialog.kafka.processors.process
import no.nav.brukerdialog.kafka.types.Cleanup
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.utils.HealthIndicatorUtils
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.UngdomsytelsesøknadTopologyConfiguration.Companion.UNGDOMSYTELSE_SØKNAD_JOURNALFØRING_STREAMS_BUILDER_NAME
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.domene.UngdomsytelsesøknadPreprosessertSøknad
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
class UngdomsytelsesøknadJournalføring(
    private val journalføringsService: JournalføringsService,
    private val ungdomsytelsesøknadPreprosessertTopic: Topic<TopicEntry<UngdomsytelsesøknadPreprosessertSøknad>>,
    private val ungdomsytelsesøknadCleanupTopic: Topic<TopicEntry<Cleanup<UngdomsytelsesøknadPreprosessertSøknad>>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(UNGDOMSYTELSE_SØKNAD_JOURNALFØRING_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(UNGDOMSYTELSE_SØKNAD_JOURNALFØRING_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
) : HealthIndicator {

    private companion object {
        private val STREAM_NAME = KafkaStreamName.UNGDOMSYTELSE_SØKNAD_JOURNALFØRING
        private val logger = LoggerFactory.getLogger(UngdomsytelsesøknadJournalføring::class.java)
    }

    @Bean
    fun ungdomsytelsesøknadJournalføringsStream(): KStream<String, TopicEntry<UngdomsytelsesøknadPreprosessertSøknad>> {
        val stream = streamsBuilder
            .stream(ungdomsytelsesøknadPreprosessertTopic.name, ungdomsytelsesøknadPreprosessertTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .mapValues { _: String, value: TopicEntry<UngdomsytelsesøknadPreprosessertSøknad> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val preprosessertSøknad = value.data
                    val journalførSøknad = journalføringsService.journalfør(preprosessertSøknad)
                    Cleanup(value.metadata, preprosessertSøknad, journalførSøknad)
                }
            }
            .to(ungdomsytelsesøknadCleanupTopic.name, ungdomsytelsesøknadCleanupTopic.producedWith)

        return stream
    }

    override fun health(): Health =
        HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
