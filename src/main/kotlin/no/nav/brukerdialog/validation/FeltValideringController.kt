package no.nav.brukerdialog.validation

import jakarta.validation.Valid
import no.nav.brukerdialog.config.Issuers
import no.nav.brukerdialog.validation.fritekst.ValidFritekst
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/valider")
@RequiredIssuers(
    ProtectedWithClaims(issuer = Issuers.TOKEN_X, claimMap = ["acr=Level4"])
)
class FeltValideringController {

    @PostMapping("/friteksfelt")
    fun validerFriteksfelt(@RequestBody @Valid friteksfelt: Friteksfelt) {
        // Validering av fritekstfelt
    }

    data class Friteksfelt(
        @field:ValidFritekst
        val verdi: String,
    )
}
