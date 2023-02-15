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
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Service


@Service
class EndringsmeldingPleiepengerSyktBarnKonsument(
    private val preprosesseringsService: PreprosesseringsService,
    private val journalføringsService: JournalføringsService,
    private val cleanupService: CleanupService<PSBPreprossesertEndringsmelding>,
    private val psbEndringsmeldingMottattTopic: Topic<TopicEntry<PSBEndringsmeldingMottatt>>,
    private val psbEndringsmeldingPreprosesertTopic: Topic<TopicEntry<PSBPreprossesertEndringsmelding>>,
    private val psbEndringsmeldingCleanupTopic: Topic<TopicEntry<Cleanup<PSBPreprossesertEndringsmelding>>>,
    private val retryTemplate: RetryTemplate,
    @Qualifier(KafkaStreamsConfig.PSB_ENDRINGSMELDING_STREAMS_BUILDER_BEAN_NAME) private val psbEndringsmeldingKStreamBuilder: StreamsBuilder,
) {

    private companion object {
        private val IGNORE_LIST = listOf<String>()
        private val logger = LoggerFactory.getLogger(EndringsmeldingPleiepengerSyktBarnKonsument::class.java)
    }

    @Bean
    fun pleiepengerSyktBarnEndringsmeldingPreprosesseringsStream(): KStream<String, TopicEntry<PSBEndringsmeldingMottatt>> {
        val stream: KStream<String, TopicEntry<PSBEndringsmeldingMottatt>> = psbEndringsmeldingKStreamBuilder
            .stream(psbEndringsmeldingMottattTopic.name, psbEndringsmeldingMottattTopic.consumedWith)

        stream
            .process(ProcessorSupplier { LoggingToMDCProcessor<PSBEndringsmeldingMottatt>() })
            .filterNot { _, entry -> IGNORE_LIST.contains(entry.metadata.correlationId) }
            .mapValues { _: String, value: TopicEntry<PSBEndringsmeldingMottatt> ->
                process(name = "pleiepengerSyktBarnEndringsmeldingPreprosesseringsStream", entry = value, retryTemplate = retryTemplate, logger = logger) {
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
            .filterNot { _, entry -> IGNORE_LIST.contains(entry.metadata.correlationId) }
            .mapValues { _: String, value: TopicEntry<PSBPreprossesertEndringsmelding> ->
                process(name = "pleiepengerSyktBarnEndringsmeldingJournalføringsStream", entry = value, retryTemplate = retryTemplate, logger = logger) {
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
            .filterNot { _, entry -> IGNORE_LIST.contains(entry.metadata.correlationId) }
            .mapValues { _: String, value: TopicEntry<Cleanup<PSBPreprossesertEndringsmelding>> ->
                process(name = "pleiepengerSyktBarnEndringsmeldingCleanupStream", entry = value, retryTemplate = retryTemplate, logger = logger) {
                    cleanupService.cleanup(value.data)
                }
            }

        return stream
    }
}
