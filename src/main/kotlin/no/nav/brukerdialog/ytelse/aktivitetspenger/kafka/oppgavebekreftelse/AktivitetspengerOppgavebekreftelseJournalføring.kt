package no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.oppgavebekreftelse

import no.nav.brukerdialog.domenetjenester.mottak.JournalføringsService
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.processors.LoggingToMDCProcessor
import no.nav.brukerdialog.kafka.processors.process
import no.nav.brukerdialog.kafka.types.Cleanup
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.utils.HealthIndicatorUtils
import no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.oppgavebekreftelse.domene.AktivitetspengerOppgavebekreftelsePreprosessert
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
class AktivitetspengerOppgavebekreftelseJournalføring(
    private val journalføringsService: JournalføringsService,
    private val aktivitetspengerOppgavebekreftelsePreprosessertTopic: Topic<TopicEntry<AktivitetspengerOppgavebekreftelsePreprosessert>>,
    private val aktivitetspengerOppgavebekreftelseCleanupTopic: Topic<TopicEntry<Cleanup<AktivitetspengerOppgavebekreftelsePreprosessert>>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(AktivitetspengerOppgavebekreftelseTopologyConfiguration.AKTIVITETSPENGER_OPPGAVEBEKREFTELSE_JOURNALFØRING_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(AktivitetspengerOppgavebekreftelseTopologyConfiguration.AKTIVITETSPENGER_OPPGAVEBEKREFTELSE_JOURNALFØRING_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
) : HealthIndicator {

    private companion object {
        private val STREAM_NAME = KafkaStreamName.AKTIVITETSPENGER_OPPGAVEBEKREFTELSE_JOURNALFØRING
        private val logger = LoggerFactory.getLogger(AktivitetspengerOppgavebekreftelseJournalføring::class.java)
    }

    @Bean
    fun aktivitetspengerOppgavebekreftelseJournalføringsStream(): KStream<String, TopicEntry<AktivitetspengerOppgavebekreftelsePreprosessert>> {
        val stream = streamsBuilder
            .stream(
                aktivitetspengerOppgavebekreftelsePreprosessertTopic.name,
                aktivitetspengerOppgavebekreftelsePreprosessertTopic.consumedWith,
            )

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .mapValues { _: String, value: TopicEntry<AktivitetspengerOppgavebekreftelsePreprosessert> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val preprosessertSøknad = value.data
                    val journalførSøknad = journalføringsService.journalfør(preprosessertSøknad)
                    Cleanup(value.metadata, preprosessertSøknad, journalførSøknad)
                }
            }
            .to(
                aktivitetspengerOppgavebekreftelseCleanupTopic.name,
                aktivitetspengerOppgavebekreftelseCleanupTopic.producedWith,
            )

        return stream
    }

    override fun health(): Health =
        HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
