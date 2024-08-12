package no.nav.brukerdialog.api.ytelse.omsorgspengerutvidetrett

import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import no.nav.brukerdialog.api.innsending.InnsendingCache
import no.nav.brukerdialog.api.innsending.InnsendingService
import no.nav.brukerdialog.api.ytelse.MetrikkService
import no.nav.brukerdialog.api.ytelse.omsorgspengerutvidetrett.domene.OmsorgspengerKroniskSyktBarnSøknad
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.common.formaterStatuslogging
import no.nav.brukerdialog.config.Issuers
import no.nav.brukerdialog.oppslag.barn.BarnService
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
@RequestMapping("/omsorgspenger-utvidet-rett")
@RequiredIssuers(
    ProtectedWithClaims(issuer = Issuers.TOKEN_X, claimMap = ["acr=Level4"])
)
@Validated
class OmsorgspengerUtvidetRettController(
    private val innsendingService: InnsendingService,
    private val barnService: BarnService,
    private val innsendingCache: InnsendingCache,
    private val springTokenValidationContextHolder: SpringTokenValidationContextHolder,
    private val metrikkService: MetrikkService,
) {
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(OmsorgspengerUtvidetRettController::class.java)
    }

    @PostMapping("/innsending")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun innsending(
        @RequestHeader(NavHeaders.BRUKERDIALOG_YTELSE) ytelse: String,
        @RequestHeader(NavHeaders.BRUKERDIALOG_GIT_SHA) gitSha: String,
        @Valid @RequestBody søknad: OmsorgspengerKroniskSyktBarnSøknad,
    ) = runBlocking {
        val metadata = MetaInfo(correlationId = MDCUtil.callIdOrNew(), soknadDialogCommitSha = gitSha)
        val cacheKey = "${springTokenValidationContextHolder.personIdent()}_${søknad.ytelse()}"

        logger.info(formaterStatuslogging(søknad.ytelse(), søknad.søknadId, "mottatt."))
        søknad.leggTilIdentifikatorPåBarnHvisMangler(barnService.hentBarn())

        innsendingCache.put(cacheKey)
        innsendingService.registrer(søknad, metadata)
        metrikkService.registrerMottattSøknad(søknad.ytelse())
    }
}