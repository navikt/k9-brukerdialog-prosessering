package no.nav.brukerdialog.ytelse.aktivitetspenger.api

import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import no.nav.brukerdialog.config.Issuers
import no.nav.brukerdialog.utils.NavHeaders
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.soknad.Aktivitetspengersøknad
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/aktivitetspenger")
@RequiredIssuers(
    ProtectedWithClaims(issuer = Issuers.TOKEN_X, claimMap = ["acr=Level4"])
)
class AktivitetspengerController(
    private val aktivitetspengerService: AktivitetspengerService
) {
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(AktivitetspengerController::class.java)
    }

    @PostMapping("/soknad/innsending")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun innsendingAktivitetspengersøknad(
        @RequestHeader(NavHeaders.BRUKERDIALOG_GIT_SHA) gitSha: String,
        @Valid @RequestBody søknad: Aktivitetspengersøknad,
    ) = runBlocking {
        aktivitetspengerService.innsendingAktivitetspengersøknad(søknad, gitSha)
    }

}
