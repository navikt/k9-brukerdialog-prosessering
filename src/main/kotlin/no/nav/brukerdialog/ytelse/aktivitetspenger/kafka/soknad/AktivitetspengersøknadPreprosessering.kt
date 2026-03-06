package no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.soknad

import no.nav.brukerdialog.domenetjenester.mottak.PreprosesseringsService
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.processors.LoggingToMDCProcessor
import no.nav.brukerdialog.kafka.processors.process
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.utils.HealthIndicatorUtils
import no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.soknad.domene.AktivitetspengersøknadMottatt
import no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.soknad.domene.AktivitetspengersøknadPreprosessertSøknad
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
class AktivitetspengersøknadPreprosessering(
    private val preprosesseringsService: PreprosesseringsService,
    private val aktivitetspengersøknadMottattTopic: Topic<TopicEntry<AktivitetspengersøknadMottatt>>,
    private val aktivitetspengersøknadPreprosessertTopic: Topic<TopicEntry<AktivitetspengersøknadPreprosessertSøknad>>,
    private val retryTemplate: RetryTemplate,
    @param:Qualifier(AktivitetspengersøknadTopologyConfiguration.AKTIVITETSPENGER_SØKNAD_PREPROSESSERING_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @param:Qualifier(AktivitetspengersøknadTopologyConfiguration.AKTIVITETSPENGER_SØKNAD_PREPROSESSERING_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
) : HealthIndicator {

    private companion object {
        private val STREAM_NAME = KafkaStreamName.AKTIVITETSPENGER_SØKNAD_PREPROSESSERING
        private val logger = LoggerFactory.getLogger(AktivitetspengersøknadPreprosessering::class.java)
    }

    @Bean
    fun aktivitetspengersøknadPreprosesseringsStream(): KStream<String, TopicEntry<AktivitetspengersøknadMottatt>> {
        val stream: KStream<String, TopicEntry<AktivitetspengersøknadMottatt>> = streamsBuilder
            .stream(aktivitetspengersøknadMottattTopic.name, aktivitetspengersøknadMottattTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .mapValues { _: String, value: TopicEntry<AktivitetspengersøknadMottatt> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val søknad = value.data
                    val preprosesseringsResultat =
                        preprosesseringsService.preprosesser(søknad.mapTilPreprosesseringsData())

                    søknad.mapTilPreprosessert(preprosesseringsResultat.dokumenter)
                }
            }
            .to(aktivitetspengersøknadPreprosessertTopic.name, aktivitetspengersøknadPreprosessertTopic.producedWith)
        return stream
    }

    override fun health(): Health =
        HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
