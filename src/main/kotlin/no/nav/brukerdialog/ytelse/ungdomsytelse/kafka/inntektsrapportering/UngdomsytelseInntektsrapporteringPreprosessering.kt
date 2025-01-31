package no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.inntektsrapportering

import no.nav.brukerdialog.domenetjenester.mottak.PreprosesseringsService
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.processors.LoggingToMDCProcessor
import no.nav.brukerdialog.kafka.processors.process
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.utils.HealthIndicatorUtils
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.inntektsrapportering.domene.UngdomsytelseInntektsrapporteringMottatt
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.inntektsrapportering.domene.UngdomsytelseInntektsrapporteringPreprosessert
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
class UngdomsytelseInntektsrapporteringPreprosessering(
    private val preprosesseringsService: PreprosesseringsService,
    private val ungdomsytelseInntektsrapporteringMottattTopic: Topic<TopicEntry<UngdomsytelseInntektsrapporteringMottatt>>,
    private val ungdomsytelseInntektsrapporteringPreprosessertTopic: Topic<TopicEntry<UngdomsytelseInntektsrapporteringPreprosessert>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(UngdomsytelseInntektsrapporteringTopologyConfiguration.Companion.UNGDOMSYTELSE_INNTEKTSRAPPORTERING_PREPROSESSERING_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(UngdomsytelseInntektsrapporteringTopologyConfiguration.Companion.UNGDOMSYTELSE_INNTEKTSRAPPORTERING_PREPROSESSERING_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
) : HealthIndicator {

    private companion object {
        private val STREAM_NAME = KafkaStreamName.UNGDOMSYTELSE_INNTEKTSRAPPORTERING_PREPROSESSERING
        private val logger = LoggerFactory.getLogger(UngdomsytelseInntektsrapporteringPreprosessering::class.java)
    }

    @Bean
    fun ungdomsytelseInntektsrapporteringPreprosesseringsStream(): KStream<String, TopicEntry<UngdomsytelseInntektsrapporteringMottatt>> {
        val stream: KStream<String, TopicEntry<UngdomsytelseInntektsrapporteringMottatt>> = streamsBuilder
            .stream(
                ungdomsytelseInntektsrapporteringMottattTopic.name,
                ungdomsytelseInntektsrapporteringMottattTopic.consumedWith
            )

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor<UngdomsytelseInntektsrapporteringMottatt>() })
            .mapValues { _: String, value: TopicEntry<UngdomsytelseInntektsrapporteringMottatt> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val søknad = value.data
                    val preprosesseringsResultat =
                        preprosesseringsService.preprosesser(søknad.mapTilPreprosesseringsData())

                    søknad.mapTilPreprosessert(preprosesseringsResultat.dokumenter)
                }
            }
            .to(
                ungdomsytelseInntektsrapporteringPreprosessertTopic.name,
                ungdomsytelseInntektsrapporteringPreprosessertTopic.producedWith
            )
        return stream
    }

    override fun health(): Health =
        HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
