package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.kafka

import no.nav.brukerdialog.domenetjenester.mottak.PreprosesseringsService
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.processors.LoggingToMDCProcessor
import no.nav.brukerdialog.kafka.processors.process
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.meldinger.endringsmelding.domene.PSBEndringsmeldingMottatt
import no.nav.brukerdialog.utils.HealthIndicatorUtils
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.kafka.PSBEndringsmeldingTopologyConfiguration.Companion.PSB_ENDRINGSMELDING_PREPROSESSERING_STREAMS_BUILDER_NAME
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.kafka.domene.PSBPreprossesertEndringsmelding
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
class PleiepengerSyktBarnEndringsmeldingPreprossering(
    private val preprosesseringsService: PreprosesseringsService,
    private val psbEndringsmeldingMottattTopic: Topic<TopicEntry<PSBEndringsmeldingMottatt>>,
    private val psbEndringsmeldingPreprosesertTopic: Topic<TopicEntry<PSBPreprossesertEndringsmelding>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(PSB_ENDRINGSMELDING_PREPROSESSERING_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(PSB_ENDRINGSMELDING_PREPROSESSERING_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean
) : HealthIndicator {

    private companion object {
        private val STREAM_NAME = KafkaStreamName.PSB_ENDRINGSMELDING_PREPROSESSERING
        private val IGNORE_LIST = listOf<String>()
        private val logger = LoggerFactory.getLogger(PleiepengerSyktBarnEndringsmeldingPreprossering::class.java)
    }

    @Bean
    fun pleiepengerSyktBarnEndringsmeldingPreprosesseringsStream(): KStream<String, TopicEntry<PSBEndringsmeldingMottatt>> {
        val stream: KStream<String, TopicEntry<PSBEndringsmeldingMottatt>> = streamsBuilder
            .stream(psbEndringsmeldingMottattTopic.name, psbEndringsmeldingMottattTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor<PSBEndringsmeldingMottatt>() })
            .filterNot { _, entry -> IGNORE_LIST.contains(entry.metadata.correlationId) }
            .mapValues { _: String, value: TopicEntry<PSBEndringsmeldingMottatt> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val psbEndringsmeldingMottatt = value.data
                    val preprosesseringsResultat =
                        preprosesseringsService.preprosesser(psbEndringsmeldingMottatt.mapTilPreprosesseringsData())

                    psbEndringsmeldingMottatt.mapTilPreprosessert(preprosesseringsResultat.dokumenter)
                }
            }
            .to(psbEndringsmeldingPreprosesertTopic.name, psbEndringsmeldingPreprosesertTopic.producedWith)
        return stream
    }

    override fun health(): Health =
        HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
