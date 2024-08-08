package no.nav.k9brukerdialogprosessering.oppslag.barn

import kotlinx.coroutines.runBlocking
import no.nav.k9brukerdialogprosessering.config.Issuers
import no.nav.k9brukerdialogprosessering.utils.NavHeaders
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/oppslag/barn")
@RequiredIssuers(
    ProtectedWithClaims(issuer = Issuers.TOKEN_X, claimMap = ["acr=Level4"])
)
class BarnController(private val barnService: BarnService) {

    @GetMapping
    fun hentBarn(@RequestHeader(NavHeaders.BRUKERDIALOG_YTELSE) ytelse: String): List<BarnOppslag>  = runBlocking {
        barnService.hentBarn()
    }
}
