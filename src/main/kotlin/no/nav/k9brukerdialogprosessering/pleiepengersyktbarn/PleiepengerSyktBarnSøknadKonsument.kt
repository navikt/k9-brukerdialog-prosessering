package no.nav.k9brukerdialogprosessering.pleiepengersyktbarn

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.k9brukerdialogprosessering.common.TopicEntry
import no.nav.k9brukerdialogprosessering.common.TypeReferanseHelper
import no.nav.k9brukerdialogprosessering.config.kafka.KafkaStreamsConfig
import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene.PSBMottattSøknad
import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene.PSBPreprosessertSøknad
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Produced
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.kafka.support.serializer.JsonSerde
import org.springframework.stereotype.Service


@Service
class PleiepengerSyktBarnSøknadKonsument(
    private val mapper: ObjectMapper,
    private val preprosesseringsService: PSBPreprosesseringsService,
    @Qualifier(KafkaStreamsConfig.PSB_STREAMS_BUILDER_BEAN_NAME) private val psbKStreamBuilder: StreamsBuilder,
) {
    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(PleiepengerSyktBarnSøknadKonsument::class.java)
        const val PSB_MOTTATT_TOPIC = "dusseldorf.privat-pleiepengesoknad-mottatt-v2"
        const val PSB_PREPROSESSERT_TOPIC = "dusseldorf.privat-pleiepengesoknad-preprosessert"
        const val PSB_CLEANUP_TOPIC = "dusseldorf.privat-pleiepengesoknad-cleanup"
    }

    @Bean
    fun pleiepengerSyktBarnPreprosesseringsStream(): KStream<String, TopicEntry<PSBMottattSøknad>> {
        val stream: KStream<String, TopicEntry<PSBMottattSøknad>> = psbKStreamBuilder.stream(
            PSB_MOTTATT_TOPIC,
            Consumed.with(Serdes.StringSerde(), JsonSerde(TypeReferanseHelper(), mapper))
        )

        stream
            .mapValues { key: String, value: TopicEntry<PSBMottattSøknad> ->
                logger.info("Mottatt søknad med key: $key -> $value")
                val preprosesser = preprosesseringsService.preprosesser(value.data)
                TopicEntry(value.metadata, preprosesser)
            }.to(PSB_PREPROSESSERT_TOPIC, Produced.with(Serdes.StringSerde(), JsonSerde(TypeReferanseHelper(), mapper)))

        return stream
    }


    @Bean
    fun pleiepengerSyktBarnJournalføringsStream(): KStream<String, TopicEntry<PSBPreprosessertSøknad>> {
        val stream: KStream<String, TopicEntry<PSBPreprosessertSøknad>> = psbKStreamBuilder.stream(
            PSB_PREPROSESSERT_TOPIC,
            Consumed.with(Serdes.StringSerde(), JsonSerde(TypeReferanseHelper(), mapper))
        )
        stream.foreach { key: String, value: TopicEntry<PSBPreprosessertSøknad> ->
            logger.info("Journalfører søknad med key: $key -> $value")
            TopicEntry(value.metadata, null)
        }
        //.to(PSB_CLEANUP_TOPIC, Produced.with(Serdes.StringSerde(), JsonSerde(TypeReferanseHelper(), mapper)))

        return stream
    }

    @Bean
    fun pleiepengerSyktBarnCleanupStream(): KStream<String, TopicEntry<PSBPreprosessertSøknad>> {
        val stream: KStream<String, TopicEntry<PSBPreprosessertSøknad>> =
            psbKStreamBuilder.stream(PSB_CLEANUP_TOPIC, Consumed.with(Serdes.StringSerde(), JsonSerde(TypeReferanseHelper(), mapper)))
        stream.mapValues { key: String, value: TopicEntry<PSBPreprosessertSøknad> ->
            logger.info("Cleanup søknad med key: $key -> $value")
        }

        return stream
    }
}
