package no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.oppgavebekreftelse

import no.nav.brukerdialog.domenetjenester.mottak.PreprosesseringsService
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.processors.LoggingToMDCProcessor
import no.nav.brukerdialog.kafka.processors.process
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.utils.HealthIndicatorUtils
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.oppgavebekreftelse.domene.UngdomsytelseOppgavebekreftelseMottatt
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.oppgavebekreftelse.domene.UngdomsytelseOppgavebekreftelsePreprosessert
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.soknad.UngdomsytelsesøknadPreprosessering
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
class UngdomsytelseOppgavebekreftelsePreprosessering(
    private val preprosesseringsService: PreprosesseringsService,
    private val ungdomsytelseOppgavebekreftelseMottattTopic: Topic<TopicEntry<UngdomsytelseOppgavebekreftelseMottatt>>,
    private val ungdomsytelseOppgavebekreftelsePreprosessertTopic: Topic<TopicEntry<UngdomsytelseOppgavebekreftelsePreprosessert>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(UngdomsytelseOppgavebekreftelseTopologyConfiguration.UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_PREPROSESSERING_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(UngdomsytelseOppgavebekreftelseTopologyConfiguration.UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_PREPROSESSERING_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
) : HealthIndicator {

    private companion object {
        private val STREAM_NAME = KafkaStreamName.UNGDOMSYTELSE_OPPGAVEBEKREFTELSE_PREPROSESSERING
        private val logger = LoggerFactory.getLogger(UngdomsytelsesøknadPreprosessering::class.java)
    }

    @Bean
    fun ungdomsytelseOppgavebekreftelsePreprosesseringsStream(): KStream<String, TopicEntry<UngdomsytelseOppgavebekreftelseMottatt>> {
        val stream: KStream<String, TopicEntry<UngdomsytelseOppgavebekreftelseMottatt>> = streamsBuilder
            .stream(ungdomsytelseOppgavebekreftelseMottattTopic.name, ungdomsytelseOppgavebekreftelseMottattTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor<UngdomsytelseOppgavebekreftelseMottatt>() })
            .mapValues { _: String, value: TopicEntry<UngdomsytelseOppgavebekreftelseMottatt> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val søknad = value.data
                    val preprosesseringsResultat =
                        preprosesseringsService.preprosesser(søknad.mapTilPreprosesseringsData())

                    søknad.mapTilPreprosessert(preprosesseringsResultat.dokumenter)
                }
            }
            .to(ungdomsytelseOppgavebekreftelsePreprosessertTopic.name, ungdomsytelseOppgavebekreftelsePreprosessertTopic.producedWith)
        return stream
    }

    override fun health(): Health =
        HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
