package no.nav.brukerdialog.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.brukerdialog.ytelse.Ytelse
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.common.formaterStatuslogging
import no.nav.brukerdialog.kafka.Topics.ETTERSENDING_TOPIC
import no.nav.brukerdialog.kafka.Topics.MOTTATT_ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN_TOPIC
import no.nav.brukerdialog.kafka.Topics.OMSORGSDAGER_ALENEOMSORG_TOPIC
import no.nav.brukerdialog.kafka.Topics.OMSORGSPENGER_MIDLERTIDIG_ALENE_TOPIC
import no.nav.brukerdialog.kafka.Topics.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER_TOPIC
import no.nav.brukerdialog.kafka.Topics.OMSORGSPENGER_UTBETALING_SNF_TOPIC
import no.nav.brukerdialog.kafka.Topics.OMSORGSPENGER_UTVIDET_RETT_TOPIC
import no.nav.brukerdialog.kafka.Topics.PLEIEPENGER_LIVETS_SLUTTFASE_TOPIC
import no.nav.brukerdialog.kafka.Topics.PLEIEPENGER_SYKT_BARN_TOPIC
import no.nav.brukerdialog.kafka.Topics.UNGDOMSYTELSE_SOKNAD_TOPIC
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
class KafkaProducerService(
    val aivenKafkaTemplate: KafkaTemplate<String, String>,
    val objectMapper: ObjectMapper,
) : ReactiveHealthIndicator {
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
            JSONObject(TopicEntry(
                metadata = metadata,
                data = komplettSøknadSomJson
            )).toString()
        )
        val result = aivenKafkaTemplate.executeInTransaction {
            it.send(producerRecord).get()
        }
        logger.info(
            formaterStatuslogging(
                ytelse,
                komplettSøknadSomJson.getString("søknadId"),
                "sendes til topic $topic med offset '${result.recordMetadata.offset()}' til partition '${result.recordMetadata.offset()}'"
            )
        )
    }

    override fun health(): Mono<Health> {
        return try {
            aivenKafkaTemplate.partitionsFor(OMSORGSPENGER_UTVIDET_RETT_TOPIC)
            aivenKafkaTemplate.partitionsFor(OMSORGSPENGER_MIDLERTIDIG_ALENE_TOPIC)
            aivenKafkaTemplate.partitionsFor(ETTERSENDING_TOPIC)
            aivenKafkaTemplate.partitionsFor(OMSORGSDAGER_ALENEOMSORG_TOPIC)
            aivenKafkaTemplate.partitionsFor(OMSORGSPENGER_UTBETALING_ARBEIDSTAKER_TOPIC)
            aivenKafkaTemplate.partitionsFor(OMSORGSPENGER_UTBETALING_SNF_TOPIC)
            aivenKafkaTemplate.partitionsFor(PLEIEPENGER_LIVETS_SLUTTFASE_TOPIC)
            aivenKafkaTemplate.partitionsFor(MOTTATT_ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN_TOPIC)
            aivenKafkaTemplate.partitionsFor(PLEIEPENGER_SYKT_BARN_TOPIC)
            aivenKafkaTemplate.partitionsFor(UNGDOMSYTELSE_SOKNAD_TOPIC)

            Mono.just(Health.up().withDetail(NAME, "Tilkobling til Kafka OK!").build())
        } catch (cause: Throwable) {
            logger.error("Feil ved tilkobling til Kafka", cause)
            Mono.just(Health.down(cause).withDetail(NAME, "Feil ved tilkobling mot Kafka").build())
        }
    }
}
