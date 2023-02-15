package no.nav.k9brukerdialogprosessering.endringsmelding

import no.nav.k9brukerdialogprosessering.endringsmelding.domene.PSBEndringsmeldingMottatt
import no.nav.k9brukerdialogprosessering.endringsmelding.domene.PSBPreprossesertEndringsmelding
import no.nav.k9brukerdialogprosessering.innsending.CleanupService
import no.nav.k9brukerdialogprosessering.innsending.JournalføringsService
import no.nav.k9brukerdialogprosessering.innsending.PreprosesseringsService
import no.nav.k9brukerdialogprosessering.kafka.config.KafkaStreamsConfig
import no.nav.k9brukerdialogprosessering.kafka.config.Topic
import no.nav.k9brukerdialogprosessering.kafka.processors.LoggingToMDCProcessor
import no.nav.k9brukerdialogprosessering.kafka.processors.process
import no.nav.k9brukerdialogprosessering.kafka.types.Cleanup
import no.nav.k9brukerdialogprosessering.kafka.types.TopicEntry
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.processor.api.ProcessorSupplier
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service


@Service
class EndringsmeldingPleiepengerSyktBarnKonsument(
    private val preprosesseringsService: PreprosesseringsService,
    private val journalføringsService: JournalføringsService,
    private val cleanupService: CleanupService<PSBPreprossesertEndringsmelding>,
    private val psbEndringsmeldingMottattTopic: Topic<TopicEntry<PSBEndringsmeldingMottatt>>,
    private val psbEndringsmeldingPreprosesertTopic: Topic<TopicEntry<PSBPreprossesertEndringsmelding>>,
    private val psbEndringsmeldingCleanupTopic: Topic<TopicEntry<Cleanup<PSBPreprossesertEndringsmelding>>>,
    @Qualifier(KafkaStreamsConfig.PSB_ENDRINGSMELDING_STREAMS_BUILDER_BEAN_NAME) private val psbEndringsmeldingKStreamBuilder: StreamsBuilder,
) {

    @Bean
    fun pleiepengerSyktBarnEndringsmeldingPreprosesseringsStream(): KStream<String, TopicEntry<PSBEndringsmeldingMottatt>> {
        val stream: KStream<String, TopicEntry<PSBEndringsmeldingMottatt>> = psbEndringsmeldingKStreamBuilder
            .stream(psbEndringsmeldingMottattTopic.name, psbEndringsmeldingMottattTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor<PSBEndringsmeldingMottatt>() })
            .filterNot { søknadId, entry -> "generated-e7b749b9-3777-4900-9d58-a6e52f2d5105" == entry.metadata.correlationId }
            .filterNot { søknadId, entry -> "generated-7c20e434-3416-4ace-80b4-ad844c58e80f" == entry.metadata.correlationId }
            .filterNot { søknadId, entry -> "generated-e7b749b9-3777-4900-9d58-a6e52f2d5105"== entry.metadata.correlationId }
            .filterNot { søknadId, entry -> "generated-865a538e-ca9a-4217-9d27-9cf68de4d875"== entry.metadata.correlationId }
            .mapValues { søknadId: String, value: TopicEntry<PSBEndringsmeldingMottatt> ->
                process(name = "pleiepengerSyktBarnEndringsmeldingPreprosesseringsStream", soknadId = søknadId, entry = value) {
                    val psbEndringsmeldingMottatt = value.data
                    val preprosesseringsResultat =
                        preprosesseringsService.preprosesser(psbEndringsmeldingMottatt.mapTilPreprosesseringsData())

                    psbEndringsmeldingMottatt.mapTilPreprosessert(preprosesseringsResultat.dokumenter)
                }
            }
            .to(psbEndringsmeldingPreprosesertTopic.name, psbEndringsmeldingPreprosesertTopic.producedWith)
        return stream
    }

    @Bean
    fun pleiepengerSyktBarnEndringsmeldingJournalføringsStream(): KStream<String, TopicEntry<PSBPreprossesertEndringsmelding>> {
        val stream = psbEndringsmeldingKStreamBuilder
            .stream(psbEndringsmeldingPreprosesertTopic.name, psbEndringsmeldingPreprosesertTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .filterNot { søknadId, entry -> "generated-e7b749b9-3777-4900-9d58-a6e52f2d5105" == entry.metadata.correlationId }
            .filterNot { søknadId, entry -> "generated-7c20e434-3416-4ace-80b4-ad844c58e80f" == entry.metadata.correlationId }
            .filterNot { søknadId, entry -> "generated-e7b749b9-3777-4900-9d58-a6e52f2d5105"== entry.metadata.correlationId }
            .filterNot { søknadId, entry -> "generated-865a538e-ca9a-4217-9d27-9cf68de4d875"== entry.metadata.correlationId }
            .mapValues { søknadId: String, value: TopicEntry<PSBPreprossesertEndringsmelding> ->
                process(name = "pleiepengerSyktBarnEndringsmeldingJournalføringsStream", soknadId = søknadId, entry = value) {
                    val preprosessertSøknad: PSBPreprossesertEndringsmelding = value.data
                    val journalførSøknad = journalføringsService.journalfør(preprosessertSøknad)
                    Cleanup(value.metadata, preprosessertSøknad, journalførSøknad)
                }
            }
            .to(psbEndringsmeldingCleanupTopic.name, psbEndringsmeldingCleanupTopic.producedWith)

        return stream
    }

    @Bean
    fun pleiepengerSyktBarnEndringsmeldingCleanupStream(): KStream<String, TopicEntry<Cleanup<PSBPreprossesertEndringsmelding>>> {
        val stream: KStream<String, TopicEntry<Cleanup<PSBPreprossesertEndringsmelding>>> = psbEndringsmeldingKStreamBuilder.stream(psbEndringsmeldingCleanupTopic.name, psbEndringsmeldingCleanupTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor() })
            .filterNot { søknadId, entry -> "generated-e7b749b9-3777-4900-9d58-a6e52f2d5105" == entry.metadata.correlationId }
            .filterNot { søknadId, entry -> "generated-7c20e434-3416-4ace-80b4-ad844c58e80f" == entry.metadata.correlationId }
            .filterNot { søknadId, entry -> "generated-e7b749b9-3777-4900-9d58-a6e52f2d5105"== entry.metadata.correlationId }
            .filterNot { søknadId, entry -> "generated-865a538e-ca9a-4217-9d27-9cf68de4d875"== entry.metadata.correlationId }
            .mapValues { søknadId: String, value: TopicEntry<Cleanup<PSBPreprossesertEndringsmelding>> ->
                process(name = "pleiepengerSyktBarnEndringsmeldingCleanupStream", soknadId = søknadId, entry = value) {
                    cleanupService.cleanup(value.data)
                }
            }

        return stream
    }
}
