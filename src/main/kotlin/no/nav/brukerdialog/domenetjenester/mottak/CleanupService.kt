package no.nav.brukerdialog.domenetjenester.mottak

import no.nav.brukerdialog.kafka.types.Cleanup
import no.nav.brukerdialog.mellomlagring.dokument.DokumentEier
import no.nav.brukerdialog.mellomlagring.dokument.DokumentService
import org.springframework.stereotype.Service

@Service
class CleanupService<T : Preprosessert>(
    private val dokumentService: DokumentService,
) {
    private companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(CleanupService::class.java)
    }

    suspend fun cleanup(cleanup: Cleanup<T>): Cleanup<T> {
        val preprosessert = cleanup.melding
        val antallDokumenter = preprosessert.dokumenter().flatten().size
        logger.info("Sletter $antallDokumenter dokumenter etter prosessering...")
        dokumentService.slettDokumenter(
            dokumentIder = preprosessert.dokumenter().flatten(),
            dokumentEier = DokumentEier(preprosessert.søkerFødselsnummer())
        )
        return cleanup
    }
}
