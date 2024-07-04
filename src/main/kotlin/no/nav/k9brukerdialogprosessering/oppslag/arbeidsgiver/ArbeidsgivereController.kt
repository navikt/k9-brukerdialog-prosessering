package no.nav.k9brukerdialogprosessering.oppslag.arbeidsgiver

import no.nav.k9brukerdialogapi.oppslag.arbeidsgiver.Arbeidsgivere
import no.nav.k9brukerdialogprosessering.api.ytelse.Ytelse
import no.nav.k9brukerdialogprosessering.config.Issuers
import no.nav.k9brukerdialogprosessering.utils.NavHeaders
import no.nav.k9brukerdialogprosessering.validation.ValidationErrorResponseException
import no.nav.k9brukerdialogprosessering.validation.ValidationProblemDetails
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/arbeidsgiver")
@RequiredIssuers(
    ProtectedWithClaims(issuer = Issuers.TOKEN_X, claimMap = ["acr=Level4"])
)
class ArbeidsgivereController(private val arbeidsgiverService: ArbeidsgiverService) {

    @GetMapping
    suspend fun hentArbeidsgivere(
        @RequestHeader(NavHeaders.BRUKERDIALOG_YTELSE) ytelse: Ytelse,
        @RequestParam("fra_og_med", required = true) fraOgMed: String,
        @RequestParam("til_og_med", required = true) tilOgMed: String,
        @RequestParam("frilansoppdrag", required = false, defaultValue = "false") frilansoppdrag: Boolean,
        @RequestParam("private_arbeidsgivere", required = false, defaultValue = "false") privateArbeidsgivere: Boolean,
    ): Arbeidsgivere {

        val valideringsfeil = FraOgMedTilOgMedValidator.valider(fraOgMed, tilOgMed)
        if (valideringsfeil.isNotEmpty()) throw ValidationErrorResponseException(
            ValidationProblemDetails(
                valideringsfeil
            )
        )

        val arbeidsgivere = arbeidsgiverService.hentArbedisgivere(
            fraOgMed = LocalDate.parse(fraOgMed),
            tilOgMed = LocalDate.parse(tilOgMed),
            skalHentePrivateArbeidsgivere = privateArbeidsgivere,
            skalHenteFrilansoppdrag = frilansoppdrag
        )

        return arbeidsgivere
    }
}
