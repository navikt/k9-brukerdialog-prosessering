package no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.endringsmelding

import jakarta.validation.Valid
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarn
import no.nav.k9brukerdialogprosessering.api.innsending.InnsendingCache
import no.nav.k9brukerdialogprosessering.api.innsending.InnsendingService
import no.nav.k9brukerdialogprosessering.api.ytelse.MetrikkService
import no.nav.k9brukerdialogprosessering.api.ytelse.Ytelse
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.endringsmelding.domene.Endringsmelding
import no.nav.k9brukerdialogprosessering.common.MetaInfo
import no.nav.k9brukerdialogprosessering.common.formaterStatuslogging
import no.nav.k9brukerdialogprosessering.config.Issuers
import no.nav.k9brukerdialogprosessering.innsyn.InnsynService
import no.nav.k9brukerdialogprosessering.utils.MDCUtil
import no.nav.k9brukerdialogprosessering.utils.NavHeaders
import no.nav.k9brukerdialogprosessering.utils.TokenUtils.personIdent
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
@RequestMapping("/pleiepenger-sykt-barn/endringsmelding")
@RequiredIssuers(
    ProtectedWithClaims(issuer = Issuers.TOKEN_X, claimMap = ["acr=Level4"])
)
@Validated
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
    suspend fun innsending(
        @RequestHeader(NavHeaders.BRUKERDIALOG_YTELSE) ytelse: Ytelse,
        @RequestHeader(NavHeaders.BRUKERDIALOG_GIT_SHA) gitSha: String,
        @RequestBody @Valid endringsmelding: Endringsmelding,
    ) {
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
