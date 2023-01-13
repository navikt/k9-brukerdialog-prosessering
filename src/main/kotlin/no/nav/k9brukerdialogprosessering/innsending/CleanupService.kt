package no.nav.k9brukerdialogprosessering.innsending

import no.nav.k9brukerdialogprosessering.kafka.types.Cleanup
import org.springframework.stereotype.Service

@Service
class CleanupService<V: Preprosessert> {
    private companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(CleanupService::class.java)
    }

    fun cleanup(cleanup: Cleanup<V>): Cleanup<V> {
        logger.info("Sletter dokumenter etter prosessering av søknad: $cleanup")
        return cleanup
    }
}
