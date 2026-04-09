package no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.inntektsrapportering

import no.nav.brukerdialog.domenetjenester.mottak.JournalføringsService
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
class AktivitetspengerInntektsrapporteringJournalføring(
    private val journalføringsService: JournalføringsService,
    private val aktivitetspengerInntektsrapporteringPreprosessertTopic: Topic<TopicEntry<AktivitetspengerInntektsrapporteringPreprosessert>>,
    private val aktivitetspengerInntektsrapporteringCleanupTopic: Topic<TopicEntry<Cleanup<AktivitetspengerInntektsrapporteringPreprosessert>>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(AktivitetspengerInntektsrapporteringTopologyConfiguration.Companion.AKTIVITETSPENGER_INNTEKTSRAPPORTERING_JOURNALFØRING_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(AktivitetspengerInntektsrapporteringTopologyConfiguration.Companion.AKTIVITETSPENGER_INNTEKTSRAPPORTERING_JOURNALFØRING_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
) : HealthIndicator {

    private companion object {
        private val STREAM_NAME = KafkaStreamName.AKTIVITETSPENGER_INNTEKTSRAPPORTERING_JOURNALFØRING
        private val logger = LoggerFactory.getLogger(AktivitetspengerInntektsrapporteringJournalføring::class.java)
    }

    @Bean
    fun aktivitetspengerInntektsrapporteringStream(): KStream<String, TopicEntry<AktivitetspengerInntektsrapporteringPreprosessert>> {
        val stream = streamsBuilder
            .stream(
                aktivitetspengerInntektsrapporteringPreprosessertTopic.name,
                aktivitetspengerInntektsrapporteringPreprosessertTopic.consumedWith
            )

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .mapValues { _: String, value: TopicEntry<AktivitetspengerInntektsrapporteringPreprosessert> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val preprosessertSøknad = value.data
                    val journalførSøknad = journalføringsService.journalfør(preprosessertSøknad)
                    Cleanup(value.metadata, preprosessertSøknad, journalførSøknad)
                }
            }
            .to(
                aktivitetspengerInntektsrapporteringCleanupTopic.name,
                aktivitetspengerInntektsrapporteringCleanupTopic.producedWith
            )

        return stream
    }

    override fun health(): Health =
        HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
