package no.nav.brukerdialog.domenetjenester.mottak

import no.nav.k9.søknad.Søknad
import no.nav.brukerdialog.integrasjon.k9joark.JournalføringsResponse
import no.nav.brukerdialog.integrasjon.k9joark.K9JoarkService
import no.nav.brukerdialog.kafka.types.Journalfort
import no.nav.brukerdialog.kafka.types.JournalfortEttersendelse
import no.nav.brukerdialog.kafka.types.JournalfortSøknad
import org.springframework.stereotype.Service
import no.nav.k9.ettersendelse.Ettersendelse as K9Ettersendelse

@Service
class JournalføringsService(
    private val k9JoarkService: K9JoarkService,
) {
    private companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(JournalføringsService::class.java)
    }

    suspend fun journalfør(preprosessertSøknad: Preprosessert): Journalfort {
        logger.info("Journalfører dokumenter: ${preprosessertSøknad.dokumenter().size}")
        val journalføringsResponse = k9JoarkService.journalfør(preprosessertSøknad.tilJournaførigsRequest())
        return resolve(preprosessertSøknad, journalføringsResponse)
    }

    private fun resolve(
        preprosessertSøknad: Preprosessert,
        journalføringsResponse: JournalføringsResponse,
    ) = when (val innsending = preprosessertSøknad.k9FormatSøknad()) {
        is Søknad -> JournalfortSøknad(journalpostId = journalføringsResponse.journalPostId, søknad = innsending)
        is K9Ettersendelse -> JournalfortEttersendelse(
            journalpostId = journalføringsResponse.journalPostId,
            søknad = innsending
        )

        else -> {
            logger.error("Ukjent søknadstype: $innsending")
            throw IllegalStateException("Ukjent søknadstype: $innsending")
        }
    }
}
