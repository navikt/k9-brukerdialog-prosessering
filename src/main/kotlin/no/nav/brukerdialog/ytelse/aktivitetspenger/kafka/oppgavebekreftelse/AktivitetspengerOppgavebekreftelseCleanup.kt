package no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.oppgavebekreftelse

import no.nav.brukerdialog.dittnavvarsel.K9Beskjed
import no.nav.brukerdialog.domenetjenester.mottak.CleanupService
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
class AktivitetspengerOppgavebekreftelseCleanup(
    private val cleanupService: CleanupService<AktivitetspengerOppgavebekreftelsePreprosessert>,
    private val aktivitetspengerOppgavebekreftelseCleanupTopic: Topic<TopicEntry<Cleanup<AktivitetspengerOppgavebekreftelsePreprosessert>>>,
    private val k9DittnavVarselTopic: Topic<TopicEntry<K9Beskjed>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(AktivitetspengerOppgavebekreftelseTopologyConfiguration.AKTIVITETSPENGER_OPPGAVEBEKREFTELSE_CLEANUP_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(AktivitetspengerOppgavebekreftelseTopologyConfiguration.AKTIVITETSPENGER_OPPGAVEBEKREFTELSE_CLEANUP_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
) : HealthIndicator {

    private companion object {
        private val STREAM_NAME = KafkaStreamName.AKTIVITETSPENGER_OPPGAVEBEKREFTELSE_CLEANUP
        private val logger = LoggerFactory.getLogger(AktivitetspengerOppgavebekreftelseCleanup::class.java)
    }

    @Bean
    fun aktivitetspengerOppgavebekreftelseCleanupStream(): KStream<String, TopicEntry<Cleanup<AktivitetspengerOppgavebekreftelsePreprosessert>>> {
        val stream: KStream<String, TopicEntry<Cleanup<AktivitetspengerOppgavebekreftelsePreprosessert>>> =
            streamsBuilder.stream(
                aktivitetspengerOppgavebekreftelseCleanupTopic.name,
                aktivitetspengerOppgavebekreftelseCleanupTopic.consumedWith,
            )

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .mapValues { _: String, value: TopicEntry<Cleanup<AktivitetspengerOppgavebekreftelsePreprosessert>> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val cleanup = cleanupService.cleanup(value.data)
                    logger.info("Sender K9Beskjed videre til ${k9DittnavVarselTopic.name}")
                    cleanup.melding.tilK9DittnavVarsel(value.metadata)
                }
            }
            .to(k9DittnavVarselTopic.name, k9DittnavVarselTopic.producedWith)

        return stream
    }

    override fun health(): Health =
        HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
