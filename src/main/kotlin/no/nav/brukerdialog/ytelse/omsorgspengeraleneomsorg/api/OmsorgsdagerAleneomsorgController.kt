package no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.api

import kotlinx.coroutines.runBlocking
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.common.formaterStatuslogging
import no.nav.brukerdialog.config.Issuers
import no.nav.brukerdialog.domenetjenester.innsending.DuplikatInnsendingSjekker
import no.nav.brukerdialog.domenetjenester.innsending.InnsendingService
import no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag.BarnService
import no.nav.brukerdialog.metrikk.MetrikkService
import no.nav.brukerdialog.utils.MDCUtil
import no.nav.brukerdialog.utils.NavHeaders
import no.nav.brukerdialog.utils.TokenUtils.personIdent
import no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.api.domene.OmsorgsdagerAleneOmOmsorgenSøknad
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/omsorgsdager-aleneomsorg")
@RequiredIssuers(
    ProtectedWithClaims(issuer = Issuers.TOKEN_X, claimMap = ["acr=Level4"])
)
class OmsorgsdagerAleneomsorgController(
    private val innsendingService: InnsendingService,
    private val barnService: BarnService,
    private val duplikatInnsendingSjekker: DuplikatInnsendingSjekker,
    private val springTokenValidationContextHolder: SpringTokenValidationContextHolder,
    private val metrikkService: MetrikkService,
) {
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(OmsorgsdagerAleneomsorgController::class.java)
    }

    @PostMapping("/innsending")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun innsending(
        @RequestHeader(NavHeaders.BRUKERDIALOG_GIT_SHA) gitSha: String,
        @RequestBody søknad: OmsorgsdagerAleneOmOmsorgenSøknad,
    ) = runBlocking {
        val metadata = MetaInfo(correlationId = MDCUtil.callIdOrNew(), soknadDialogCommitSha = gitSha)
        val cacheKey = "${springTokenValidationContextHolder.personIdent()}_${søknad.ytelse()}"

        logger.info(formaterStatuslogging(søknad.ytelse(), søknad.søknadId, "mottatt."))

        søknad.leggTilIdentifikatorPåBarnHvisMangler(barnService.hentBarn())

        if (søknad.gjelderFlereBarn()) {
            val søknader = søknad.splittTilEgenSøknadPerBarn()
            logger.info("SøknadId:${søknad.søknadId} splittet ut til ${søknader.map { it.søknadId }}")
            søknader.forEach {
                innsendingService.registrer(it, metadata)
            }
        } else {
            innsendingService.registrer(søknad, metadata)
        }
        duplikatInnsendingSjekker.forsikreIkkeDuplikatInnsending(cacheKey)
        metrikkService.registrerMottattSøknad(søknad.ytelse())
    }
}
