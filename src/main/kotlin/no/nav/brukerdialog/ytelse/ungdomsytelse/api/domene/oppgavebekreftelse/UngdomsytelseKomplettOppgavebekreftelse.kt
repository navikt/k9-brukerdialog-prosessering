package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse

import no.nav.brukerdialog.domenetjenester.innsending.KomplettInnsending
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.k9.oppgave.OppgaveBekreftelse
import java.time.ZonedDateTime

data class UngdomsytelseKomplettOppgavebekreftelse(
    val søknadId: String,
    val oppgave: KomplettUngdomsytelseOppgaveDTO,
    val søker: Søker,
    val mottatt: ZonedDateTime,
    val k9Format: OppgaveBekreftelse,
) : KomplettInnsending {
    override fun innsendingId(): String = søknadId
}
