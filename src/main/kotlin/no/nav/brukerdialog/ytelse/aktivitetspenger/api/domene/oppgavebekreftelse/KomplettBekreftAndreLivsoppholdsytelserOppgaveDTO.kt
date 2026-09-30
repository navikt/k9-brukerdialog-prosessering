package no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.oppgavebekreftelse

import no.nav.k9.oppgave.bekreftelse.Bekreftelse
import no.nav.k9.oppgave.bekreftelse.ung.livsopphold.AndreLivsoppholdsytelserAvklaringBekreftelse
import java.util.*

data class KomplettBekreftAndreLivsoppholdsytelserOppgaveDTO(
    override val oppgaveReferanse: String,
    override val uttalelse: AktivitetspengerOppgaveUttalelseDTO,
) : KomplettAktivitetspengerOppgaveDTO(oppgaveReferanse, uttalelse) {
    override fun somK9Format(): Bekreftelse {

        val uttalelseFraBruker = if (!uttalelse.uttalelseFraDeltaker.isNullOrBlank()) {
            uttalelse.uttalelseFraDeltaker
        } else null

        return AndreLivsoppholdsytelserAvklaringBekreftelse(
            UUID.fromString(oppgaveReferanse),
            uttalelse.harUttalelse,
            uttalelseFraBruker
        )
    }

    override fun dokumentTittelSuffix(): String = "andre ytelser til livsopphold"
}
