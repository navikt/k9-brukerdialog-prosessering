package no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.kafka

import no.nav.brukerdialog.domenetjenester.mottak.PreprosesseringsService
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.processors.LoggingToMDCProcessor
import no.nav.brukerdialog.kafka.processors.process
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.PilsSøknadMottatt
import no.nav.brukerdialog.utils.HealthIndicatorUtils
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.kafka.PILSTopologyConfiguration.Companion.PILS_PREPROSESSERING_STREAMS_BUILDER_NAME
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.kafka.domene.PilsPreprosessertSøknad
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
class PleiepengerILivetsSluttfaseSøknadPreprosessering(
    private val preprosesseringsService: PreprosesseringsService,
    private val pilsMottattTopic: Topic<TopicEntry<PilsSøknadMottatt>>,
    private val pilsPreprosesertTopic: Topic<TopicEntry<PilsPreprosessertSøknad>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(PILS_PREPROSESSERING_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(PILS_PREPROSESSERING_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
) : HealthIndicator{

    private companion object {
        private val STREAM_NAME = KafkaStreamName.PILS_SØKNAD_PREPROSESSERING
        private val logger = LoggerFactory.getLogger(PleiepengerILivetsSluttfaseSøknadPreprosessering::class.java)
    }

    @Bean
    fun pleiepengerILivetsSluttfasePreprosesseringsStream(): KStream<String, TopicEntry<PilsSøknadMottatt>> {
        val stream: KStream<String, TopicEntry<PilsSøknadMottatt>> = streamsBuilder
            .stream(pilsMottattTopic.name, pilsMottattTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor<PilsSøknadMottatt>() })
            .mapValues { _: String, value: TopicEntry<PilsSøknadMottatt> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val pilsMottattSøknad = value.data
                    val preprosesseringsResultat =
                        preprosesseringsService.preprosesser(pilsMottattSøknad.mapTilPreprosesseringsData())

                    pilsMottattSøknad.mapTilPreprosessert(preprosesseringsResultat.dokumenter)
                }
            }
            .to(pilsPreprosesertTopic.name, pilsPreprosesertTopic.producedWith)
        return stream
    }

    override fun health(): Health = HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
