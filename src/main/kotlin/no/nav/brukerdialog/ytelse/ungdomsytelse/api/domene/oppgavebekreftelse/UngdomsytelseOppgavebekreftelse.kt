package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.domenetjenester.innsending.Innsending
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.brukerdialog.ytelse.Ytelse
import no.nav.k9.oppgave.OppgaveBekreftelse
import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.SøknadValidator
import no.nav.k9.søknad.felles.Kildesystem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.type.Språk
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.ung.v1.UngdomsytelseSøknadValidator
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

data class UngdomsytelseOppgavebekreftelse(
    val deltakelseId: UUID,

    @field:Valid val oppgave: UngdomsytelseOppgaveDTO,

    @Schema(hidden = true) // Settes i UngdomsytelseService.oppgavebekreftelse
    var komplettOppgavebekreftelse: KomplettUngdomsytelseOppgaveDTO? = null,

    @Schema(hidden = true)
    val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),

    ) : Innsending {
    companion object {
        private val UNG_OPPGAVE_VERSJON = Versjon.of("1.0.0")
    }

    override fun somKomplettSøknad(
        søker: Søker,
        k9Format: no.nav.k9.søknad.Innsending?,
        titler: List<String>,
    ): UngdomsytelseKomplettOppgavebekreftelse {
        requireNotNull(k9Format)
        return UngdomsytelseKomplettOppgavebekreftelse(
            deltakelseId = deltakelseId,
            oppgave = komplettOppgavebekreftelse
                ?: throw IllegalStateException("komplettOppgavebekreftelse må være satt før registrering"),
            søknadId = komplettOppgavebekreftelse!!.oppgaveId,
            mottatt = mottatt,
            søker = søker,
            k9Format = k9Format as OppgaveBekreftelse
        )
    }

    override fun valider() = mutableListOf<String>()

    override fun somK9Format(søker: Søker, metadata: MetaInfo): OppgaveBekreftelse {
        return OppgaveBekreftelse()
            .medVersjon(UNG_OPPGAVE_VERSJON)
            .medMottattDato(mottatt)
            .medSpråk(Språk.NORSK_BOKMÅL)
            .medSøknadId(SøknadId(oppgave.oppgaveId))
            .medSøker(søker.somK9Søker())
            .medBekreftelse(oppgave.somK9Format())
            .medKildesystem(Kildesystem.SØKNADSDIALOG)
    }

    override fun søkerNorskIdent(): String? = null
    override fun ytelse(): Ytelse = Ytelse.UNGDOMSYTELSE_OPPGAVEBEKREFTELSE
    override fun søknadId(): String = oppgave.oppgaveId
    override fun vedlegg(): List<URL> = mutableListOf()
    override fun søknadValidator(): SøknadValidator<Søknad> = UngdomsytelseSøknadValidator()
}
