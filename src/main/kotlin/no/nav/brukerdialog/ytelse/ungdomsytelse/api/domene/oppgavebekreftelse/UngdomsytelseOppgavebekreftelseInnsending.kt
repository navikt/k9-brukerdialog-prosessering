package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse

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

data class UngdomsytelseOppgavebekreftelseInnsending(
    val komplettOppgavebekreftelse: KomplettUngdomsytelseOppgaveDTO,
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
            oppgave = komplettOppgavebekreftelse,
            søknadId = komplettOppgavebekreftelse.oppgaveReferanse,
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
            .medSøknadId(SøknadId(komplettOppgavebekreftelse.oppgaveReferanse))
            .medSøker(søker.somK9Søker())
            .medBekreftelse(komplettOppgavebekreftelse.somK9Format())
            .medKildesystem(Kildesystem.SØKNADSDIALOG)
    }

    override fun søkerNorskIdent(): String? = null
    override fun ytelse(): Ytelse = Ytelse.UNGDOMSYTELSE_OPPGAVEBEKREFTELSE
    override fun innsendingId(): String = komplettOppgavebekreftelse.oppgaveReferanse
    override fun vedlegg(): List<URL> = mutableListOf()
    override fun søknadValidator(): SøknadValidator<Søknad> = UngdomsytelseSøknadValidator()
}
