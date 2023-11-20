package no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn

import no.nav.k9brukerdialogprosessering.innsending.JournalføringsService
import no.nav.k9brukerdialogprosessering.kafka.config.KafkaStreamName
import no.nav.k9brukerdialogprosessering.kafka.config.Topic
import no.nav.k9brukerdialogprosessering.kafka.processors.LoggingToMDCProcessor
import no.nav.k9brukerdialogprosessering.kafka.processors.process
import no.nav.k9brukerdialogprosessering.kafka.types.Cleanup
import no.nav.k9brukerdialogprosessering.kafka.types.TopicEntry
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn.OMPKSTopologyConfiguration.Companion.OMP_UTV_KS_JOURNALFØRING_STREAMS_BUILDER_NAME
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn.domene.OMPUTVKroniskSyktBarnSøknadPreprosesssert
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
class OmsorgspengerKroniskSyktBarnSøknadJournalføring(
    private val journalføringsService: JournalføringsService,
    private val ompUtvKsPreprosessertTopic: Topic<TopicEntry<OMPUTVKroniskSyktBarnSøknadPreprosesssert>>,
    private val ompUtvKsCleanupTopic: Topic<TopicEntry<Cleanup<OMPUTVKroniskSyktBarnSøknadPreprosesssert>>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(OMP_UTV_KS_JOURNALFØRING_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(OMP_UTV_KS_JOURNALFØRING_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
): HealthIndicator {

    private companion object {
        private val STREAM_NAME = KafkaStreamName.OMP_UTV_KS_JOURNALFØRING
        private val logger = LoggerFactory.getLogger(OmsorgspengerKroniskSyktBarnSøknadJournalføring::class.java)
    }

    @Bean
    fun omsorgspengerKroniskSyktBarnJournalføringsStream(): KStream<String, TopicEntry<OMPUTVKroniskSyktBarnSøknadPreprosesssert>> {
        val stream = streamsBuilder
            .stream(ompUtvKsPreprosessertTopic.name, ompUtvKsPreprosessertTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .mapValues { _: String, value: TopicEntry<OMPUTVKroniskSyktBarnSøknadPreprosesssert> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val preprosessertSøknad: OMPUTVKroniskSyktBarnSøknadPreprosesssert = value.data
                    val journalførSøknad = journalføringsService.journalfør(
                        preprosessertSøknad,
                        value.metadata.correlationId
                    )
                    Cleanup(value.metadata, preprosessertSøknad, journalførSøknad)
                }
            }
            .to(ompUtvKsCleanupTopic.name, ompUtvKsCleanupTopic.producedWith)

        return stream
    }

    override fun health(): Health = HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
