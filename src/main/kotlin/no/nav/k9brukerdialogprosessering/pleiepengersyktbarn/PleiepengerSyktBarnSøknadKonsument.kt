package no.nav.k9brukerdialogprosessering.pleiepengersyktbarn

import no.nav.k9brukerdialogprosessering.innsending.CleanupService
import no.nav.k9brukerdialogprosessering.innsending.JournalføringsService
import no.nav.k9brukerdialogprosessering.innsending.PreprosesseringsService
import no.nav.k9brukerdialogprosessering.kafka.config.KafkaStreamsConfig
import no.nav.k9brukerdialogprosessering.kafka.config.Topic
import no.nav.k9brukerdialogprosessering.kafka.processors.LoggingToMDCProcessor
import no.nav.k9brukerdialogprosessering.kafka.types.Cleanup
import no.nav.k9brukerdialogprosessering.kafka.types.TopicEntry
import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.PSBTopologyConfiguration.Companion.PSB_CLEANUP_TOPIC
import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.PSBTopologyConfiguration.Companion.PSB_PREPROSESSERT_TOPIC
import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene.PSBMottattSøknad
import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene.PSBPreprosessertSøknad
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.processor.api.ProcessorSupplier
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service


@Service
class PleiepengerSyktBarnSøknadKonsument(
    private val preprosesseringsService: PreprosesseringsService,
    private val journalføringsService: JournalføringsService,
    private val cleanupService: CleanupService,
    private val psbMottattTopic: Topic<TopicEntry<PSBMottattSøknad>>,
    private val psbPreprosesertTopic: Topic<TopicEntry<PSBPreprosessertSøknad>>,
    private val psbCleanupTopic: Topic<TopicEntry<Cleanup>>,
    @Qualifier(KafkaStreamsConfig.PSB_STREAMS_BUILDER_BEAN_NAME) private val psbKStreamBuilder: StreamsBuilder,
) {

    @Bean
    fun pleiepengerSyktBarnPreprosesseringsStream(): KStream<String, TopicEntry<PSBMottattSøknad>> {
        val stream: KStream<String, TopicEntry<PSBMottattSøknad>> = psbKStreamBuilder
            .stream(psbMottattTopic.name, psbMottattTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor<PSBMottattSøknad>() })
            .mapValues { _: String, value: TopicEntry<PSBMottattSøknad> ->
                val psbMottattSøknad = value.data
                val preprosesseringResultat =
                    preprosesseringsService.preprosesser(psbMottattSøknad.mapTilPreprosesseringsData())
                val preprosessert: PSBPreprosessertSøknad =
                    psbMottattSøknad.mapTilPreprosessert(preprosesseringResultat.dokumenter)
                TopicEntry(value.metadata, preprosessert)
            }
            .to(psbPreprosesertTopic.name, psbPreprosesertTopic.producedWith)
        return stream
    }

    @Bean
    fun pleiepengerSyktBarnJournalføringsStream(): KStream<String, TopicEntry<PSBPreprosessertSøknad>> {
        val stream: KStream<String, TopicEntry<PSBPreprosessertSøknad>> = psbKStreamBuilder
            .stream(psbPreprosesertTopic.name, psbPreprosesertTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .mapValues { _: String, value: TopicEntry<PSBPreprosessertSøknad> ->
                val preprosessertSøknad: PSBPreprosessertSøknad = value.data
                val journalførSøknad = journalføringsService.journalfør(preprosessertSøknad)
                val cleanup: Cleanup = Cleanup(value.metadata, preprosessertSøknad, journalførSøknad)
                TopicEntry(value.metadata, cleanup)
            }
            .to(psbCleanupTopic.name, psbCleanupTopic.producedWith)

        return stream
    }

    @Bean
    fun pleiepengerSyktBarnCleanupStream(): KStream<String, TopicEntry<Cleanup>> {
        val stream: KStream<String, TopicEntry<Cleanup>> = psbKStreamBuilder
            .stream(psbCleanupTopic.name, psbCleanupTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor<Cleanup>() })
            .mapValues { _: String, value: TopicEntry<Cleanup> ->
                cleanupService.cleanup(value.data)
                value
            }

        return stream
    }
}
