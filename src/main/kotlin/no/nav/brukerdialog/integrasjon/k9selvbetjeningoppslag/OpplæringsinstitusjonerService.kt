package no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag

import com.github.benmanes.caffeine.cache.Cache
import no.nav.brukerdialog.oppslag.opplaeringsintitusjoner.Opplæringsinstitusjon
import org.springframework.stereotype.Service

@Service
class OpplæringsinstitusjonerService(
    private val opplæringsinstitusjonCache: Cache<Unit, List<Opplæringsinstitusjon>>,
    private val opplæringsinstitusjonerOppslagsService: OpplæringsinstitusjonerOppslagsService
) {
    private val cacheKey = Unit;
    internal suspend fun hentOpplæringsinstitusjoner(): List<Opplæringsinstitusjon> {
        val opplæringsinstitusjonerFraCache = opplæringsinstitusjonCache.getIfPresent(cacheKey)
        if (opplæringsinstitusjonerFraCache != null) {
            return opplæringsinstitusjonerFraCache
        }
        return try {
            val opplæringsinstitusjoner = opplæringsinstitusjonerOppslagsService.hentOpplæringsinstitusjoner()
            opplæringsinstitusjonCache.put(cacheKey, opplæringsinstitusjoner)
            opplæringsinstitusjoner
        } catch (cause: Throwable) {
            when (cause) {
                else -> {
                    emptyList()
                }
            }
        }
    }
}