package no.nav.k9brukerdialogprosessering.innsending

import no.nav.k9brukerdialogprosessering.journalforing.K9JoarkService
import no.nav.k9brukerdialogprosessering.kafka.types.Journalfort
import org.springframework.stereotype.Service

@Service
class JournalføringsService(
    private val k9JoarkService: K9JoarkService
) {
    private companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(JournalføringsService::class.java)
    }

    suspend fun journalfør(preprosessertSøknad: Preprosessert): Journalfort {
        logger.info("Journalfører søknad: $preprosessertSøknad")
        val journalføringsResponse = k9JoarkService.journalfør(preprosessertSøknad.tilJournaførigsRequest())
        return Journalfort(journalpostId = journalføringsResponse.journalPostId, søknad = preprosessertSøknad.k9FormatSøknad())
    }
}
