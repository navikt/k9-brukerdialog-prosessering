package no.nav.brukerdialog.ytelse.omsorgspengermidlertidigalene.kafka

import no.nav.brukerdialog.domenetjenester.mottak.PreprosesseringsService
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.processors.LoggingToMDCProcessor
import no.nav.brukerdialog.kafka.processors.process
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.ytelse.omsorgspengermidlertidigalene.kafka.OMPMidlertidigAleneTopologyConfiguration.Companion.OMP_MA_PREPROSESSERING_STREAMS_BUILDER_NAME
import no.nav.brukerdialog.meldinger.omsorgspengermidlertidigalene.domene.OMPMidlertidigAleneSoknadMottatt
import no.nav.brukerdialog.meldinger.omsorgspengermidlertidigalene.domene.OMPMidlertidigAleneSoknadPreprosessert
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
class OMPMidlertidigAleneSoknadPreprosessering(
    private val preprosesseringsService: PreprosesseringsService,
    private val ompMidlertidigAleneMottattTopic: Topic<TopicEntry<OMPMidlertidigAleneSoknadMottatt>>,
    private val ompMidlertidigAlenePreprosessertTopic: Topic<TopicEntry<OMPMidlertidigAleneSoknadPreprosessert>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(OMP_MA_PREPROSESSERING_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(OMP_MA_PREPROSESSERING_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
) : HealthIndicator{

    private companion object {
        private val STREAM_NAME = KafkaStreamName.OMP_MA_PREPROSESSERING
        private val logger = LoggerFactory.getLogger(OMPMidlertidigAleneSoknadPreprosessering::class.java)
    }

    @Bean
    fun ompMidlertidigAlenePreprosesseringsStream(): KStream<String, TopicEntry<OMPMidlertidigAleneSoknadMottatt>> {
        val stream: KStream<String, TopicEntry<OMPMidlertidigAleneSoknadMottatt>> = streamsBuilder
            .stream(ompMidlertidigAleneMottattTopic.name, ompMidlertidigAleneMottattTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor<OMPMidlertidigAleneSoknadMottatt>() })
            .mapValues { _: String, value: TopicEntry<OMPMidlertidigAleneSoknadMottatt> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val mottattSøknad = value.data
                    val preprosesseringsResultat =
                        preprosesseringsService.preprosesser(mottattSøknad.mapTilPreprosesseringsData())

                    mottattSøknad.mapTilPreprosessert(preprosesseringsResultat.dokumenter)
                }
            }
            .to(ompMidlertidigAlenePreprosessertTopic.name, ompMidlertidigAlenePreprosessertTopic.producedWith)
        return stream
    }

    override fun health(): Health = HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
