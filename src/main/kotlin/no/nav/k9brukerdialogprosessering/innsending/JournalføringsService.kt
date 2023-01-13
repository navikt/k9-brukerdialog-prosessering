package no.nav.k9brukerdialogprosessering.innsending

import no.nav.k9brukerdialogprosessering.kafka.types.Journalfort
import org.springframework.stereotype.Service

@Service
class JournalføringsService {
    private companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(JournalføringsService::class.java)
    }

    fun journalfør(preprosessertSøknad: Preprosessert): Journalfort {
        logger.info("Journalfører søknad: $preprosessertSøknad")
        return Journalfort(journalpostId = "123456789", søknad = preprosessertSøknad.k9FormatSøknad())
    }
}
