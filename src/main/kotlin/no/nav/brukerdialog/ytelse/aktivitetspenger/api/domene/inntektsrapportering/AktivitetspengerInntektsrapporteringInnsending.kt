package no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.inntektsrapportering

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.domenetjenester.innsending.Innsending
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.brukerdialog.ytelse.Ytelse
import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.SøknadValidator
import no.nav.k9.søknad.felles.Kildesystem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.type.Språk
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.aktivitetspenger.v1.Aktivitetspenger
import no.nav.k9.søknad.ytelse.aktivitetspenger.v1.AktivitetspengerSøknadValidator
import no.nav.k9.søknad.ytelse.ung.v1.inntekt.OppgittInntekt
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime

data class AktivitetspengerInntektsrapporteringInnsending(
    @field:org.hibernate.validator.constraints.UUID(message = "Forventet gyldig UUID, men var '\${validatedValue}'")
    val oppgaveReferanse: String,

    @Schema(hidden = true)
    val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),

    @field:Valid val oppgittInntektForPeriode: OppgittInntektForPeriode,

    ) : Innsending {
    companion object {
        private val K9_SØKNAD_VERSJON = Versjon.of("1.0.0")
    }

    override fun somKomplettSøknad(
        søker: Søker,
        k9Format: no.nav.k9.søknad.Innsending?,
        titler: List<String>,
    ): AktivitetspengerKomplettInntektsrapportering {
        requireNotNull(k9Format)
        return AktivitetspengerKomplettInntektsrapportering(
            oppgaveReferanse = oppgaveReferanse,
            mottatt = mottatt,
            søker = søker,
            oppgittInntektForPeriode = oppgittInntektForPeriode,
            k9Format = k9Format as Søknad
        )
    }

    override fun valider() = mutableListOf<String>()

    override fun somK9Format(søker: Søker, metadata: MetaInfo): Søknad {
        val ytelse = Aktivitetspenger()
            .medSøknadsperiode(oppgittInntektForPeriode.periodeForInntekt.somUngPeriode())
            .medInntekter(OppgittInntekt(setOf(oppgittInntektForPeriode.somUngOppgittInntektForPeriode())))

        return Søknad()
            .medVersjon(K9_SØKNAD_VERSJON)
            .medMottattDato(mottatt)
            .medSpråk(Språk.NORSK_BOKMÅL)
            .medSøknadId(SøknadId(oppgaveReferanse))
            .medSøker(søker.somK9Søker())
            .medYtelse(ytelse)
            .medKildesystem(Kildesystem.SØKNADSDIALOG)
    }

    override fun søkerNorskIdent(): String? = null
    override fun ytelse(): Ytelse = Ytelse.AKTIVITETSPENGER_INNTEKTSRAPPORTERING
    override fun innsendingId(): String = oppgaveReferanse
    override fun vedlegg(): List<URL> = mutableListOf()
    override fun søknadValidator(): SøknadValidator<Søknad> = AktivitetspengerSøknadValidator()
}
