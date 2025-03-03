package no.nav.brukerdialog.oppslag.barn

import kotlinx.coroutines.runBlocking
import no.nav.brukerdialog.config.Issuers
import no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag.BarnService
import no.nav.brukerdialog.utils.NavHeaders
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
    fun hentBarn(): BarnOppslagListe = runBlocking {
        val barn = barnService.hentBarn()
        BarnOppslagListe(barn)
    }

    data class BarnOppslagListe(val barn: List<BarnOppslag>)
}
