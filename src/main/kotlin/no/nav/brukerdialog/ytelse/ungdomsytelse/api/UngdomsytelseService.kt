package no.nav.brukerdialog.ytelse.ungdomsytelse.api

import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.common.formaterStatuslogging
import no.nav.brukerdialog.domenetjenester.innsending.DuplikatInnsendingSjekker
import no.nav.brukerdialog.domenetjenester.innsending.InnsendingService
import no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser.UngDeltakelseOpplyserService
import no.nav.brukerdialog.metrikk.MetrikkService
import no.nav.brukerdialog.utils.MDCUtil
import no.nav.brukerdialog.utils.TokenUtils.personIdent
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.inntektsrapportering.UngdomsytelseInntektsrapportering
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.UngdomsytelseOppgavebekreftelseInnsending
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.UngdomsytelseOppgavebekreftelse
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.soknad.Ungdomsytelsesøknad
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class UngdomsytelseService(
    private val innsendingService: InnsendingService,
    private val duplikatInnsendingSjekker: DuplikatInnsendingSjekker,
    private val springTokenValidationContextHolder: SpringTokenValidationContextHolder,
    private val metrikkService: MetrikkService,
    private val ungDeltakelseOpplyserService: UngDeltakelseOpplyserService
) {
    private companion object {
        private val logger = LoggerFactory.getLogger(UngdomsytelseService::class.java)
    }

    suspend fun innsendingUngdomsytelsesøknad(søknad: Ungdomsytelsesøknad, gitSha: String) {
        val metadata = MetaInfo(correlationId = MDCUtil.callIdOrNew(), soknadDialogCommitSha = gitSha)
        val cacheKey = "${springTokenValidationContextHolder.personIdent()}_${søknad.ytelse()}"

        logger.info(formaterStatuslogging(søknad.ytelse(), søknad.søknadId(), "mottatt."))
        duplikatInnsendingSjekker.forsikreIkkeDuplikatInnsending(cacheKey)
        innsendingService.registrer(søknad, metadata)
        metrikkService.registrerMottattInnsending(søknad.ytelse())
    }

    suspend fun inntektrapportering(rapportetInntekt: UngdomsytelseInntektsrapportering, gitSha: String) {
        val metadata = MetaInfo(correlationId = MDCUtil.callIdOrNew(), soknadDialogCommitSha = gitSha)
        val cacheKey = "${springTokenValidationContextHolder.personIdent()}_${rapportetInntekt.ytelse()}"

        logger.info(formaterStatuslogging(rapportetInntekt.ytelse(), rapportetInntekt.søknadId(), "mottatt."))
        duplikatInnsendingSjekker.forsikreIkkeDuplikatInnsending(cacheKey)
        innsendingService.registrer(rapportetInntekt, metadata)
        metrikkService.registrerMottattInnsending(rapportetInntekt.ytelse())
    }

    suspend fun oppgavebekreftelse(oppgavebekreftelse: UngdomsytelseOppgavebekreftelse, gitSha: String) {
        val oppgaveDTO = ungDeltakelseOpplyserService.hentOppgaveForDeltakelse(
            UUID.fromString(oppgavebekreftelse.oppgave.oppgaveReferanse)
        )

        val ungdomsytelseOppgavebekreftelseInnsending = UngdomsytelseOppgavebekreftelseInnsending(
            komplettOppgavebekreftelse = oppgavebekreftelse.oppgave.somKomplettOppgave(oppgaveDTO)
        )

        val metadata = MetaInfo(correlationId = MDCUtil.callIdOrNew(), soknadDialogCommitSha = gitSha)
        val cacheKey = "${springTokenValidationContextHolder.personIdent()}_${ungdomsytelseOppgavebekreftelseInnsending.ytelse()}"

        logger.info(formaterStatuslogging(ungdomsytelseOppgavebekreftelseInnsending.ytelse(), ungdomsytelseOppgavebekreftelseInnsending.søknadId(), "mottatt."))
        duplikatInnsendingSjekker.forsikreIkkeDuplikatInnsending(cacheKey)

        innsendingService.registrer(ungdomsytelseOppgavebekreftelseInnsending, metadata)
        metrikkService.registrerMottattInnsending(ungdomsytelseOppgavebekreftelseInnsending.ytelse())
    }
}
