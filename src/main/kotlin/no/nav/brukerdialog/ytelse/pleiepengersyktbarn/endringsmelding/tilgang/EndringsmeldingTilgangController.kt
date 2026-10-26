package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.tilgang

import no.nav.brukerdialog.config.Issuers
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import org.springframework.http.CacheControl
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Vurderer om innlogget bruker kan bruke endringsmeldingen for pleiepenger. Kalles av frontend ved oppstart.
 *
 * Tar ingen parametere. Saker og arbeidsgivere hentes på vegne av brukeren via TokenX, så bruker kan bare
 * få vurdert egne data.
 */
@RestController
@RequestMapping("/pleiepenger-sykt-barn/endringsmelding")
@RequiredIssuers(
    ProtectedWithClaims(issuer = Issuers.TOKEN_X, claimMap = ["acr=Level4"])
)
class EndringsmeldingTilgangController(
    private val endringsmeldingTilgangService: EndringsmeldingTilgangService,
) {

    @GetMapping("/tilgang", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hentTilgang(): ResponseEntity<TilgangResultat> =
        ResponseEntity.ok()
            // Svaret er utledet av brukerens saker og skal ikke mellomlagres i nettleser eller proxy.
            .cacheControl(CacheControl.noStore())
            .body(endringsmeldingTilgangService.vurderTilgang())
}
