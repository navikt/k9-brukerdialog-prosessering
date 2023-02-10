package no.nav.k9brukerdialogprosessering.endringsmelding

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.k9brukerdialogprosessering.endringsmelding.domene.PSBEndringsmeldingMottatt
import no.nav.k9brukerdialogprosessering.endringsmelding.domene.PSBPreprossesertEndringsmelding
import no.nav.k9brukerdialogprosessering.kafka.config.SerDes
import no.nav.k9brukerdialogprosessering.kafka.config.Topic
import no.nav.k9brukerdialogprosessering.kafka.types.Cleanup
import no.nav.k9brukerdialogprosessering.kafka.types.TopicEntry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PSBEndringsmeldingTopologyConfiguration(
    private val objectMapper: ObjectMapper,
) {
    companion object {
        const val PSB_ENDRINGSMELDING_MOTTATT_TOPIC = "dusseldorf.privat-endringsmelding-pleiepenger-sykt-barn-mottatt"
        const val PSB_ENDRINGSMELDING_PREPROSESSERT_TOPIC = "dusseldorf.privat-endringsmelding-pleiepenger-sykt-barn-preprosessert"
        const val PSB_ENDRINGSMELDING_CLEANUP_TOPIC = "dusseldorf.privat-endringsmelding-pleiepenger-sykt-barn-cleanup"
    }

    @Bean
    fun psbEndringsmeldingMottattTopic(): Topic<TopicEntry<PSBEndringsmeldingMottatt>> {
        return Topic(
            name = PSB_ENDRINGSMELDING_MOTTATT_TOPIC,
            serDes = PSBEndringsmeldingMottattSerdes(objectMapper)
        )
    }

    @Bean
    fun psbEndringsmeldingPreprosessertTopic(): Topic<TopicEntry<PSBPreprossesertEndringsmelding>> {
        return Topic(
            name = PSB_ENDRINGSMELDING_PREPROSESSERT_TOPIC,
            serDes = PSBEndringsmeldingPreprosessertSerdes(objectMapper)
        )
    }

    @Bean
    fun psbEndringsmeldingCleanupTopic(): Topic<TopicEntry<Cleanup<PSBPreprossesertEndringsmelding>>> {
        return Topic(
            name = PSB_ENDRINGSMELDING_CLEANUP_TOPIC,
            serDes = CleanupPSBEndringsmeldingSerdes(objectMapper)
        )
    }
}

class PSBEndringsmeldingMottattSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<PSBEndringsmeldingMottatt>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<PSBEndringsmeldingMottatt> {
        return objectMapper.readValue(data)
    }
}

class PSBEndringsmeldingPreprosessertSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<PSBPreprossesertEndringsmelding>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<PSBPreprossesertEndringsmelding> {
        return objectMapper.readValue(data)
    }
}

class CleanupPSBEndringsmeldingSerdes(
    private val objectMapper: ObjectMapper,
) : SerDes<TopicEntry<Cleanup<PSBPreprossesertEndringsmelding>>>(objectMapper) {
    override fun deserialize(topic: String, data: ByteArray): TopicEntry<Cleanup<PSBPreprossesertEndringsmelding>> {
        return objectMapper.readValue(data)
    }
}
