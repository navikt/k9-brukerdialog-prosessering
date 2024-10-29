package no.nav.brukerdialog.ytelse.opplæringspenger.api

import kotlinx.coroutines.runBlocking
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.common.formaterStatuslogging
import no.nav.brukerdialog.config.Issuers
import no.nav.brukerdialog.domenetjenester.innsending.InnsendingCache
import no.nav.brukerdialog.domenetjenester.innsending.InnsendingService
import no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag.BarnService
import no.nav.brukerdialog.metrikk.MetrikkService
import no.nav.brukerdialog.utils.MDCUtil
import no.nav.brukerdialog.utils.NavHeaders
import no.nav.brukerdialog.utils.TokenUtils.personIdent
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.PleiepengerSyktBarnSøknad
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pleiepenger-sykt-barn")
@RequiredIssuers(
    ProtectedWithClaims(issuer = Issuers.TOKEN_X, claimMap = ["acr=Level4"])
)
class OpplæringspengerController(
    private val innsendingService: InnsendingService,
    private val innsendingCache: InnsendingCache,
    private val barnService: BarnService,
    private val springTokenValidationContextHolder: SpringTokenValidationContextHolder,
    private val metrikkService: MetrikkService,
) {
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(OpplæringspengerController::class.java)
    }

    @PostMapping("/innsending", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun innsending(
        @RequestHeader(NavHeaders.BRUKERDIALOG_YTELSE) ytelse: String,
        @RequestHeader(NavHeaders.BRUKERDIALOG_GIT_SHA) gitSha: String,
        @RequestBody søknad: PleiepengerSyktBarnSøknad,
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
