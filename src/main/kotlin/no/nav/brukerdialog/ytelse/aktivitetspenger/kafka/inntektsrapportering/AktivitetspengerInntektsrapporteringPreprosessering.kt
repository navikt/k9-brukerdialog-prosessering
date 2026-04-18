package no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.inntektsrapportering

import no.nav.brukerdialog.domenetjenester.mottak.PreprosesseringsService
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.processors.LoggingToMDCProcessor
import no.nav.brukerdialog.kafka.processors.process
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.utils.HealthIndicatorUtils
import no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.inntektsrapportering.domene.AktivitetspengerInntektsrapporteringMottatt
import no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.inntektsrapportering.domene.AktivitetspengerInntektsrapporteringPreprosessert
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
class AktivitetspengerInntektsrapporteringPreprosessering(
    private val preprosesseringsService: PreprosesseringsService,
    private val aktivitetspengerInntektsrapporteringMottattTopic: Topic<TopicEntry<AktivitetspengerInntektsrapporteringMottatt>>,
    private val aktivitetspengerInntektsrapporteringPreprosessertTopic: Topic<TopicEntry<AktivitetspengerInntektsrapporteringPreprosessert>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(AktivitetspengerInntektsrapporteringTopologyConfiguration.Companion.AKTIVITETSPENGER_INNTEKTSRAPPORTERING_PREPROSESSERING_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(AktivitetspengerInntektsrapporteringTopologyConfiguration.Companion.AKTIVITETSPENGER_INNTEKTSRAPPORTERING_PREPROSESSERING_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
) : HealthIndicator {

    private companion object {
        private val STREAM_NAME = KafkaStreamName.AKTIVITETSPENGER_INNTEKTSRAPPORTERING_PREPROSESSERING
        private val logger = LoggerFactory.getLogger(AktivitetspengerInntektsrapporteringPreprosessering::class.java)
    }

    @Bean
    fun aktivitetspengerInntektsrapporteringPreprosesseringsStream(): KStream<String, TopicEntry<AktivitetspengerInntektsrapporteringMottatt>> {
        val stream: KStream<String, TopicEntry<AktivitetspengerInntektsrapporteringMottatt>> = streamsBuilder
            .stream(
                aktivitetspengerInntektsrapporteringMottattTopic.name,
                aktivitetspengerInntektsrapporteringMottattTopic.consumedWith
            )

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor<AktivitetspengerInntektsrapporteringMottatt>() })
            .mapValues { _: String, value: TopicEntry<AktivitetspengerInntektsrapporteringMottatt> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val søknad = value.data
                    val preprosesseringsResultat =
                        preprosesseringsService.preprosesser(søknad.mapTilPreprosesseringsData())

                    søknad.mapTilPreprosessert(preprosesseringsResultat.dokumenter)
                }
            }
            .to(
                aktivitetspengerInntektsrapporteringPreprosessertTopic.name,
                aktivitetspengerInntektsrapporteringPreprosessertTopic.producedWith
            )
        return stream
    }

    override fun health(): Health =
        HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
