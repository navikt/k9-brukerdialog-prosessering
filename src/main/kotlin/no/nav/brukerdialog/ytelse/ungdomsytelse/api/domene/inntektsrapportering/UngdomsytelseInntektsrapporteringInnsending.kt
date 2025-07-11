package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.inntektsrapportering

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
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
import no.nav.k9.søknad.ytelse.ung.v1.inntekt.OppgittInntekt
import no.nav.k9.søknad.ytelse.ung.v1.UngSøknadstype
import no.nav.k9.søknad.ytelse.ung.v1.Ungdomsytelse
import no.nav.k9.søknad.ytelse.ung.v1.UngdomsytelseSøknadValidator
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime
import no.nav.k9.søknad.Søknad as UngSøknad

data class UngdomsytelseInntektsrapporteringInnsending(
    @field:org.hibernate.validator.constraints.UUID(message = "Forventet gyldig UUID, men var '\${validatedValue}'")
    val oppgaveReferanse: String,

    @Schema(hidden = true)
    val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),

    @field:Valid val oppgittInntektForPeriode: OppgittInntektForPeriode,

    @field:AssertTrue(message = "Inntektsopplysningene må bekreftes for å kunne rapportere")
    val harBekreftetInntekt: Boolean,

    ) : Innsending {
    companion object {
        private val K9_SØKNAD_VERSJON = Versjon.of("1.0.0")
    }

    override fun somKomplettSøknad(
        søker: Søker,
        k9Format: no.nav.k9.søknad.Innsending?,
        titler: List<String>,
    ): UngdomsytelseKomplettInntektsrapportering {
        requireNotNull(k9Format)
        return UngdomsytelseKomplettInntektsrapportering(
            oppgaveReferanse = oppgaveReferanse,
            mottatt = mottatt,
            søker = søker,
            oppgittInntektForPeriode = oppgittInntektForPeriode,
            harBekreftetInntekt = harBekreftetInntekt,
            k9Format = k9Format as UngSøknad
        )
    }

    override fun valider() = mutableListOf<String>()

    override fun somK9Format(søker: Søker, metadata: MetaInfo): UngSøknad {
        val ytelse = Ungdomsytelse()
            .medSøknadType(UngSøknadstype.RAPPORTERING_SØKNAD)
            .medInntekter(OppgittInntekt(setOf(oppgittInntektForPeriode.somUngOppgittInntektForPeriode())))

        return UngSøknad()
            .medVersjon(K9_SØKNAD_VERSJON)
            .medMottattDato(mottatt)
            .medSpråk(Språk.NORSK_BOKMÅL)
            .medSøknadId(SøknadId(oppgaveReferanse))
            .medSøker(søker.somK9Søker())
            .medYtelse(ytelse)
            .medKildesystem(Kildesystem.SØKNADSDIALOG)
    }

    override fun søkerNorskIdent(): String? = null
    override fun ytelse(): Ytelse = Ytelse.UNGDOMSYTELSE_INNTEKTSRAPPORTERING
    override fun innsendingId(): String = oppgaveReferanse
    override fun vedlegg(): List<URL> = mutableListOf()
    override fun søknadValidator(): SøknadValidator<Søknad> = UngdomsytelseSøknadValidator()
}
