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
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.KomplettUngdomsytelseOppgaveDTO
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
        metrikkService.registrerMottattSøknad(søknad.ytelse())
    }

    suspend fun inntektrapportering(rapportetInntekt: UngdomsytelseInntektsrapportering, gitSha: String) {
        val metadata = MetaInfo(correlationId = MDCUtil.callIdOrNew(), soknadDialogCommitSha = gitSha)
        val cacheKey = "${springTokenValidationContextHolder.personIdent()}_${rapportetInntekt.ytelse()}"

        logger.info(formaterStatuslogging(rapportetInntekt.ytelse(), rapportetInntekt.søknadId(), "mottatt."))
        duplikatInnsendingSjekker.forsikreIkkeDuplikatInnsending(cacheKey)
        innsendingService.registrer(rapportetInntekt, metadata)
        metrikkService.registrerMottattSøknad(rapportetInntekt.ytelse())
    }

    suspend fun oppgavebekreftelse(oppgavebekreftelse: UngdomsytelseOppgavebekreftelse, gitSha: String) {
        val metadata = MetaInfo(correlationId = MDCUtil.callIdOrNew(), soknadDialogCommitSha = gitSha)
        val cacheKey = "${springTokenValidationContextHolder.personIdent()}_${oppgavebekreftelse.ytelse()}"

        logger.info(formaterStatuslogging(oppgavebekreftelse.ytelse(), oppgavebekreftelse.søknadId(), "mottatt."))
        duplikatInnsendingSjekker.forsikreIkkeDuplikatInnsending(cacheKey)

        val oppgaveDTO = ungDeltakelseOpplyserService.hentOppgaveForDeltakelse(
            oppgavebekreftelse.deltakelseId,
            UUID.fromString(oppgavebekreftelse.oppgave.oppgaveId)
        )

        oppgavebekreftelse.komplettOppgavebekreftelse = oppgavebekreftelse.oppgave.somKomplettOppgave(oppgaveDTO)

        innsendingService.registrer(oppgavebekreftelse, metadata)
        metrikkService.registrerMottattSøknad(oppgavebekreftelse.ytelse())
    }
}
