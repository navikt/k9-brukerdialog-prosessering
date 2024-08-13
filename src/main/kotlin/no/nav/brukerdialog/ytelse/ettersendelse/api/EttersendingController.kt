package no.nav.brukerdialog.ytelse.ettersendelse.api

import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import no.nav.brukerdialog.domenetjenester.innsending.InnsendingCache
import no.nav.brukerdialog.domenetjenester.innsending.InnsendingService
import no.nav.brukerdialog.metrikk.MetrikkService
import no.nav.brukerdialog.ytelse.ettersendelse.api.domene.Ettersendelse
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.common.formaterStatuslogging
import no.nav.brukerdialog.config.Issuers
import no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag.BarnService
import no.nav.brukerdialog.utils.MDCUtil
import no.nav.brukerdialog.utils.NavHeaders
import no.nav.brukerdialog.utils.TokenUtils.personIdent
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/ettersending")
@RequiredIssuers(
    ProtectedWithClaims(issuer = Issuers.TOKEN_X, claimMap = ["acr=Level4"])
)
@Validated
class EttersendingController(
    private val innsendingService: InnsendingService,
    private val barnService: BarnService,
    private val innsendingCache: InnsendingCache,
    private val springTokenValidationContextHolder: SpringTokenValidationContextHolder,
    private val metrikkService: MetrikkService,
) {
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(EttersendingController::class.java)
    }

    @PostMapping("/innsending")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun innsending(
        @RequestHeader(NavHeaders.BRUKERDIALOG_YTELSE) ytelse: String,
        @RequestHeader(NavHeaders.BRUKERDIALOG_GIT_SHA) gitSha: String,
        @Valid @RequestBody ettersendelse: Ettersendelse,
    ) = runBlocking {
        val metadata = MetaInfo(correlationId = MDCUtil.callIdOrNew(), soknadDialogCommitSha = gitSha)
        val cacheKey = "${springTokenValidationContextHolder.personIdent()}_${ettersendelse.ytelse()}"

        logger.info(formaterStatuslogging(ettersendelse.ytelse(), ettersendelse.søknadId, "mottatt."))
        logger.info("Ettersending for ytelse ${ettersendelse.søknadstype}")

        val barnFraOppslag = barnService.hentBarn()
        ettersendelse.leggTilIdentifikatorPåBarnHvisMangler(barnFraOppslag)

        innsendingCache.put(cacheKey)
        innsendingService.registrer(ettersendelse, metadata)
        metrikkService.registrerMottattSøknad(ettersendelse.ytelse())
    }
}
