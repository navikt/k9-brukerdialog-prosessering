package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.kafka

import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.kafka.PSBEndringsmeldingTopologyConfiguration.Companion.PSB_ENDRINGSMELDING_JOURNALFØRING_STREAMS_BUILDER_NAME
import no.nav.brukerdialog.meldinger.endringsmelding.domene.PSBPreprossesertEndringsmelding
import no.nav.brukerdialog.innsending.JournalføringsService
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
class PleiepengerSyktBarnEndringsmeldingJournalføring(
    private val journalføringsService: JournalføringsService,
    private val psbEndringsmeldingPreprosesertTopic: Topic<TopicEntry<PSBPreprossesertEndringsmelding>>,
    private val psbEndringsmeldingCleanupTopic: Topic<TopicEntry<Cleanup<PSBPreprossesertEndringsmelding>>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(PSB_ENDRINGSMELDING_JOURNALFØRING_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(PSB_ENDRINGSMELDING_JOURNALFØRING_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean
) : HealthIndicator {

    private companion object {
        private val STREAM_NAME = KafkaStreamName.PSB_ENDRINGSMELDING_JOURNALFØRING
        private val IGNORE_LIST = listOf<String>()
        private val logger = LoggerFactory.getLogger(PleiepengerSyktBarnEndringsmeldingJournalføring::class.java)
    }

    @Bean
    fun pleiepengerSyktBarnEndringsmeldingJournalføringsStream(): KStream<String, TopicEntry<PSBPreprossesertEndringsmelding>> {
        val stream = streamsBuilder
            .stream(psbEndringsmeldingPreprosesertTopic.name, psbEndringsmeldingPreprosesertTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .filterNot { _, entry -> IGNORE_LIST.contains(entry.metadata.correlationId) }
            .mapValues { _: String, value: TopicEntry<PSBPreprossesertEndringsmelding> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val preprosessertSøknad: PSBPreprossesertEndringsmelding = value.data
                    val journalførSøknad = journalføringsService.journalfør(preprosessertSøknad)
                    Cleanup(value.metadata, preprosessertSøknad, journalførSøknad)
                }
            }
            .to(psbEndringsmeldingCleanupTopic.name, psbEndringsmeldingCleanupTopic.producedWith)

        return stream
    }

    override fun health(): Health =
        HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
