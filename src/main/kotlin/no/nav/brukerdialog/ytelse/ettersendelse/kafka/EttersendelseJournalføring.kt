package no.nav.brukerdialog.ytelse.ettersendelse.kafka

import no.nav.brukerdialog.domenetjenester.mottak.JournalføringsService
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.processors.LoggingToMDCProcessor
import no.nav.brukerdialog.kafka.processors.process
import no.nav.brukerdialog.kafka.types.Cleanup
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.meldinger.ettersendelse.domene.PreprosessertEttersendelse
import no.nav.brukerdialog.utils.HealthIndicatorUtils
import no.nav.brukerdialog.ytelse.ettersendelse.kafka.EttersendelseTopologyConfiguration.Companion.ETTERSENDELSE_JOURNALFØRING_STREAMS_BUILDER_NAME
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
class EttersendelseJournalføring(
    private val journalføringsService: JournalføringsService,
    private val ettersendelsePreprosesertTopic: Topic<TopicEntry<PreprosessertEttersendelse>>,
    private val ettersendelseCleanupTopic: Topic<TopicEntry<Cleanup<PreprosessertEttersendelse>>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(ETTERSENDELSE_JOURNALFØRING_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(ETTERSENDELSE_JOURNALFØRING_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
): HealthIndicator {

    private companion object {
        private val STREAM_NAME = KafkaStreamName.ETTERSENDELSE_JOURNALFØRING
        private val logger = LoggerFactory.getLogger(EttersendelseJournalføring::class.java)
    }

    @Bean
    fun ettersendelseJournalføringsStream(): KStream<String, TopicEntry<PreprosessertEttersendelse>> {
        val stream = streamsBuilder
            .stream(ettersendelsePreprosesertTopic.name, ettersendelsePreprosesertTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .mapValues { _: String, value: TopicEntry<PreprosessertEttersendelse> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val preprosessertSøknad: PreprosessertEttersendelse = value.data
                    val journalførSøknad = journalføringsService.journalfør(preprosessertSøknad)
                    Cleanup(value.metadata, preprosessertSøknad, journalførSøknad)
                }
            }
            .to(ettersendelseCleanupTopic.name, ettersendelseCleanupTopic.producedWith)

        return stream
    }

    override fun health(): Health = HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
