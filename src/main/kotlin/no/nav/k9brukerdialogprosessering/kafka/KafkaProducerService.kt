package no.nav.k9brukerdialogprosessering.kafka

import no.nav.k9brukerdialogprosessering.api.ytelse.Ytelse
import no.nav.k9brukerdialogprosessering.common.MetaInfo
import no.nav.k9brukerdialogprosessering.common.formaterStatuslogging
import no.nav.k9brukerdialogprosessering.kafka.Topics.ETTERSENDING_TOPIC
import no.nav.k9brukerdialogprosessering.kafka.Topics.MOTTATT_ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN_TOPIC
import no.nav.k9brukerdialogprosessering.kafka.Topics.OMSORGSDAGER_ALENEOMSORG_TOPIC
import no.nav.k9brukerdialogprosessering.kafka.Topics.OMSORGSPENGER_MIDLERTIDIG_ALENE_TOPIC
import no.nav.k9brukerdialogprosessering.kafka.Topics.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER_TOPIC
import no.nav.k9brukerdialogprosessering.kafka.Topics.OMSORGSPENGER_UTBETALING_SNF_TOPIC
import no.nav.k9brukerdialogprosessering.kafka.Topics.OMSORGSPENGER_UTVIDET_RETT_TOPIC
import no.nav.k9brukerdialogprosessering.kafka.Topics.PLEIEPENGER_LIVETS_SLUTTFASE_TOPIC
import no.nav.k9brukerdialogprosessering.kafka.Topics.PLEIEPENGER_SYKT_BARN_TOPIC
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.ReactiveHealthIndicator
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class KafkaProducerService(val kafkaTemplate: KafkaTemplate<String, TopicEntry<JSONObject>>) : ReactiveHealthIndicator {
    private val NAME = "KafkaProducer"
    private val logger = LoggerFactory.getLogger(KafkaProducer::class.java)

    internal fun produserKafkaMelding(metadata: MetaInfo, komplettSøknadSomJson: JSONObject, ytelse: Ytelse) {
        sendMeldingTilTopic(metadata, komplettSøknadSomJson, ytelse)
    }

    internal fun produserKafkaMeldinger(metadata: MetaInfo, komplettSøknadSomJson: List<JSONObject>, ytelse: Ytelse) {
        try {
            komplettSøknadSomJson.forEach { sendMeldingTilTopic(metadata, it, ytelse) }
        } catch (e: Exception) {
            logger.error("Feilet med produsering av kafkamelding")
            throw e
        }
    }

    private fun sendMeldingTilTopic(metadata: MetaInfo, komplettSøknadSomJson: JSONObject, ytelse: Ytelse) {
        val topic = hentTopicForYtelse(ytelse)
        val producerRecord = ProducerRecord(
            topic,
            komplettSøknadSomJson.getString("søknadId"),
            TopicEntry(
                metadata = metadata,
                data = komplettSøknadSomJson
            )
        )
        val result = kafkaTemplate.send(producerRecord).get()
        logger.info(
            formaterStatuslogging(
                ytelse,
                komplettSøknadSomJson.getString("søknadId"),
                "sendes til topic ${topic} med offset '${result.recordMetadata.offset()}' til partition '${result.recordMetadata.offset()}'"
            )
        )
    }

    override fun health(): Mono<Health> {
        return try {
            kafkaTemplate.partitionsFor(OMSORGSPENGER_UTVIDET_RETT_TOPIC)
            kafkaTemplate.partitionsFor(OMSORGSPENGER_MIDLERTIDIG_ALENE_TOPIC)
            kafkaTemplate.partitionsFor(ETTERSENDING_TOPIC)
            kafkaTemplate.partitionsFor(OMSORGSDAGER_ALENEOMSORG_TOPIC)
            kafkaTemplate.partitionsFor(OMSORGSPENGER_UTBETALING_ARBEIDSTAKER_TOPIC)
            kafkaTemplate.partitionsFor(OMSORGSPENGER_UTBETALING_SNF_TOPIC)
            kafkaTemplate.partitionsFor(PLEIEPENGER_LIVETS_SLUTTFASE_TOPIC)
            kafkaTemplate.partitionsFor(MOTTATT_ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN_TOPIC)
            kafkaTemplate.partitionsFor(PLEIEPENGER_SYKT_BARN_TOPIC)

            Mono.just(Health.up().withDetail(NAME, "Tilkobling til Kafka OK!").build())
        } catch (cause: Throwable) {
            logger.error("Feil ved tilkobling til Kafka", cause)
            Mono.just(Health.down(cause).withDetail(NAME, "Feil ved tilkobling mot Kafka").build())
        }
    }
}