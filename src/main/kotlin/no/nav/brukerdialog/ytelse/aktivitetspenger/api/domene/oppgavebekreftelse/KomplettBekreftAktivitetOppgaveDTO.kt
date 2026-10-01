package no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.oppgavebekreftelse

import no.nav.k9.oppgave.bekreftelse.Bekreftelse
import no.nav.k9.oppgave.bekreftelse.ung.aktivitet.AktivitetAvklaringBekreftelse
import java.util.*

data class KomplettBekreftAktivitetOppgaveDTO(
    override val oppgaveReferanse: String,
    override val uttalelse: AktivitetspengerOppgaveUttalelseDTO,
) : KomplettAktivitetspengerOppgaveDTO(oppgaveReferanse, uttalelse) {
    override fun somK9Format(): Bekreftelse {

        val uttalelseFraBruker = if (!uttalelse.uttalelseFraDeltaker.isNullOrBlank()) {
            uttalelse.uttalelseFraDeltaker
        } else null

        return AktivitetAvklaringBekreftelse(
            UUID.fromString(oppgaveReferanse),
            uttalelse.harUttalelse,
            uttalelseFraBruker
        )
    }

    override fun dokumentTittelSuffix(): String = "aktivitet"
}
