package no.nav.k9brukerdialogprosessering.oppslag.barn

import com.github.benmanes.caffeine.cache.Cache
import no.nav.k9brukerdialogprosessering.api.ytelse.Ytelse
import no.nav.k9brukerdialogprosessering.oppslag.TilgangNektetException
import no.nav.k9brukerdialogprosessering.utils.personIdent
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class BarnService(
    private val barnOppslagsService: BarnOppslagsService,
    private val barnCache: Cache<String, List<BarnOppslag>>,
    private val springTokenValidationContextHolder: SpringTokenValidationContextHolder,
) {
    private val logger: Logger = LoggerFactory.getLogger(BarnService::class.java)

    internal suspend fun hentBarn(ytelse: Ytelse): List<BarnOppslag> {
        val personIdent = springTokenValidationContextHolder.personIdent()
        val barnFraCache = barnCache.getIfPresent(personIdent)
        if (barnFraCache != null) return barnFraCache

        return try {
            val barn = barnOppslagsService.hentBarn(ytelse = ytelse).map { it.tilBarnOppslag() }
            barnCache.put(personIdent, barn)
            barn
        } catch (cause: Throwable) {
            when (cause) {
                is TilgangNektetException -> throw cause
                else -> {
                    logger.error("Feil ved henting av barn, returnerer en tom liste", cause)
                    emptyList()
                }
            }
        }
    }
}
