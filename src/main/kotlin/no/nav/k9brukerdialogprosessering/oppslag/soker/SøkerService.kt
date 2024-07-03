package no.nav.k9brukerdialogprosessering.oppslag.soker

import no.nav.k9brukerdialogprosessering.utils.personIdent
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.springframework.stereotype.Service

@Service
class SøkerService(
    private val søkerOppslagsService: SøkerOppslagsService,
    private val tokenValidationContextHolder: SpringTokenValidationContextHolder
) {
    suspend fun hentSøker(): Søker {
        val søker = søkerOppslagsService.hentSøker()
        return søker.tilSøker(tokenValidationContextHolder.personIdent())
    }
}
