package no.nav.brukerdialog.ytelse.opplæringspenger.kafka

import no.nav.brukerdialog.domenetjenester.mottak.PreprosesseringsService
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.processors.LoggingToMDCProcessor
import no.nav.brukerdialog.kafka.processors.process
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.utils.HealthIndicatorUtils
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.OLPTopologyConfiguration.Companion.OLP_PREPROSESSERING_STREAMS_BUILDER_NAME
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.OLPMottattSøknad
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.OLPPreprosessertSøknad
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
class OLPSøknadPreprosessering(
    private val preprosesseringsService: PreprosesseringsService,
    private val olpMottattTopic: Topic<TopicEntry<OLPMottattSøknad>>,
    private val olpPreprosesertTopic: Topic<TopicEntry<OLPPreprosessertSøknad>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(OLP_PREPROSESSERING_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(OLP_PREPROSESSERING_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
) : HealthIndicator{

    private companion object {
        private val STREAM_NAME = KafkaStreamName.OPPLÆRINGSPENGER_SØKNAD_PREPROSESSERING
        private val logger = LoggerFactory.getLogger(OLPSøknadPreprosessering::class.java)
    }

    @Bean
    fun opplæringspengerPreprosesseringsStream(): KStream<String, TopicEntry<OLPMottattSøknad>> {
        val stream: KStream<String, TopicEntry<OLPMottattSøknad>> = streamsBuilder
            .stream(olpMottattTopic.name, olpMottattTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor<OLPMottattSøknad>() })
            .mapValues { _: String, value: TopicEntry<OLPMottattSøknad> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val olpMottattSøknad = value.data
                    val preprosesseringsResultat =
                        preprosesseringsService.preprosesser(olpMottattSøknad.mapTilPreprosesseringsData())

                    olpMottattSøknad.mapTilPreprosessert(preprosesseringsResultat.dokumenter)
                }
            }
            .to(olpPreprosesertTopic.name, olpPreprosesertTopic.producedWith)
        return stream
    }

    override fun health(): Health = HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
