package no.nav.brukerdialog.meldinger.omsorgspengerkronisksyktbarn

import no.nav.brukerdialog.dittnavvarsel.K9Beskjed
import no.nav.brukerdialog.innsending.CleanupService
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.processors.LoggingToMDCProcessor
import no.nav.brukerdialog.kafka.processors.process
import no.nav.brukerdialog.kafka.types.Cleanup
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.meldinger.omsorgspengerkronisksyktbarn.OMPKSTopologyConfiguration.Companion.OMP_UTV_KS_CLEANUP_STREAMS_BUILDER_NAME
import no.nav.brukerdialog.meldinger.omsorgspengerkronisksyktbarn.domene.OMPUTVKroniskSyktBarnSøknadPreprosesssert
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
class OmsorgspengerKroniskSyktBarnSøknadCleanup(
    private val cleanupService: CleanupService<OMPUTVKroniskSyktBarnSøknadPreprosesssert>,
    private val ompUtvKsCleanupTopic: Topic<TopicEntry<Cleanup<OMPUTVKroniskSyktBarnSøknadPreprosesssert>>>,
    private val k9DittnavVarselTopic: Topic<TopicEntry<K9Beskjed>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(OMP_UTV_KS_CLEANUP_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(OMP_UTV_KS_CLEANUP_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean
): HealthIndicator {

    private companion object {
        private val STREAM_NAME = KafkaStreamName.OMP_UTV_KS_CLEANUP
        private val logger = LoggerFactory.getLogger(OmsorgspengerKroniskSyktBarnSøknadCleanup::class.java)
    }

    @Bean
    fun omsorgspengerKroniskSyktBarnCleanupStream(): KStream<String, TopicEntry<Cleanup<OMPUTVKroniskSyktBarnSøknadPreprosesssert>>> {
        val stream: KStream<String, TopicEntry<Cleanup<OMPUTVKroniskSyktBarnSøknadPreprosesssert>>> = streamsBuilder.stream(ompUtvKsCleanupTopic.name, ompUtvKsCleanupTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .mapValues { _: String, value: TopicEntry<Cleanup<OMPUTVKroniskSyktBarnSøknadPreprosesssert>> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val cleanup = cleanupService.cleanup(value.data)

                    logger.info("Sender K9Beskjed viderer til ${k9DittnavVarselTopic.name}")
                   cleanup.melding.tilK9DittnavVarsel(value.metadata)
                }
            }
            .to(k9DittnavVarselTopic.name, k9DittnavVarselTopic.producedWith)

        return stream
    }

    override fun health(): Health = HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
