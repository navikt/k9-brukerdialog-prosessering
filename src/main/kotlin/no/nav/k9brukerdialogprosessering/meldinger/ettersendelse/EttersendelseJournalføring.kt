package no.nav.k9brukerdialogprosessering.meldinger.ettersendelse

import no.nav.k9brukerdialogprosessering.innsending.JournalføringsService
import no.nav.k9brukerdialogprosessering.kafka.config.KafkaStreamName
import no.nav.k9brukerdialogprosessering.kafka.config.Topic
import no.nav.k9brukerdialogprosessering.kafka.processors.LoggingToMDCProcessor
import no.nav.k9brukerdialogprosessering.kafka.processors.process
import no.nav.k9brukerdialogprosessering.kafka.types.Cleanup
import no.nav.k9brukerdialogprosessering.kafka.types.TopicEntry
import no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.EttersendelseTopologyConfiguration.Companion.ETTERSENDELSE_JOURNALFØRING_STREAMS_BUILDER_NAME
import no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.domene.PreprosessertEttersendelse
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
