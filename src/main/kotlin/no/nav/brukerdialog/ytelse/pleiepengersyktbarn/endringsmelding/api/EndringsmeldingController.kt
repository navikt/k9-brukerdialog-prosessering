package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.api

import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.common.formaterStatuslogging
import no.nav.brukerdialog.config.Issuers
import no.nav.brukerdialog.domenetjenester.innsending.InnsendingCache
import no.nav.brukerdialog.domenetjenester.innsending.InnsendingService
import no.nav.brukerdialog.innsyn.InnsynService
import no.nav.brukerdialog.metrikk.MetrikkService
import no.nav.brukerdialog.utils.MDCUtil
import no.nav.brukerdialog.utils.NavHeaders
import no.nav.brukerdialog.utils.TokenUtils.personIdent
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.api.domene.Endringsmelding
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarn
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
@RequestMapping("/pleiepenger-sykt-barn/endringsmelding")
@RequiredIssuers(
    ProtectedWithClaims(issuer = Issuers.TOKEN_X, claimMap = ["acr=Level4"])
)
class EndringsmeldingController(
    private val innsendingService: InnsendingService,
    private val innsendingCache: InnsendingCache,
    private val innsynService: InnsynService,
    private val springTokenValidationContextHolder: SpringTokenValidationContextHolder,
    private val metrikkService: MetrikkService
) {
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(EndringsmeldingController::class.java)
    }

    @PostMapping("/innsending")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun innsending(
        @RequestHeader(NavHeaders.BRUKERDIALOG_YTELSE) ytelse: String,
        @RequestHeader(NavHeaders.BRUKERDIALOG_GIT_SHA) gitSha: String,
        @RequestBody @Valid endringsmelding: Endringsmelding,
    ) = runBlocking {
        val metadata = MetaInfo(correlationId = MDCUtil.callIdOrNew(), soknadDialogCommitSha = gitSha)
        val cacheKey = "${springTokenValidationContextHolder.personIdent()}_${endringsmelding.ytelse()}"

        logger.info(formaterStatuslogging(endringsmelding.ytelse(), endringsmelding.søknadId, "mottatt."))

        val søknadsopplysninger = innsynService.hentSøknadsopplysningerForBarn(endringsmelding)

        val psbYtelse = søknadsopplysninger.søknad.getYtelse<PleiepengerSyktBarn>()
        endringsmelding.gyldigeEndringsPerioder = psbYtelse.søknadsperiodeList
        endringsmelding.pleietrengendeNavn = søknadsopplysninger.barn.navn()

        innsendingService.registrer(endringsmelding, metadata)
        innsendingCache.put(cacheKey)
        metrikkService.registrerMottattSøknad(endringsmelding.ytelse())
    }
}
