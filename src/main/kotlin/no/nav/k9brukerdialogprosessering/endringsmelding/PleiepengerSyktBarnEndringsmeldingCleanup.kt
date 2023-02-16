package no.nav.k9brukerdialogprosessering.endringsmelding

import no.nav.k9brukerdialogprosessering.endringsmelding.PSBEndringsmeldingTopologyConfiguration.Companion.PSB_ENDRINGSMELDING_CLEANUP_STREAMS_BUILDER_NAME
import no.nav.k9brukerdialogprosessering.endringsmelding.domene.PSBPreprossesertEndringsmelding
import no.nav.k9brukerdialogprosessering.innsending.CleanupService
import no.nav.k9brukerdialogprosessering.kafka.config.KafkaStreamName
import no.nav.k9brukerdialogprosessering.kafka.config.Topic
import no.nav.k9brukerdialogprosessering.kafka.processors.LoggingToMDCProcessor
import no.nav.k9brukerdialogprosessering.kafka.processors.process
import no.nav.k9brukerdialogprosessering.kafka.types.Cleanup
import no.nav.k9brukerdialogprosessering.kafka.types.TopicEntry
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
class PleiepengerSyktBarnEndringsmeldingCleanup(
    private val cleanupService: CleanupService<PSBPreprossesertEndringsmelding>,
    private val psbEndringsmeldingCleanupTopic: Topic<TopicEntry<Cleanup<PSBPreprossesertEndringsmelding>>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(PSB_ENDRINGSMELDING_CLEANUP_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(PSB_ENDRINGSMELDING_CLEANUP_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean
) : HealthIndicator {

    private companion object {
        private val STREAM_NAME = KafkaStreamName.PSB_ENDRINGSMELDING_CLEANUP
        private val IGNORE_LIST = listOf<String>()
        private val logger = LoggerFactory.getLogger(PleiepengerSyktBarnEndringsmeldingCleanup::class.java)
    }

    @Bean
    fun pleiepengerSyktBarnEndringsmeldingCleanupStream(): KStream<String, TopicEntry<Cleanup<PSBPreprossesertEndringsmelding>>> {
        val stream: KStream<String, TopicEntry<Cleanup<PSBPreprossesertEndringsmelding>>> =
            streamsBuilder.stream(psbEndringsmeldingCleanupTopic.name, psbEndringsmeldingCleanupTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .filterNot { _, entry -> IGNORE_LIST.contains(entry.metadata.correlationId) }
            .mapValues { _: String, value: TopicEntry<Cleanup<PSBPreprossesertEndringsmelding>> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    cleanupService.cleanup(value.data)
                }
            }

        return stream
    }

    override fun health(): Health =
        HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
