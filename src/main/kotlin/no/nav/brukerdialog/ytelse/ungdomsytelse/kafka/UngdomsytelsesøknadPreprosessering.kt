package no.nav.brukerdialog.ytelse.ungdomsytelse.kafka

import no.nav.brukerdialog.domenetjenester.mottak.PreprosesseringsService
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.processors.LoggingToMDCProcessor
import no.nav.brukerdialog.kafka.processors.process
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.utils.HealthIndicatorUtils
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.UngdomsytelsesøknadTopologyConfiguration.Companion.UNGDOMSYTELSE_SØKNAD_PREPROSESSERING_STREAMS_BUILDER_NAME
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.domene.UngdomsytelsesøknadMottatt
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.domene.UngdomsytelsesøknadPreprosessertSøknad
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
class UngdomsytelsesøknadPreprosessering(
    private val preprosesseringsService: PreprosesseringsService,
    private val ungdomsytelsesøknadMottattTopic: Topic<TopicEntry<UngdomsytelsesøknadMottatt>>,
    private val ungdomsytelsesøknadPreprosessertTopic: Topic<TopicEntry<UngdomsytelsesøknadPreprosessertSøknad>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(UNGDOMSYTELSE_SØKNAD_PREPROSESSERING_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(UNGDOMSYTELSE_SØKNAD_PREPROSESSERING_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
) : HealthIndicator {

    private companion object {
        private val STREAM_NAME = KafkaStreamName.UNGDOMSYTELSE_SØKNAD_PREPROSESSERING
        private val logger = LoggerFactory.getLogger(UngdomsytelsesøknadPreprosessering::class.java)
    }

    @Bean
    fun ungdomsytelsesøknadPreprosesseringsStream(): KStream<String, TopicEntry<UngdomsytelsesøknadMottatt>> {
        val stream: KStream<String, TopicEntry<UngdomsytelsesøknadMottatt>> = streamsBuilder
            .stream(ungdomsytelsesøknadMottattTopic.name, ungdomsytelsesøknadMottattTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor<UngdomsytelsesøknadMottatt>() })
            .mapValues { _: String, value: TopicEntry<UngdomsytelsesøknadMottatt> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val søknad = value.data
                    val preprosesseringsResultat =
                        preprosesseringsService.preprosesser(søknad.mapTilPreprosesseringsData())

                    søknad.mapTilPreprosessert(preprosesseringsResultat.dokumenter)
                }
            }
            .to(ungdomsytelsesøknadPreprosessertTopic.name, ungdomsytelsesøknadPreprosessertTopic.producedWith)
        return stream
    }

    override fun health(): Health =
        HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
