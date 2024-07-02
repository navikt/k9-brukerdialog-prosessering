package no.nav.k9brukerdialogprosessering.api.ytelse.omsorgsdageraleneomsorg

import no.nav.k9brukerdialogprosessering.api.innsending.InnsendingCache
import no.nav.k9brukerdialogprosessering.api.innsending.InnsendingService
import no.nav.k9brukerdialogprosessering.api.ytelse.Ytelse
import no.nav.k9brukerdialogprosessering.api.ytelse.omsorgsdageraleneomsorg.domene.OmsorgsdagerAleneOmOmsorgenSøknad
import no.nav.k9brukerdialogprosessering.api.ytelse.registrerMottattSøknad
import no.nav.k9brukerdialogprosessering.common.MetaInfo
import no.nav.k9brukerdialogprosessering.common.formaterStatuslogging
import no.nav.k9brukerdialogprosessering.config.Issuers
import no.nav.k9brukerdialogprosessering.oppslag.barn.BarnService
import no.nav.k9brukerdialogprosessering.utils.MDCUtil
import no.nav.k9brukerdialogprosessering.utils.NavHeaders
import no.nav.k9brukerdialogprosessering.utils.personIdent
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
@RequestMapping("//omsorgsdager-aleneomsorg")
@RequiredIssuers(
    ProtectedWithClaims(issuer = Issuers.TOKEN_X, claimMap = ["acr=Level4"])
)
class EttersendingController(
    private val innsendingService: InnsendingService,
    private val barnService: BarnService,
    private val innsendingCache: InnsendingCache,
    private val springTokenValidationContextHolder: SpringTokenValidationContextHolder,
) {
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(EttersendingController::class.java)
    }

    @PostMapping("/innsending")
    @ResponseStatus(HttpStatus.ACCEPTED)
    suspend fun innsending(
        @RequestHeader(NavHeaders.BRUKERDIALOG_YTELSE) ytelse: Ytelse,
        @RequestHeader(NavHeaders.BRUKERDIALOG_GIT_SHA) gitSha: String,
        @RequestBody søknad: OmsorgsdagerAleneOmOmsorgenSøknad,
    ) {
        val metadata = MetaInfo(correlationId = MDCUtil.callIdOrNew(), soknadDialogCommitSha = gitSha)
        val cacheKey = "${springTokenValidationContextHolder.personIdent()}_${søknad.ytelse()}"

        logger.info(formaterStatuslogging(søknad.ytelse(), søknad.søknadId, "mottatt."))

        søknad.leggTilIdentifikatorPåBarnHvisMangler(barnService.hentBarn(ytelse))

        if (søknad.gjelderFlereBarn()) {
            val søknader = søknad.splittTilEgenSøknadPerBarn()
            logger.info("SøknadId:${søknad.søknadId} splittet ut til ${søknader.map { it.søknadId }}")
            søknader.forEach {
                innsendingService.registrer(it, metadata, ytelse)
            }
        } else {
            innsendingService.registrer(søknad, metadata, ytelse)
        }
        innsendingCache.put(cacheKey)
        registrerMottattSøknad(søknad.ytelse())
    }
}
