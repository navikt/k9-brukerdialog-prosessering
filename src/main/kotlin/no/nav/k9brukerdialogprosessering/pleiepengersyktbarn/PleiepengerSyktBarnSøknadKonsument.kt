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
    private val cleanupService: CleanupService<PSBPreprosessertSøknad>,
    private val psbMottattTopic: Topic<TopicEntry<PSBMottattSøknad>>,
    private val psbPreprosesertTopic: Topic<TopicEntry<PSBPreprosessertSøknad>>,
    private val psbCleanupTopic: Topic<TopicEntry<Cleanup<PSBPreprosessertSøknad>>>,
    @Qualifier(KafkaStreamsConfig.PSB_STREAMS_BUILDER_BEAN_NAME) private val psbKStreamBuilder: StreamsBuilder,
) {

    @Bean
    fun pleiepengerSyktBarnPreprosesseringsStream(): KStream<String, TopicEntry<PSBMottattSøknad>> {
        val stream: KStream<String, TopicEntry<PSBMottattSøknad>> = psbKStreamBuilder.streamFrom(psbMottattTopic)
        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor<PSBMottattSøknad>() })
            .mapValues { _: String, value: TopicEntry<PSBMottattSøknad> ->
                val psbMottattSøknad = value.data
                val preprosesseringResultat =
                    preprosesseringsService.preprosesser(psbMottattSøknad.mapTilPreprosesseringsData())
                TopicEntry(value.metadata, psbMottattSøknad.mapTilPreprosessert(preprosesseringResultat.dokumenter))
            }
            .to(PSB_PREPROSESSERT_TOPIC, psbPreprosesertTopic.producedWith)
        return stream
    }

    @Bean
    fun pleiepengerSyktBarnJournalføringsStream(): KStream<String, TopicEntry<PSBPreprosessertSøknad>> {
        val stream: KStream<String, TopicEntry<PSBPreprosessertSøknad>> =
            psbKStreamBuilder.streamFrom(psbPreprosesertTopic)
        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .mapValues { _: String, value: TopicEntry<PSBPreprosessertSøknad> ->
                val preprosessertSøknad: PSBPreprosessertSøknad = value.data
                val journalførSøknad = journalføringsService.journalfør(preprosessertSøknad)
                TopicEntry(value.metadata, Cleanup(value.metadata, preprosessertSøknad, journalførSøknad))
            }
            .to(PSB_CLEANUP_TOPIC, psbCleanupTopic.producedWith)

        return stream
    }

    @Bean
    fun pleiepengerSyktBarnCleanupStream(): KStream<String, TopicEntry<Cleanup<PSBPreprosessertSøknad>>> {
        val stream: KStream<String, TopicEntry<Cleanup<PSBPreprosessertSøknad>>> =
            psbKStreamBuilder.streamFrom(psbCleanupTopic)
        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor<Cleanup<PSBPreprosessertSøknad>>() })
            .mapValues { _: String, value: TopicEntry<Cleanup<PSBPreprosessertSøknad>> ->
                cleanupService.cleanup(value.data)
                value
            }

        return stream
    }
}

inline fun <reified T> StreamsBuilder.streamFrom(topic: Topic<TopicEntry<T>>): KStream<String, TopicEntry<T>> {
    return this.stream(topic.name, topic.consumedWith)
}
