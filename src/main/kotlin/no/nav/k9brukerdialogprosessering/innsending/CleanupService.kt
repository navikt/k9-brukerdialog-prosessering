package no.nav.k9brukerdialogprosessering.innsending

import no.nav.k9brukerdialogprosessering.kafka.types.Cleanup
import no.nav.k9brukerdialogprosessering.mellomlagring.dokument.DokumentEier
import no.nav.k9brukerdialogprosessering.mellomlagring.dokument.K9DokumentMellomlagringService
import org.springframework.stereotype.Service

@Service
class CleanupService<T: Preprosessert>(
    private val k9DokumentMellomlagringService: K9DokumentMellomlagringService
) {
    private companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(CleanupService::class.java)
    }

    suspend fun cleanup(cleanup: Cleanup<T>): Cleanup<T> {
        val preprosessert = cleanup.melding
        val antallDokumenter = preprosessert.dokumenter().flatten().size
        logger.info("Sletter $antallDokumenter dokumenter etter prosessering...")
        k9DokumentMellomlagringService.slettDokumenter(
            dokumentIder = preprosessert.dokumenter().flatten(),
            dokumentEier = DokumentEier(preprosessert.søkerFødselsnummer())
        )
        return cleanup
    }
}
