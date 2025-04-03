package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse

import no.nav.brukerdialog.domenetjenester.innsending.KomplettInnsending
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.k9.oppgave.OppgaveBekreftelse
import java.time.ZonedDateTime

data class UngdomsytelseKomplettOppgavebekreftelse(
    private val søknadId: String,
    private val oppgave: KomplettUngdomsytelseOppgaveDTO,
    private val søker: Søker,
    private val mottatt: ZonedDateTime,
    private val k9Format: OppgaveBekreftelse,
) : KomplettInnsending
