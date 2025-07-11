package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.kafka

import no.nav.brukerdialog.domenetjenester.mottak.PreprosesseringsService
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.processors.LoggingToMDCProcessor
import no.nav.brukerdialog.kafka.processors.process
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.kafka.PSBTopologyConfiguration.Companion.PSB_PREPROSESSERING_STREAMS_BUILDER_NAME
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.PSBMottattSøknad
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.kafka.domene.PSBPreprosessertSøknad
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
class PleiepengerSyktBarnSøknadPreprosessering(
    private val preprosesseringsService: PreprosesseringsService,
    private val psbMottattTopic: Topic<TopicEntry<PSBMottattSøknad>>,
    private val psbPreprosesertTopic: Topic<TopicEntry<PSBPreprosessertSøknad>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(PSB_PREPROSESSERING_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(PSB_PREPROSESSERING_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
) : HealthIndicator{

    private companion object {
        private val STREAM_NAME = KafkaStreamName.PSB_SØKNAD_PREPROSESSERING
        private val logger = LoggerFactory.getLogger(PleiepengerSyktBarnSøknadPreprosessering::class.java)
    }

    @Bean
    fun pleiepengerSyktBarnPreprosesseringsStream(): KStream<String, TopicEntry<PSBMottattSøknad>> {
        val stream: KStream<String, TopicEntry<PSBMottattSøknad>> = streamsBuilder
            .stream(psbMottattTopic.name, psbMottattTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor<PSBMottattSøknad>() })
            .mapValues { _: String, value: TopicEntry<PSBMottattSøknad> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val psbMottattSøknad = value.data
                    val preprosesseringsResultat =
                        preprosesseringsService.preprosesser(psbMottattSøknad.mapTilPreprosesseringsData())

                    psbMottattSøknad.mapTilPreprosessert(preprosesseringsResultat.dokumenter)
                }
            }
            .to(psbPreprosesertTopic.name, psbPreprosesertTopic.producedWith)
        return stream
    }

    override fun health(): Health = HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
