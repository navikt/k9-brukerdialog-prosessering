package no.nav.brukerdialog.oppslag.soker

import kotlinx.coroutines.runBlocking
import no.nav.brukerdialog.config.Issuers
import no.nav.brukerdialog.utils.NavHeaders
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/oppslag/soker")
@RequiredIssuers(
    ProtectedWithClaims(issuer = Issuers.TOKEN_X, claimMap = ["acr=Level4"])
)
class SøkerController(private val søkerService: SøkerService) {

    @GetMapping
    fun hentSøker(): Søker = runBlocking {
        søkerService.hentSøker()
    }
}
