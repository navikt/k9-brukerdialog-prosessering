package no.nav.brukerdialog.ytelse.ungdomsytelse.api

import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.common.formaterStatuslogging
import no.nav.brukerdialog.domenetjenester.innsending.DuplikatInnsendingSjekker
import no.nav.brukerdialog.domenetjenester.innsending.InnsendingService
import no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag.BarnService
import no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser.UngDeltakelseOpplyserService
import no.nav.brukerdialog.metrikk.MetrikkService
import no.nav.brukerdialog.utils.MDCUtil
import no.nav.brukerdialog.utils.TokenUtils.personIdent
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.inntektsrapportering.OppgittInntektForPeriode
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.inntektsrapportering.UngPeriode
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.inntektsrapportering.UngdomsytelseInntektsrapportering
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.inntektsrapportering.UngdomsytelseInntektsrapporteringInnsending
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.UngdomsytelseOppgavebekreftelseInnsending
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.UngdomsytelseOppgavebekreftelse
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.soknad.Barn
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.soknad.Ungdomsytelsesøknad
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.soknad.UngdomsytelsesøknadInnsending
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.InntektsrapporteringOppgavetypeDataDTO
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.SendSøknadOppgavetypeDataDTO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class UngdomsytelseService(
    private val innsendingService: InnsendingService,
    private val duplikatInnsendingSjekker: DuplikatInnsendingSjekker,
    private val springTokenValidationContextHolder: SpringTokenValidationContextHolder,
    private val metrikkService: MetrikkService,
    private val ungDeltakelseOpplyserService: UngDeltakelseOpplyserService,
    private val barnService: BarnService,
) {
    private companion object {
        private val logger = LoggerFactory.getLogger(UngdomsytelseService::class.java)
    }

    suspend fun innsendingUngdomsytelsesøknad(søknad: Ungdomsytelsesøknad, gitSha: String) {
        val sendSøknadOppgave =
            ungDeltakelseOpplyserService.hentOppgaveForDeltakelse(UUID.fromString(søknad.oppgaveReferanse))

        val sendSøknadOppgavetypeDataDTO = sendSøknadOppgave.oppgavetypeData as? SendSøknadOppgavetypeDataDTO
            ?: throw IllegalStateException("OppgavetypeData er ikke av type SendSøknadOppgavetypeDataDTO")

        val barn = barnService.hentBarn().map { Barn(navn = it.navn()) }


        val ungdomsytelsesøknadInnsending = UngdomsytelsesøknadInnsending(
            oppgaveReferanse = søknad.oppgaveReferanse,
            språk = søknad.språk,
            mottatt = søknad.mottatt,
            startdato = sendSøknadOppgavetypeDataDTO.fomDato,
            søkerNorskIdent = søknad.søkerNorskIdent,
            barn = barn,
            barnErRiktig = søknad.barnErRiktig,
            kontonummerFraRegister = søknad.kontonummerFraRegister,
            kontonummerErRiktig = søknad.kontonummerErRiktig,
            harBekreftetOpplysninger = søknad.harBekreftetOpplysninger,
            harForståttRettigheterOgPlikter = søknad.harForståttRettigheterOgPlikter
        )

        val metadata = MetaInfo(correlationId = MDCUtil.callIdOrNew(), soknadDialogCommitSha = gitSha)
        val cacheKey = "${springTokenValidationContextHolder.personIdent()}_${ungdomsytelsesøknadInnsending.ytelse()}"

        logger.info(formaterStatuslogging(ungdomsytelsesøknadInnsending.ytelse(), ungdomsytelsesøknadInnsending.innsendingId(), "mottatt."))
        duplikatInnsendingSjekker.forsikreIkkeDuplikatInnsending(cacheKey)

        innsendingService.registrer(ungdomsytelsesøknadInnsending, metadata)
        metrikkService.registrerMottattInnsending(ungdomsytelsesøknadInnsending.ytelse())
    }

    suspend fun inntektrapportering(rapportetInntekt: UngdomsytelseInntektsrapportering, gitSha: String) {
        val rapporterInntektOppgave = ungDeltakelseOpplyserService.hentOppgaveForDeltakelse(
            UUID.fromString(rapportetInntekt.oppgaveReferanse)
        )

        val inntektsrapporteringOppgaveData =
            (rapporterInntektOppgave.oppgavetypeData as? InntektsrapporteringOppgavetypeDataDTO
                ?: throw IllegalStateException("OppgavetypeData er ikke av type InntektsrapporteringOppgavetypeDataDTO"))

        val inntektsrapporteringInnsending = UngdomsytelseInntektsrapporteringInnsending(
            oppgaveReferanse = rapportetInntekt.oppgaveReferanse,
            mottatt = rapportetInntekt.mottatt,
            oppgittInntektForPeriode = OppgittInntektForPeriode(
                arbeidstakerOgFrilansInntekt = rapportetInntekt.oppgittInntekt.arbeidstakerOgFrilansInntekt,
                periodeForInntekt = UngPeriode(
                    fraOgMed = inntektsrapporteringOppgaveData.base.fraOgMed,
                    tilOgMed = inntektsrapporteringOppgaveData.base.tilOgMed
                )
            ),
            harBekreftetInntekt = rapportetInntekt.harBekreftetInntekt,
        )

        val metadata = MetaInfo(correlationId = MDCUtil.callIdOrNew(), soknadDialogCommitSha = gitSha)
        val cacheKey = "${springTokenValidationContextHolder.personIdent()}_${inntektsrapporteringInnsending.ytelse()}"

        logger.info(
            formaterStatuslogging(
                inntektsrapporteringInnsending.ytelse(),
                inntektsrapporteringInnsending.innsendingId(),
                "mottatt."
            )
        )
        duplikatInnsendingSjekker.forsikreIkkeDuplikatInnsending(cacheKey)
        innsendingService.registrer(inntektsrapporteringInnsending, metadata)
        metrikkService.registrerMottattInnsending(inntektsrapporteringInnsending.ytelse())
    }

    suspend fun oppgavebekreftelse(oppgavebekreftelse: UngdomsytelseOppgavebekreftelse, gitSha: String) {
        val oppgaveDTO = ungDeltakelseOpplyserService.hentOppgaveForDeltakelse(
            UUID.fromString(oppgavebekreftelse.oppgave.oppgaveReferanse)
        )

        val ungdomsytelseOppgavebekreftelseInnsending = UngdomsytelseOppgavebekreftelseInnsending(
            komplettOppgavebekreftelse = oppgavebekreftelse.oppgave.somKomplettOppgave(oppgaveDTO)
        )

        val metadata = MetaInfo(correlationId = MDCUtil.callIdOrNew(), soknadDialogCommitSha = gitSha)
        val cacheKey =
            "${springTokenValidationContextHolder.personIdent()}_${ungdomsytelseOppgavebekreftelseInnsending.ytelse()}"

        logger.info(
            formaterStatuslogging(
                ungdomsytelseOppgavebekreftelseInnsending.ytelse(),
                ungdomsytelseOppgavebekreftelseInnsending.innsendingId(),
                "mottatt."
            )
        )
        duplikatInnsendingSjekker.forsikreIkkeDuplikatInnsending(cacheKey)

        innsendingService.registrer(ungdomsytelseOppgavebekreftelseInnsending, metadata)
        metrikkService.registrerMottattInnsending(ungdomsytelseOppgavebekreftelseInnsending.ytelse())
    }
}
