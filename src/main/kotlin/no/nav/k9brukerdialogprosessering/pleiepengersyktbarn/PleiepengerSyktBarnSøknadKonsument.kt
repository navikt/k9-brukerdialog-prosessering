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
            .mapValues { søknadId: String, value: TopicEntry<PSBMottattSøknad> ->
                process(name = "pleiepengerSyktBarnPreprosesseringsStream", soknadId = søknadId, entry = value) {
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
        val stream: KStream<String, TopicEntry<PSBPreprosessertSøknad>> = psbKStreamBuilder
            .stream(psbPreprosesertTopic.name, psbPreprosesertTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .mapValues { søknadId: String, value: TopicEntry<PSBPreprosessertSøknad> ->
                process(name = "pleiepengerSyktBarnJournalføringsStream", soknadId = søknadId, entry = value) {
                    val preprosessertSøknad: PSBPreprosessertSøknad = value.data
                    val journalførSøknad = journalføringsService.journalfør(preprosessertSøknad)
                    Cleanup(value.metadata, preprosessertSøknad, journalførSøknad)
                }
            }
            .to(psbCleanupTopic.name, psbCleanupTopic.producedWith)

        return stream
    }

    @Bean
    fun pleiepengerSyktBarnCleanupStream(): KStream<String, TopicEntry<Cleanup>> {
        val stream: KStream<String, TopicEntry<Cleanup>> = psbKStreamBuilder
            .stream(psbCleanupTopic.name, psbCleanupTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .mapValues { søknadId: String, value: TopicEntry<Cleanup> ->
                process(name = "pleiepengerSyktBarnCleanupStream", soknadId = søknadId, entry = value) {
                    cleanupService.cleanup(value.data)
                }
            }

        return stream
    }
}
