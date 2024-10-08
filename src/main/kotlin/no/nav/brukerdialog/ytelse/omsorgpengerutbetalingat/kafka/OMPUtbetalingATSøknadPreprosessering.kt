package no.nav.brukerdialog.ytelse.omsorgpengerutbetalingat.kafka

import no.nav.brukerdialog.domenetjenester.mottak.PreprosesseringsService
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.processors.LoggingToMDCProcessor
import no.nav.brukerdialog.kafka.processors.process
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingat.kafka.OMPUtbetalingATTopologyConfiguration.Companion.OMP_UTB_AT_PREPROSESSERING_STREAMS_BUILDER_NAME
import no.nav.brukerdialog.meldinger.omsorgpengerutbetalingat.domene.OMPUtbetalingATSoknadMottatt
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingat.kafka.domene.OMPUtbetalingATSoknadPreprosessert
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
class OMPUtbetalingATSøknadPreprosessering(
    private val preprosesseringsService: PreprosesseringsService,
    private val ompUtbetalingAtMottattTopic: Topic<TopicEntry<OMPUtbetalingATSoknadMottatt>>,
    private val ompUtbetalingAtPreprosessertTopic: Topic<TopicEntry<OMPUtbetalingATSoknadPreprosessert>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(OMP_UTB_AT_PREPROSESSERING_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(OMP_UTB_AT_PREPROSESSERING_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
) : HealthIndicator{

    private companion object {
        private val STREAM_NAME = KafkaStreamName.OMP_UTB_AT_PREPROSESSERING
        private val logger = LoggerFactory.getLogger(OMPUtbetalingATSøknadPreprosessering::class.java)
    }

    @Bean
    fun omsorgspengerUtbetalingArbeidstakerPreprosesseringsStream(): KStream<String, TopicEntry<OMPUtbetalingATSoknadMottatt>> {
        val stream: KStream<String, TopicEntry<OMPUtbetalingATSoknadMottatt>> = streamsBuilder
            .stream(ompUtbetalingAtMottattTopic.name, ompUtbetalingAtMottattTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor<OMPUtbetalingATSoknadMottatt>() })
            .mapValues { _: String, value: TopicEntry<OMPUtbetalingATSoknadMottatt> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val mottattSøknad = value.data
                    val preprosesseringsResultat =
                        preprosesseringsService.preprosesser(mottattSøknad.mapTilPreprosesseringsData())

                    mottattSøknad.mapTilPreprosessert(preprosesseringsResultat.dokumenter)
                }
            }
            .to(ompUtbetalingAtPreprosessertTopic.name, ompUtbetalingAtPreprosessertTopic.producedWith)
        return stream
    }

    override fun health(): Health = HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
