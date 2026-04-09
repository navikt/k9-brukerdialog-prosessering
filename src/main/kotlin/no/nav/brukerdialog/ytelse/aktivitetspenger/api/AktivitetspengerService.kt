package no.nav.brukerdialog.ytelse.aktivitetspenger.api

import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.common.formaterStatuslogging
import no.nav.brukerdialog.domenetjenester.innsending.DuplikatInnsendingSjekker
import no.nav.brukerdialog.domenetjenester.innsending.InnsendingService
import no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag.BarnService
import no.nav.brukerdialog.integrasjon.ungbrukerdialogapi.UngBrukerdialogApiService
import no.nav.brukerdialog.metrikk.MetrikkService
import no.nav.brukerdialog.utils.MDCUtil
import no.nav.brukerdialog.utils.TokenUtils.personIdent
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.inntektsrapportering.AktivitetspengerInntektsrapportering
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.inntektsrapportering.AktivitetspengerInntektsrapporteringInnsending
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.inntektsrapportering.OppgittInntektForPeriode
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.inntektsrapportering.UngPeriode
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.soknad.Aktivitetspengersøknad
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.soknad.AktivitetspengersøknadInnsending
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.soknad.Barn
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import no.nav.ung.brukerdialog.kontrakt.oppgaver.LøsOppgaveRequest
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.inntektsrapportering.InntektsrapporteringOppgavetypeDataDto
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.inntektsrapportering.RapportertInntektDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

@Service
class AktivitetspengerService(
    private val innsendingService: InnsendingService,
    private val duplikatInnsendingSjekker: DuplikatInnsendingSjekker,
    private val springTokenValidationContextHolder: SpringTokenValidationContextHolder,
    private val metrikkService: MetrikkService,
    private val barnService: BarnService,
    private val ungBrukerdialogApiService: UngBrukerdialogApiService,
) {
    private companion object {
        private val logger = LoggerFactory.getLogger(AktivitetspengerService::class.java)
    }

    suspend fun innsendingAktivitetspengersøknad(søknad: Aktivitetspengersøknad, gitSha: String) {
        val barn = barnService.hentBarn().map { Barn(navn = it.navn()) }

        val aktivitetspengersøknadInnsending = AktivitetspengersøknadInnsending(
            søknadId = søknad.søknadId,
            forutgåendeBosteder = søknad.forutgåendeBosteder,
            språk = søknad.språk,
            mottatt = søknad.mottatt,
            startdato = søknad.startdato,
            søkerNorskIdent = søknad.søkerNorskIdent,
            barn = barn,
            barnErRiktig = søknad.barnErRiktig,
            kontonummerInfo = søknad.kontonummerInfo,
            harBekreftetOpplysninger = søknad.harBekreftetOpplysninger,
            harForståttRettigheterOgPlikter = søknad.harForståttRettigheterOgPlikter
        )

        val metadata = MetaInfo(correlationId = MDCUtil.callIdOrNew(), soknadDialogCommitSha = gitSha)
        val cacheKey =
            "${springTokenValidationContextHolder.personIdent()}_${aktivitetspengersøknadInnsending.ytelse()}"

        logger.info(
            formaterStatuslogging(
                aktivitetspengersøknadInnsending.ytelse(),
                aktivitetspengersøknadInnsending.innsendingId(),
                "mottatt."
            )
        )
        duplikatInnsendingSjekker.forsikreIkkeDuplikatInnsending(cacheKey)

        innsendingService.registrer(aktivitetspengersøknadInnsending, metadata)

        metrikkService.registrerMottattInnsending(aktivitetspengersøknadInnsending.ytelse())
    }


    suspend fun inntektrapportering(rapportetInntekt: AktivitetspengerInntektsrapportering, gitSha: String) {
        val inntektsrapporteringPeriode = hentInntektsrapporteringPeriode(rapportetInntekt)

        val inntektsrapporteringInnsending = AktivitetspengerInntektsrapporteringInnsending(
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

    private fun hentInntektsrapporteringPeriode(rapportetInntekt: AktivitetspengerInntektsrapportering): UngPeriode {
        val oppgave = ungBrukerdialogApiService.hentOppgave(UUID.fromString(rapportetInntekt.oppgaveReferanse))
        val oppgavetypeData =
            (oppgave.oppgavetypeData as? InntektsrapporteringOppgavetypeDataDto
                ?: throw IllegalStateException("OppgavetypeData er ikke av type InntektsrapporteringOppgavetypeDataDto"))
        return UngPeriode(oppgavetypeData.fraOgMed, oppgavetypeData.tilOgMed)
    }


}
