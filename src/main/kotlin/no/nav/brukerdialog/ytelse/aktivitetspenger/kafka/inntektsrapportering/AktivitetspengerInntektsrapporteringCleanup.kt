package no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.inntektsrapportering

import no.nav.brukerdialog.dittnavvarsel.K9Beskjed
import no.nav.brukerdialog.domenetjenester.mottak.CleanupService
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.processors.LoggingToMDCProcessor
import no.nav.brukerdialog.kafka.processors.process
import no.nav.brukerdialog.kafka.types.Cleanup
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.utils.HealthIndicatorUtils
import no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.inntektsrapportering.domene.AktivitetspengerInntektsrapporteringPreprosessert
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
class AktivitetspengerInntektsrapporteringCleanup(
    private val cleanupService: CleanupService<AktivitetspengerInntektsrapporteringPreprosessert>,
    private val aktivitetspengerInntektsrapporteringCleanupTopic: Topic<TopicEntry<Cleanup<AktivitetspengerInntektsrapporteringPreprosessert>>>,
    private val k9DittnavVarselTopic: Topic<TopicEntry<K9Beskjed>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(AktivitetspengerInntektsrapporteringTopologyConfiguration.Companion.AKTIVITETSPENGER_INNTEKTSRAPPORTERING_CLEANUP_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(AktivitetspengerInntektsrapporteringTopologyConfiguration.Companion.AKTIVITETSPENGER_INNTEKTSRAPPORTERING_CLEANUP_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
) : HealthIndicator {

    private companion object {
        private val STREAM_NAME = KafkaStreamName.AKTIVITETSPENGER_INNTEKTSRAPPORTERING_CLEANUP
        private val logger = LoggerFactory.getLogger(AktivitetspengerInntektsrapporteringCleanup::class.java)
    }

    @Bean
    fun aktivitetspengerInntektsrapporteringCleanupStream(): KStream<String, TopicEntry<Cleanup<AktivitetspengerInntektsrapporteringPreprosessert>>> {
        val stream: KStream<String, TopicEntry<Cleanup<AktivitetspengerInntektsrapporteringPreprosessert>>> =
            streamsBuilder.stream(
                aktivitetspengerInntektsrapporteringCleanupTopic.name,
                aktivitetspengerInntektsrapporteringCleanupTopic.consumedWith
            )

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .mapValues { _: String, value: TopicEntry<Cleanup<AktivitetspengerInntektsrapporteringPreprosessert>> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val cleanup = cleanupService.cleanup(value.data)

                    logger.info("Sender K9Beskjed viderer til ${k9DittnavVarselTopic.name}")
                    cleanup.melding.tilK9DittnavVarsel(value.metadata)
                }
            }
            .to(k9DittnavVarselTopic.name, k9DittnavVarselTopic.producedWith)

        return stream
    }

    override fun health(): Health =
        HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
