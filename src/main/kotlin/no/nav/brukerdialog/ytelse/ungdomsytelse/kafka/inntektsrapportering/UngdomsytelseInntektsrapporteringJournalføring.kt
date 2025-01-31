package no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.inntektsrapportering

import no.nav.brukerdialog.domenetjenester.mottak.JournalføringsService
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.processors.LoggingToMDCProcessor
import no.nav.brukerdialog.kafka.processors.process
import no.nav.brukerdialog.kafka.types.Cleanup
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.utils.HealthIndicatorUtils
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.inntektsrapportering.domene.UngdomsytelseInntektsrapporteringPreprosessert
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
class UngdomsytelseInntektsrapporteringJournalføring(
    private val journalføringsService: JournalføringsService,
    private val ungdomsytelseInntektsrapporteringPreprosessertTopic: Topic<TopicEntry<UngdomsytelseInntektsrapporteringPreprosessert>>,
    private val ungdomsytelseInntektsrapporteringCleanupTopic: Topic<TopicEntry<Cleanup<UngdomsytelseInntektsrapporteringPreprosessert>>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(UngdomsytelseInntektsrapporteringTopologyConfiguration.Companion.UNGDOMSYTELSE_INNTEKTSRAPPORTERING_JOURNALFØRING_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(UngdomsytelseInntektsrapporteringTopologyConfiguration.Companion.UNGDOMSYTELSE_INNTEKTSRAPPORTERING_JOURNALFØRING_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
) : HealthIndicator {

    private companion object {
        private val STREAM_NAME = KafkaStreamName.UNGDOMSYTELSE_INNTEKTSRAPPORTERING_JOURNALFØRING
        private val logger = LoggerFactory.getLogger(UngdomsytelseInntektsrapporteringJournalføring::class.java)
    }

    @Bean
    fun ungdomsytelseInntektsrapporteringStream(): KStream<String, TopicEntry<UngdomsytelseInntektsrapporteringPreprosessert>> {
        val stream = streamsBuilder
            .stream(
                ungdomsytelseInntektsrapporteringPreprosessertTopic.name,
                ungdomsytelseInntektsrapporteringPreprosessertTopic.consumedWith
            )

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .mapValues { _: String, value: TopicEntry<UngdomsytelseInntektsrapporteringPreprosessert> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val preprosessertSøknad = value.data
                    val journalførSøknad = journalføringsService.journalfør(preprosessertSøknad)
                    Cleanup(value.metadata, preprosessertSøknad, journalførSøknad)
                }
            }
            .to(
                ungdomsytelseInntektsrapporteringCleanupTopic.name,
                ungdomsytelseInntektsrapporteringCleanupTopic.producedWith
            )

        return stream
    }

    override fun health(): Health =
        HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
