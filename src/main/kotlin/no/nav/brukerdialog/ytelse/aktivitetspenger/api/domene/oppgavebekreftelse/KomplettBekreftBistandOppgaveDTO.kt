package no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.oppgavebekreftelse

import no.nav.k9.oppgave.bekreftelse.Bekreftelse
import no.nav.k9.oppgave.bekreftelse.ung.bistand.BistandsbehovAvklaringBekreftelse
import java.util.*

data class KomplettBekreftBistandOppgaveDTO(
    override val oppgaveReferanse: String,
    override val uttalelse: AktivitetspengerOppgaveUttalelseDTO,
) : KomplettAktivitetspengerOppgaveDTO(oppgaveReferanse, uttalelse) {
    override fun somK9Format(): Bekreftelse {

        val uttalelseFraBruker = if (!uttalelse.uttalelseFraDeltaker.isNullOrBlank()) {
            uttalelse.uttalelseFraDeltaker
        } else null

        return BistandsbehovAvklaringBekreftelse(
            UUID.fromString(oppgaveReferanse),
            uttalelse.harUttalelse,
            uttalelseFraBruker
        )
    }

    override fun dokumentTittelSuffix(): String = "behov for bistand"
}
