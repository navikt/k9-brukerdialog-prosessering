package no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.oppgavebekreftelse

import no.nav.brukerdialog.domenetjenester.mottak.JournalføringsService
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.processors.LoggingToMDCProcessor
import no.nav.brukerdialog.kafka.processors.process
import no.nav.brukerdialog.kafka.types.Cleanup
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.utils.HealthIndicatorUtils
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.oppgavebekreftelse.domene.UngdomsytelseOppgavebekreftelsePreprosessert
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.soknad.UngdomsytelsesøknadJournalføring
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
class UngdomsytelseOppgavebekreftelseJournalføring(
    private val journalføringsService: JournalføringsService,
    private val ungdomsytelseOppgavebekreftelsePreprosessertTopic: Topic<TopicEntry<UngdomsytelseOppgavebekreftelsePreprosessert>>,
    private val ungdomsytelseOppgavebekreftelseCleanupTopic: Topic<TopicEntry<Cleanup<UngdomsytelseOppgavebekreftelsePreprosessert>>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(UngdomsytelseOppgavebekreftelseTopologyConfiguration.UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_JOURNALFØRING_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(UngdomsytelseOppgavebekreftelseTopologyConfiguration.UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_JOURNALFØRING_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
) : HealthIndicator {

    private companion object {
        private val STREAM_NAME = KafkaStreamName.UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_JOURNALFØRING
        private val logger = LoggerFactory.getLogger(UngdomsytelsesøknadJournalføring::class.java)
    }

    @Bean
    fun ungdomsytelseOppgavebekreftelseJournalføringsStream(): KStream<String, TopicEntry<UngdomsytelseOppgavebekreftelsePreprosessert>> {
        val stream = streamsBuilder
            .stream(ungdomsytelseOppgavebekreftelsePreprosessertTopic.name, ungdomsytelseOppgavebekreftelsePreprosessertTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .mapValues { _: String, value: TopicEntry<UngdomsytelseOppgavebekreftelsePreprosessert> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val preprosessertSøknad = value.data
                    val journalførSøknad = journalføringsService.journalfør(preprosessertSøknad)
                    Cleanup(value.metadata, preprosessertSøknad, journalførSøknad)
                }
            }
            .to(ungdomsytelseOppgavebekreftelseCleanupTopic.name, ungdomsytelseOppgavebekreftelseCleanupTopic.producedWith)

        return stream
    }

    override fun health(): Health =
        HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
