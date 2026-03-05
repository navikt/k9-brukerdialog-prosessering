package no.nav.brukerdialog.ytelse.aktivitetspenger.api

import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.common.formaterStatuslogging
import no.nav.brukerdialog.domenetjenester.innsending.DuplikatInnsendingSjekker
import no.nav.brukerdialog.domenetjenester.innsending.InnsendingService
import no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag.BarnService
import no.nav.brukerdialog.metrikk.MetrikkService
import no.nav.brukerdialog.utils.MDCUtil
import no.nav.brukerdialog.utils.TokenUtils.personIdent
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.soknad.Aktivitetspengersøknad
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.soknad.AktivitetspengersøknadInnsending
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.soknad.Barn
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AktivitetspengerService(
    private val innsendingService: InnsendingService,
    private val duplikatInnsendingSjekker: DuplikatInnsendingSjekker,
    private val springTokenValidationContextHolder: SpringTokenValidationContextHolder,
    private val metrikkService: MetrikkService,
    private val barnService: BarnService,
) {
    private companion object {
        private val logger = LoggerFactory.getLogger(AktivitetspengerService::class.java)
    }

    suspend fun innsendingAktivitetspengersøknad(søknad: Aktivitetspengersøknad, gitSha: String) {
        val barn = barnService.hentBarn().map { Barn(navn = it.navn()) }

        val aktivitetspengersøknadInnsending = AktivitetspengersøknadInnsending(
            søknadId = søknad.søknadId,
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
        val cacheKey = "${springTokenValidationContextHolder.personIdent()}_${aktivitetspengersøknadInnsending.ytelse()}"

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

}
