package no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.k9brukerdialogprosessering.dittnavvarsel.K9Beskjed
import no.nav.k9brukerdialogprosessering.innsending.CleanupService
import no.nav.k9brukerdialogprosessering.kafka.config.KafkaStreamName
import no.nav.k9brukerdialogprosessering.kafka.config.Topic
import no.nav.k9brukerdialogprosessering.kafka.processors.LoggingToMDCProcessor
import no.nav.k9brukerdialogprosessering.kafka.processors.process
import no.nav.k9brukerdialogprosessering.kafka.types.Cleanup
import no.nav.k9brukerdialogprosessering.kafka.types.TopicEntry
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn.OMPKSTopologyConfiguration.Companion.OMP_UTV_KS_CLEANUP_STREAMS_BUILDER_NAME
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

                   cleanup.melding.tilK9DittnavVarsel(value.metadata)
                }
            }
            .to(k9DittnavVarselTopic.name, k9DittnavVarselTopic.producedWith)

        return stream
    }

    override fun health(): Health = HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
