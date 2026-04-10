package no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.oppgavebekreftelse

import no.nav.brukerdialog.domenetjenester.mottak.PreprosesseringsService
import no.nav.brukerdialog.kafka.config.KafkaStreamName
import no.nav.brukerdialog.kafka.config.Topic
import no.nav.brukerdialog.kafka.processors.LoggingToMDCProcessor
import no.nav.brukerdialog.kafka.processors.process
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.utils.HealthIndicatorUtils
import no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.oppgavebekreftelse.domene.AktivitetspengerOppgavebekreftelseMottatt
import no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.oppgavebekreftelse.domene.AktivitetspengerOppgavebekreftelsePreprosessert
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
class AktivitetspengerOppgavebekreftelsePreprosessering(
    private val preprosesseringsService: PreprosesseringsService,
    private val aktivitetspengerOppgavebekreftelseMottattTopic: Topic<TopicEntry<AktivitetspengerOppgavebekreftelseMottatt>>,
    private val aktivitetspengerOppgavebekreftelsePreprosessertTopic: Topic<TopicEntry<AktivitetspengerOppgavebekreftelsePreprosessert>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(AktivitetspengerOppgavebekreftelseTopologyConfiguration.AKTIVITETSPENGER_OPPGAVEBEKREFTELSE_PREPROSESSERING_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(AktivitetspengerOppgavebekreftelseTopologyConfiguration.AKTIVITETSPENGER_OPPGAVEBEKREFTELSE_PREPROSESSERING_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
) : HealthIndicator {

    private companion object {
        private val STREAM_NAME = KafkaStreamName.AKTIVITETSPENGER_OPPGAVEBEKREFTELSE_PREPROSESSERING
        private val logger = LoggerFactory.getLogger(AktivitetspengerOppgavebekreftelsePreprosessering::class.java)
    }

    @Bean
    fun aktivitetspengerOppgavebekreftelsePreprosesseringsStream(): KStream<String, TopicEntry<AktivitetspengerOppgavebekreftelseMottatt>> {
        val stream: KStream<String, TopicEntry<AktivitetspengerOppgavebekreftelseMottatt>> = streamsBuilder
            .stream(
                aktivitetspengerOppgavebekreftelseMottattTopic.name,
                aktivitetspengerOppgavebekreftelseMottattTopic.consumedWith,
            )

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor<AktivitetspengerOppgavebekreftelseMottatt>() })
            .mapValues { _: String, value: TopicEntry<AktivitetspengerOppgavebekreftelseMottatt> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val søknad = value.data
                    val preprosesseringsResultat =
                        preprosesseringsService.preprosesser(søknad.mapTilPreprosesseringsData())
                    søknad.mapTilPreprosessert(preprosesseringsResultat.dokumenter)
                }
            }
            .to(
                aktivitetspengerOppgavebekreftelsePreprosessertTopic.name,
                aktivitetspengerOppgavebekreftelsePreprosessertTopic.producedWith,
            )
        return stream
    }

    override fun health(): Health =
        HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
