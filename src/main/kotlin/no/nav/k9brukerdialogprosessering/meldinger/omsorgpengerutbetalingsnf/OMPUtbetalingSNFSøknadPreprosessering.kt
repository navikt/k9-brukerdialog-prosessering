package no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf

import no.nav.k9brukerdialogprosessering.innsending.PreprosesseringsService
import no.nav.k9brukerdialogprosessering.kafka.config.KafkaStreamName
import no.nav.k9brukerdialogprosessering.kafka.config.Topic
import no.nav.k9brukerdialogprosessering.kafka.processors.LoggingToMDCProcessor
import no.nav.k9brukerdialogprosessering.kafka.processors.process
import no.nav.k9brukerdialogprosessering.kafka.types.TopicEntry
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.OMPUtbetalingSNFTopologyConfiguration.Companion.OMP_UTB_SNF_PREPROSESSERING_STREAMS_BUILDER_NAME
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.domene.OMPUtbetalingSNFSoknadMottatt
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.domene.OMPUtbetalingSNFSoknadPreprosessert
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
class OMPUtbetalingSNFSøknadPreprosessering(
    private val preprosesseringsService: PreprosesseringsService,
    private val ompUtbetalingSnfMottattTopic: Topic<TopicEntry<OMPUtbetalingSNFSoknadMottatt>>,
    private val ompUtbetalingSnfPreprosessertTopic: Topic<TopicEntry<OMPUtbetalingSNFSoknadPreprosessert>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(OMP_UTB_SNF_PREPROSESSERING_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(OMP_UTB_SNF_PREPROSESSERING_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
) : HealthIndicator{

    private companion object {
        private val STREAM_NAME = KafkaStreamName.OMP_UTB_SNF_PREPROSESSERING
        private val logger = LoggerFactory.getLogger(OMPUtbetalingSNFSøknadPreprosessering::class.java)
    }

    @Bean
    fun omsorgspengerUtbetalingSnfPreprosesseringsStream(): KStream<String, TopicEntry<OMPUtbetalingSNFSoknadMottatt>> {
        val stream: KStream<String, TopicEntry<OMPUtbetalingSNFSoknadMottatt>> = streamsBuilder
            .stream(ompUtbetalingSnfMottattTopic.name, ompUtbetalingSnfMottattTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor<OMPUtbetalingSNFSoknadMottatt>() })
            .mapValues { _: String, value: TopicEntry<OMPUtbetalingSNFSoknadMottatt> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val mottattSøknad = value.data
                    val preprosesseringsResultat =
                        preprosesseringsService.preprosesser(mottattSøknad.mapTilPreprosesseringsData())

                    mottattSøknad.mapTilPreprosessert(preprosesseringsResultat.dokumenter)
                }
            }
            .to(ompUtbetalingSnfPreprosessertTopic.name, ompUtbetalingSnfPreprosessertTopic.producedWith)
        return stream
    }

    override fun health(): Health = HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
