package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.kafka

import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.kafka.PSBEndringsmeldingTopologyConfiguration.Companion.PSB_ENDRINGSMELDING_CLEANUP_STREAMS_BUILDER_NAME
import no.nav.brukerdialog.meldinger.endringsmelding.domene.PSBPreprossesertEndringsmelding
import no.nav.brukerdialog.domenetjenester.mottak.CleanupService
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.processors.LoggingToMDCProcessor
import no.nav.brukerdialog.kafka.processors.process
import no.nav.brukerdialog.kafka.types.Cleanup
import no.nav.brukerdialog.kafka.types.TopicEntry
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