package no.nav.k9brukerdialogprosessering.pleiepengersyktbarn

import no.nav.k9brukerdialogprosessering.innsending.CleanupService
import no.nav.k9brukerdialogprosessering.innsending.JournalføringsService
import no.nav.k9brukerdialogprosessering.innsending.PreprosesseringsService
import no.nav.k9brukerdialogprosessering.kafka.config.KafkaStreamsConfig
import no.nav.k9brukerdialogprosessering.kafka.config.Topic
import no.nav.k9brukerdialogprosessering.kafka.processors.LoggingToMDCProcessor
import no.nav.k9brukerdialogprosessering.kafka.processors.process
import no.nav.k9brukerdialogprosessering.kafka.types.Cleanup
import no.nav.k9brukerdialogprosessering.kafka.types.TopicEntry
import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene.PSBMottattSøknad
import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene.PSBPreprosessertSøknad
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.processor.api.ProcessorSupplier
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Service


@Service
class PleiepengerSyktBarnSøknadKonsument(
    private val preprosesseringsService: PreprosesseringsService,
    private val journalføringsService: JournalføringsService,
    private val cleanupService: CleanupService<PSBPreprosessertSøknad>,
    private val psbMottattTopic: Topic<TopicEntry<PSBMottattSøknad>>,
    private val psbPreprosesertTopic: Topic<TopicEntry<PSBPreprosessertSøknad>>,
    private val psbCleanupTopic: Topic<TopicEntry<Cleanup<PSBPreprosessertSøknad>>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(KafkaStreamsConfig.PSB_STREAMS_BUILDER_BEAN_NAME) private val psbKStreamBuilder: StreamsBuilder,
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(PleiepengerSyktBarnSøknadKonsument::class.java)
    }

    @Bean
    fun pleiepengerSyktBarnPreprosesseringsStream(): KStream<String, TopicEntry<PSBMottattSøknad>> {
        val stream: KStream<String, TopicEntry<PSBMottattSøknad>> = psbKStreamBuilder
            .stream(psbMottattTopic.name, psbMottattTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor<PSBMottattSøknad>() })
            .mapValues { _: String, value: TopicEntry<PSBMottattSøknad> ->
                process(name = "pleiepengerSyktBarnPreprosesseringsStream", entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val psbMottattSøknad = value.data
                    val preprosesseringsResultat =
                        preprosesseringsService.preprosesser(psbMottattSøknad.mapTilPreprosesseringsData())

                    psbMottattSøknad.mapTilPreprosessert(preprosesseringsResultat.dokumenter)
                }
            }
            .to(psbPreprosesertTopic.name, psbPreprosesertTopic.producedWith)
        return stream
    }

    @Bean
    fun pleiepengerSyktBarnJournalføringsStream(): KStream<String, TopicEntry<PSBPreprosessertSøknad>> {
        val stream = psbKStreamBuilder
            .stream(psbPreprosesertTopic.name, psbPreprosesertTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .mapValues { _: String, value: TopicEntry<PSBPreprosessertSøknad> ->
                process(name = "pleiepengerSyktBarnJournalføringsStream", entry = value, retryTemplate = retryTemplate, logger = logger) {
                    val preprosessertSøknad: PSBPreprosessertSøknad = value.data
                    val journalførSøknad = journalføringsService.journalfør(preprosessertSøknad)
                    Cleanup(value.metadata, preprosessertSøknad, journalførSøknad)
                }
            }
            .to(psbCleanupTopic.name, psbCleanupTopic.producedWith)

        return stream
    }

    @Bean
    fun pleiepengerSyktBarnCleanupStream(): KStream<String, TopicEntry<Cleanup<PSBPreprosessertSøknad>>> {
        val stream: KStream<String, TopicEntry<Cleanup<PSBPreprosessertSøknad>>> = psbKStreamBuilder.stream(psbCleanupTopic.name, psbCleanupTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .mapValues { _: String, value: TopicEntry<Cleanup<PSBPreprosessertSøknad>> ->
                process(name = "pleiepengerSyktBarnCleanupStream", entry = value, retryTemplate = retryTemplate, logger = logger) {
                    cleanupService.cleanup(value.data)
                }
            }

        return stream
    }
}
