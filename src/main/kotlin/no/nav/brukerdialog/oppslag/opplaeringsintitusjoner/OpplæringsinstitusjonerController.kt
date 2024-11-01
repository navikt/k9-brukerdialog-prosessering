package no.nav.brukerdialog.oppslag.opplaeringsintitusjoner

import kotlinx.coroutines.runBlocking
import no.nav.brukerdialog.config.Issuers
import no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag.OpplæringsinstitusjonerService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/oppslag/opplæringsinstitusjoner")
@RequiredIssuers(
    ProtectedWithClaims(issuer = Issuers.TOKEN_X, claimMap = ["acr=Level4"])
)
class OpplæringsinstitusjonerController(
    private val opplæringsinstitusjonerService: OpplæringsinstitusjonerService
) {
    @GetMapping
    fun hentAlleOpplæringsinstitusjoner(): List<Opplæringsinstitusjon> = runBlocking {
        opplæringsinstitusjonerService.hentOpplæringsinstitusjoner()
    }
}