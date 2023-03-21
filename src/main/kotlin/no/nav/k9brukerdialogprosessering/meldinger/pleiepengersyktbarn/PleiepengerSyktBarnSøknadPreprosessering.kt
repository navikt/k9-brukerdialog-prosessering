package no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn

import no.nav.k9brukerdialogprosessering.innsending.PreprosesseringsService
import no.nav.k9brukerdialogprosessering.kafka.config.KafkaStreamName
import no.nav.k9brukerdialogprosessering.kafka.config.Topic
import no.nav.k9brukerdialogprosessering.kafka.processors.LoggingToMDCProcessor
import no.nav.k9brukerdialogprosessering.kafka.processors.process
import no.nav.k9brukerdialogprosessering.kafka.types.TopicEntry
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.PSBTopologyConfiguration.Companion.PSB_PREPROSESSERING_STREAMS_BUILDER_NAME
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.PSBMottattSøknad
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.PSBPreprosessertSøknad
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
