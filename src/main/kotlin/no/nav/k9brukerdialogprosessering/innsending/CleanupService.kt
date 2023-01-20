package no.nav.k9brukerdialogprosessering.innsending

import no.nav.k9brukerdialogprosessering.kafka.types.Cleanup
import no.nav.k9brukerdialogprosessering.mellomlagring.DokumentEier
import no.nav.k9brukerdialogprosessering.mellomlagring.K9MellomlagringService
import org.springframework.stereotype.Service

@Service
class CleanupService(
    private val k9MellomlagringService: K9MellomlagringService
) {
    private companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(CleanupService::class.java)
    }

    suspend fun cleanup(cleanup: Cleanup): Cleanup {
        logger.info("Sletter dokumenter etter prosessering av søknad: $cleanup")
        val preprosessert = cleanup.melding
        k9MellomlagringService.slettDokumeter(
            dokumentIder = preprosessert.dokumenter().flatten(),
            dokumentEier = DokumentEier(preprosessert.søkerFødselsnummer())
        )
        return cleanup
    }
}
