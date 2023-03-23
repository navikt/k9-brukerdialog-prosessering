package no.nav.k9brukerdialogprosessering.meldinger.ettersendelse

import no.nav.k9brukerdialogprosessering.innsending.PreprosesseringsService
import no.nav.k9brukerdialogprosessering.kafka.config.KafkaStreamName
import no.nav.k9brukerdialogprosessering.kafka.config.Topic
import no.nav.k9brukerdialogprosessering.kafka.processors.LoggingToMDCProcessor
import no.nav.k9brukerdialogprosessering.kafka.processors.process
import no.nav.k9brukerdialogprosessering.kafka.types.TopicEntry
import no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.EttersendelseTopologyConfiguration.Companion.ETTERSENDELSE_PREPROSESSERING_STREAMS_BUILDER_NAME
import no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.domene.Ettersendelse
import no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.domene.PreprosessertEttersendelse
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
class EttersendelsePreprosessering(
    private val preprosesseringsService: PreprosesseringsService,
    private val ettersendelseMottattTopic: Topic<TopicEntry<Ettersendelse>>,
    private val ettersendelsePreprosesertTopic: Topic<TopicEntry<PreprosessertEttersendelse>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(ETTERSENDELSE_PREPROSESSERING_STREAMS_BUILDER_NAME) private val streamsBuilder: StreamsBuilder,
    @Qualifier(ETTERSENDELSE_PREPROSESSERING_STREAMS_BUILDER_NAME) private val streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
) : HealthIndicator{

    private companion object {
        private val STREAM_NAME = KafkaStreamName.ETTERSENDELSE_PREPROSESSERING
        private val logger = LoggerFactory.getLogger(EttersendelsePreprosessering::class.java)
    }

    @Bean
    fun ettersendelsePreprosesseringsStream(): KStream<String, TopicEntry<Ettersendelse>> {
        val stream: KStream<String, TopicEntry<Ettersendelse>> = streamsBuilder
            .stream(ettersendelseMottattTopic.name, ettersendelseMottattTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor<Ettersendelse>() })
            .mapValues { _: String, value: TopicEntry<Ettersendelse> ->
                process(name = STREAM_NAME, entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val mottattEttersendelse = value.data
                    val preprosesseringsResultat =
                        preprosesseringsService.preprosesser(mottattEttersendelse.mapTilPreprosesseringsData())

                    mottattEttersendelse.mapTilPreprosessert(preprosesseringsResultat.dokumenter)
                }
            }
            .to(ettersendelsePreprosesertTopic.name, ettersendelsePreprosesertTopic.producedWith)
        return stream
    }

    override fun health(): Health = HealthIndicatorUtils.resolveKafkaStreamHealth(STREAM_NAME, streamsBuilderFactoryBean)
}
