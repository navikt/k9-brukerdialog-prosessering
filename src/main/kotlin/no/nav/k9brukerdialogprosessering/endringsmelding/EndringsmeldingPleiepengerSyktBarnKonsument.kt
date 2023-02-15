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

    private companion object {
        private val IGNORE_LIST = listOf(
            "generated-e7b749b9-3777-4900-9d58-a6e52f2d5105",
            "generated-7c20e434-3416-4ace-80b4-ad844c58e80f",
            "generated-e7b749b9-3777-4900-9d58-a6e52f2d5105",
            "generated-865a538e-ca9a-4217-9d27-9cf68de4d875",
            "generated-8c29239f-50e7-46b3-9ddd-0878405215fa",
            "generated-6a70019d-3208-459b-9e08-545c03ca0dca",
            "generated-646fde4e-779b-4521-84b3-c5f1a4b10cf7",
            "generated-4bda1c1e-e92e-4a1e-996c-ffff22e34cfd",
            "generated-83c9d8e1-2e20-4eaf-b2a5-5bf680636446",
            "generated-657dea85-e0cc-449c-acb7-2647a11d2e3b",
            "generated-e0d4039e-8238-46ac-aa8e-e16ab330f18c",
            "generated-f20325bb-823c-43cb-b360-6ff434877650"
        )
    }

    @Bean
    fun pleiepengerSyktBarnEndringsmeldingPreprosesseringsStream(): KStream<String, TopicEntry<PSBEndringsmeldingMottatt>> {
        val stream: KStream<String, TopicEntry<PSBEndringsmeldingMottatt>> = psbEndringsmeldingKStreamBuilder
            .stream(psbEndringsmeldingMottattTopic.name, psbEndringsmeldingMottattTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor<PSBEndringsmeldingMottatt>() })
            .filterNot { søknadId, entry -> IGNORE_LIST.contains(entry.metadata.correlationId) }
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
            .filterNot { søknadId, entry -> IGNORE_LIST.contains(entry.metadata.correlationId) }
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
            .filterNot { søknadId, entry -> IGNORE_LIST.contains(entry.metadata.correlationId) }
            .mapValues { søknadId: String, value: TopicEntry<Cleanup<PSBPreprossesertEndringsmelding>> ->
                process(name = "pleiepengerSyktBarnEndringsmeldingCleanupStream", soknadId = søknadId, entry = value) {
                    cleanupService.cleanup(value.data)
                }
            }

        return stream
    }
}
