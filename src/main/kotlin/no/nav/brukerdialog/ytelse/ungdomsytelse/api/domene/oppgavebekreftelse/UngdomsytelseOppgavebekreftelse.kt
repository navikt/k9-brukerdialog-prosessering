package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse

import io.swagger.v3.oas.annotations.media.Schema
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
import no.nav.k9.søknad.ytelse.ung.v1.UngSøknadstype
import no.nav.k9.søknad.ytelse.ung.v1.Ungdomsytelse
import no.nav.k9.søknad.ytelse.ung.v1.UngdomsytelseSøknadValidator
import java.net.URL
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as UngSøknad

data class UngdomsytelseOppgavebekreftelse(
    val deltakelseId: UUID,
    val oppgaveId: UUID,

    @Schema(hidden = true)
    val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),

    ) : Innsending {
    companion object {
        private val K9_SØKNAD_VERSJON = Versjon.of("1.0.0")
    }

    override fun somKomplettSøknad(
        søker: Søker,
        k9Format: no.nav.k9.søknad.Innsending?,
        titler: List<String>,
    ): UngdomsytelseKomplettOppgavebekreftelse {
        requireNotNull(k9Format)
        return UngdomsytelseKomplettOppgavebekreftelse(
            deltakelseId = deltakelseId,
            oppgaveId = oppgaveId,
            søknadId = oppgaveId,
            mottatt = mottatt,
            søker = søker,
            k9Format = k9Format as UngSøknad
        )
    }

    override fun valider() = mutableListOf<String>()

    override fun somK9Format(søker: Søker, metadata: MetaInfo): UngSøknad {
        val ytelse = Ungdomsytelse()
            .medSøknadType(UngSøknadstype.DELTAKELSE_SØKNAD)
            .medStartdato(LocalDate.now()) // TODO: Definere egen kontrakt for denne type opplysninger

        return UngSøknad()
            .medVersjon(K9_SØKNAD_VERSJON)
            .medMottattDato(mottatt)
            .medSpråk(Språk.NORSK_BOKMÅL)
            .medSøknadId(SøknadId(oppgaveId.toString()))
            .medSøker(søker.somK9Søker())
            .medYtelse(ytelse)
            .medKildesystem(Kildesystem.SØKNADSDIALOG)
    }

    override fun søkerNorskIdent(): String? = null
    override fun ytelse(): Ytelse = Ytelse.UNGDOMSYTELSE_OPPGAVEBEKREFTELSE
    override fun søknadId(): String = oppgaveId.toString()
    override fun vedlegg(): List<URL> = mutableListOf()
    override fun søknadValidator(): SøknadValidator<Søknad> = UngdomsytelseSøknadValidator()
}
