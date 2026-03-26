package no.nav.brukerdialog.ytelse.ungdomsytelse.api

import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.common.formaterStatuslogging
import no.nav.brukerdialog.domenetjenester.innsending.DuplikatInnsendingSjekker
import no.nav.brukerdialog.domenetjenester.innsending.InnsendingService
import no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag.BarnService
import no.nav.brukerdialog.integrasjon.ungbrukerdialogapi.UngBrukerdialogApiService
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
import no.nav.ung.brukerdialog.kontrakt.oppgaver.LøsOppgaveRequest
import no.nav.ung.brukerdialog.kontrakt.oppgaver.SvarPåVarselDto
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.inntektsrapportering.InntektsrapporteringOppgavetypeDataDto
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.inntektsrapportering.RapportertInntektDto
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.søkytelse.SøkYtelseOppgavetypeDataDto
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.stereotype.Service
import org.springframework.web.ErrorResponseException
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@Service
class UngdomsytelseService(
    private val innsendingService: InnsendingService,
    private val duplikatInnsendingSjekker: DuplikatInnsendingSjekker,
    private val springTokenValidationContextHolder: SpringTokenValidationContextHolder,
    private val metrikkService: MetrikkService,
    private val ungDeltakelseOpplyserService: UngDeltakelseOpplyserService,
    private val ungBrukerdialogApiService: UngBrukerdialogApiService,
    private val barnService: BarnService,
) {
    private companion object {
        private val logger = LoggerFactory.getLogger(UngdomsytelseService::class.java)
    }

    suspend fun innsendingUngdomsytelsesøknad(søknad: Ungdomsytelsesøknad, gitSha: String) {
        val startdato = hentStartdato(søknad)

        val barn = barnService.hentBarn().map { Barn(navn = it.navn()) }


        val ungdomsytelsesøknadInnsending = UngdomsytelsesøknadInnsending(
            oppgaveReferanse = søknad.oppgaveReferanse,
            deltakelseId = søknad.deltakelseId,
            språk = søknad.språk,
            mottatt = søknad.mottatt,
            startdato = startdato,
            søkerNorskIdent = søknad.søkerNorskIdent,
            barn = barn,
            barnErRiktig = søknad.barnErRiktig,
            kontonummerInfo = søknad.kontonummerInfo,
            harBekreftetOpplysninger = søknad.harBekreftetOpplysninger,
            harForståttRettigheterOgPlikter = søknad.harForståttRettigheterOgPlikter
        )

        val metadata = MetaInfo(correlationId = MDCUtil.callIdOrNew(), soknadDialogCommitSha = gitSha)
        val cacheKey = "${springTokenValidationContextHolder.personIdent()}_${ungdomsytelsesøknadInnsending.ytelse()}"

        logger.info(
            formaterStatuslogging(
                ungdomsytelsesøknadInnsending.ytelse(),
                ungdomsytelsesøknadInnsending.innsendingId(),
                "mottatt."
            )
        )
        duplikatInnsendingSjekker.forsikreIkkeDuplikatInnsending(cacheKey)

        innsendingService.registrer(ungdomsytelsesøknadInnsending, metadata)
        val deltakelseDTO =
            ungDeltakelseOpplyserService.markerDeltakelseSomSøkt(deltakelseId = UUID.fromString(søknad.deltakelseId))
        if (deltakelseDTO.søktTidspunkt == null) {
            throw ErrorResponseException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ProblemDetail.forStatusAndDetail(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Kunne ikke markere deltakelse som søkt"
                ),
                null
            )
        }

        metrikkService.registrerMottattInnsending(ungdomsytelsesøknadInnsending.ytelse())
        ungBrukerdialogApiService.markerOppgaveSomLøst(
            UUID.fromString(søknad.oppgaveReferanse),
            LøsOppgaveRequest(null)
        )
    }

    private fun hentStartdato(søknad: Ungdomsytelsesøknad): LocalDate {
        val oppgave = ungBrukerdialogApiService.hentOppgave(UUID.fromString(søknad.oppgaveReferanse))
        val oppgavetypeData =
            (oppgave.oppgavetypeData as? SøkYtelseOppgavetypeDataDto
                ?: throw IllegalStateException("OppgavetypeData er ikke av type SøkYtelseOppgavetypeDataDto"))
        return oppgavetypeData.fomDato
    }

    suspend fun inntektrapportering(rapportetInntekt: UngdomsytelseInntektsrapportering, gitSha: String) {
        val inntektsrapporteringPeriode = hentInntektsrapporteringPeriode(rapportetInntekt)

        val inntektsrapporteringInnsending = UngdomsytelseInntektsrapporteringInnsending(
            oppgaveReferanse = rapportetInntekt.oppgaveReferanse,
            mottatt = rapportetInntekt.mottatt,
            oppgittInntektForPeriode = OppgittInntektForPeriode(
                arbeidstakerOgFrilansInntekt = rapportetInntekt.oppgittInntekt.arbeidstakerOgFrilansInntekt,
                periodeForInntekt = inntektsrapporteringPeriode
            )
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
        ungBrukerdialogApiService.markerOppgaveSomLøst(
            UUID.fromString(rapportetInntekt.oppgaveReferanse),
            LøsOppgaveRequest(
                RapportertInntektDto(
                    inntektsrapporteringPeriode.fraOgMed,
                    inntektsrapporteringPeriode.tilOgMed,
                    inntektsrapporteringInnsending.oppgittInntektForPeriode.arbeidstakerOgFrilansInntekt
                        ?.let { BigDecimal.valueOf(it.toLong()) }
                )))
    }

    private fun hentInntektsrapporteringPeriode(rapportetInntekt: UngdomsytelseInntektsrapportering): UngPeriode {
        val oppgave = ungBrukerdialogApiService.hentOppgave(UUID.fromString(rapportetInntekt.oppgaveReferanse))
        val oppgavetypeData =
            (oppgave.oppgavetypeData as? InntektsrapporteringOppgavetypeDataDto
                ?: throw IllegalStateException("OppgavetypeData er ikke av type InntektsrapporteringOppgavetypeDataDto"))
        return UngPeriode(oppgavetypeData.fraOgMed, oppgavetypeData.tilOgMed)
    }

    suspend fun oppgavebekreftelse(oppgavebekreftelse: UngdomsytelseOppgavebekreftelse, gitSha: String) {
        val oppgaveDTO = ungBrukerdialogApiService.hentOppgave(
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
        ungBrukerdialogApiService.markerOppgaveSomLøst(
            oppgaveDTO.oppgaveReferanse, LøsOppgaveRequest(
                SvarPåVarselDto(
                    oppgavebekreftelse.oppgave.uttalelse.harUttalelse,
                    oppgavebekreftelse.oppgave.uttalelse.uttalelseFraDeltaker
                )
            )
        )
    }
}
