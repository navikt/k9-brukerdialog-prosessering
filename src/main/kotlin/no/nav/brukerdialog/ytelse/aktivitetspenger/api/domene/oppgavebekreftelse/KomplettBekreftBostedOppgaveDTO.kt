package no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.oppgavebekreftelse

import no.nav.k9.oppgave.bekreftelse.Bekreftelse
import no.nav.k9.oppgave.bekreftelse.ung.bosatt.BostedAvklaringBekreftelse
import java.util.*

data class KomplettBekreftBostedOppgaveDTO(
    override val oppgaveReferanse: String,
    override val uttalelse: AktivitetspengerOppgaveUttalelseDTO,
) : KomplettAktivitetspengerOppgaveDTO(oppgaveReferanse, uttalelse) {
    override fun somK9Format(): Bekreftelse {

        val uttalelseFraBruker = if (!uttalelse.uttalelseFraDeltaker.isNullOrBlank()) {
            uttalelse.uttalelseFraDeltaker
        } else null

        return BostedAvklaringBekreftelse(
            UUID.fromString(oppgaveReferanse),
            uttalelse.harUttalelse,
            uttalelseFraBruker
        )
    }

    override fun dokumentTittelSuffix(): String = "bekreftelse av bosted"
}
