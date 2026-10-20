package no.nav.brukerdialog.dato

import no.nav.brukerdialog.common.Constants
import no.nav.brukerdialog.config.Issuers
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/dato")
@RequiredIssuers(
    ProtectedWithClaims(issuer = Issuers.TOKEN_X, claimMap = ["acr=Level4"])
)
class DatoController {

    @GetMapping("/dagens")
    fun dagensDato(): DagensDatoResponse = DagensDatoResponse(dato = LocalDate.now(Constants.OSLO_ZONE_ID).toString())
}

data class DagensDatoResponse(
    val dato: String,
)
