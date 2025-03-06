package no.nav.brukerdialog.ytelse.ungdomsytelse.api

import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import no.nav.brukerdialog.config.Issuers
import no.nav.brukerdialog.utils.NavHeaders
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.inntektsrapportering.UngdomsytelseInntektsrapportering
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.UngdomsytelseOppgavebekreftelse
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.soknad.Ungdomsytelsesøknad
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.ErrorResponseException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/ungdomsytelse")
@RequiredIssuers(
    ProtectedWithClaims(issuer = Issuers.TOKEN_X, claimMap = ["acr=Level4"])
)
class UngdomsytelseController(
    private val ungdomsytelseService: UngdomsytelseService
) {
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(UngdomsytelseController::class.java)
    }

    @PostMapping("/soknad/innsending")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun innsendingUngdomsytelsesøknad(
        @RequestHeader(NavHeaders.BRUKERDIALOG_GIT_SHA) gitSha: String,
        @Value("\${ENABLE_UNDOMSYTELSE:false}") enabled: Boolean? = null,
        @Valid @RequestBody søknad: Ungdomsytelsesøknad,
    ) = runBlocking {
        if (enabled != true) {
            logger.info("Ungdomsytelse er ikke aktivert.")
            throw ErrorResponseException(HttpStatus.NOT_IMPLEMENTED)
        }
        ungdomsytelseService.innsendingUngdomsytelsesøknad(søknad, gitSha)
    }

    @PostMapping("/inntektsrapportering/innsending")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun inntektrapportering(
        @RequestHeader(NavHeaders.BRUKERDIALOG_GIT_SHA) gitSha: String,
        @Value("\${ENABLE_UNDOMSYTELSE:false}") enabled: Boolean? = null,
        @Valid @RequestBody rapportetInntekt: UngdomsytelseInntektsrapportering,
    ) = runBlocking {
        if (enabled != true) {
            logger.info("Ungdomsytelse er ikke aktivert.")
            throw ErrorResponseException(HttpStatus.NOT_IMPLEMENTED)
        }

        ungdomsytelseService.inntektrapportering(rapportetInntekt, gitSha)
    }

    @PostMapping("/oppgavebekreftelse/innsending")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun oppgavebekreftelse(
        @RequestHeader(NavHeaders.BRUKERDIALOG_GIT_SHA) gitSha: String,
        @Value("\${ENABLE_UNDOMSYTELSE:false}") enabled: Boolean? = null,
        @Valid @RequestBody bekreftetOppgave: UngdomsytelseOppgavebekreftelse,
    ) = runBlocking {
        if (enabled != true) {
            logger.info("Ungdomsytelse er ikke aktivert.")
            throw ErrorResponseException(HttpStatus.NOT_IMPLEMENTED)
        }

        ungdomsytelseService.oppgavebekreftelse(bekreftetOppgave, gitSha)
    }
}
