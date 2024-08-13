package no.nav.brukerdialog.ytelse.ettersendelse.kafka

import no.nav.brukerdialog.domenetjenester.mottak.CleanupService
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.processors.LoggingToMDCProcessor
import no.nav.brukerdialog.kafka.processors.process
import no.nav.brukerdialog.kafka.types.Cleanup
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.ytelse.ettersendelse.kafka.EttersendelseTopologyConfiguration.Companion.ETTERSENDELSE_CLEANUP_STREAMS_BUILDER_NAME
import no.nav.brukerdialog.meldinger.ettersendelse.domene.PreprosessertEttersendelse
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
class EttersendelseCleanup(
    private val cleanupService: CleanupService<PreprosessertEttersendelse>,
    private val endringsmeldingCleanupTopic: Topic<TopicEntry<Cleanup<PreprosessertEttersendelse>>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(ETTERSENDELSE_CLEANUP_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(ETTERSENDELSE_CLEANUP_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean
): HealthIndicator {

    private companion object {
        private val STREAM_NAME = KafkaStreamName.ETTERSENDELSE_CLEANUP
        private val logger = LoggerFactory.getLogger(EttersendelseCleanup::class.java)
    }

    @Bean
    fun ettersendelseCleanupStream(): KStream<String, TopicEntry<Cleanup<PreprosessertEttersendelse>>> {
        val stream: KStream<String, TopicEntry<Cleanup<PreprosessertEttersendelse>>> = streamsBuilder.stream(endringsmeldingCleanupTopic.name, endringsmeldingCleanupTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .mapValues { _: String, value: TopicEntry<Cleanup<PreprosessertEttersendelse>> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    cleanupService.cleanup(value.data)
                }
            }

        return stream
    }

    override fun health(): Health = HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
