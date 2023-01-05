package no.nav.k9brukerdialogprosessering.pleiepengersyktbarn

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene.PSBMottattSøknad
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
//@Transactional(transactionManager = "kafkaTransactionManager")
class PleiepengerSyktBarnSøknadKonsument(
    private val mapper: ObjectMapper,
    private val preprosesseringsService: PSBPreprosesseringsService,
) {
    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(PleiepengerSyktBarnSøknadKonsument::class.java)

        const val MOTTATT = "dusseldorf.privat-pleiepengesoknad-mottatt-v2"
        const val MOTTATT_RETRY = "dusseldorf.privat-pleiepengesoknad-mottatt-v2-retry"
        const val MOTTATT_DLT = "dusseldorf.privat-pleiepengesoknad-mottatt-v2-dlt"
        const val PREPROSESSERT = "dusseldorf.privat-pleiepengesoknad-preprosessert"
        const val CLEANUP = "dusseldorf.privat-pleiepengesoknad-cleanup"
    }

    /**
     * Lytter etter innkommende meldinger på MOTTATT Kafka-topicen, preprosesserer meldingen,
     * og sender den behandlede meldingen til den PREPROSESSERT topic.
     *
     * @param søknad den mottatte pleiepengesøknaden som skal preprosesseres.
     * @return den preprosesseres pleiepengesøknaden.
     */
    @KafkaListener(topics=[MOTTATT], errorHandler = "errorHandler", id = "pleiepenger-sykt-barn-mottak-listener")
    @SendTo(PREPROSESSERT)
    fun preprosesser(søknad: String): String {
        logger.info("Mottatt søknad: $søknad")
        val psbMottattSøknad = mapper.readValue(søknad, PSBMottattSøknad::class.java)
        val psbPreprosessertSøknad = preprosesseringsService.preprosesser(psbMottattSøknad)
        return mapper.writeValueAsString(psbPreprosessertSøknad)
    }

    @KafkaListener(topics=[PREPROSESSERT], errorHandler = "errorHandler", id = "pleiepenger-sykt-barn-preprosessering-listener")
    @SendTo(CLEANUP)
    fun journalfør(søknad: String): String {
        logger.info("Journalfører søknad: $søknad")
        return søknad
    }

    @KafkaListener(topics=[CLEANUP], errorHandler = "errorHandler", id = "pleiepenger-sykt-barn-cleanup-listener")
    @SendTo(CLEANUP)
    fun cleanup(søknad: String): String {
        logger.info("Cleanup søknad: $søknad")
        return søknad
    }

}
