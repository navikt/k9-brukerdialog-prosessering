package no.nav.k9brukerdialogprosessering.innsending

import no.nav.k9brukerdialogprosessering.kafka.types.Cleanup
import org.springframework.stereotype.Service

@Service
class CleanupService {
    private companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(CleanupService::class.java)
    }

    fun cleanup(cleanup: Cleanup): Cleanup {
        logger.info("Sletter dokumenter etter prosessering av søknad: $cleanup")
        return cleanup
    }
}
