package no.nav.brukerdialog.oppslag.arbeidsgiver

import no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag.ArbeidsgivereOppslagsService
import no.nav.k9brukerdialogapi.oppslag.arbeidsgiver.Arbeidsgivere
import no.nav.brukerdialog.oppslag.TilgangNektetException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ArbeidsgiverService(
    private val arbeidsgivereOppslagsService: ArbeidsgivereOppslagsService,
) {
    private val logger: Logger = LoggerFactory.getLogger(ArbeidsgiverService::class.java)

    suspend fun hentArbedisgivere(
        fraOgMed: LocalDate,
        tilOgMed: LocalDate,
        skalHentePrivateArbeidsgivere: Boolean,
        skalHenteFrilansoppdrag: Boolean,
    ): Arbeidsgivere = try {
        arbeidsgivereOppslagsService.hentArbeidsgivere(
            fraOgMed = fraOgMed,
            tilOgMed = tilOgMed,
            skalHentePrivateArbeidsgivere = skalHentePrivateArbeidsgivere,
            skalHenteFrilansoppdrag = skalHenteFrilansoppdrag
        ).arbeidsgivere
    } catch (cause: Throwable) {
        when (cause) {
            is TilgangNektetException -> throw cause
            else -> {
                logger.error("Feil ved henting av arbeidsgivere, returnerer en tom liste", cause)
                Arbeidsgivere(emptyList(), emptyList(), emptyList())
            }
        }
    }
}
