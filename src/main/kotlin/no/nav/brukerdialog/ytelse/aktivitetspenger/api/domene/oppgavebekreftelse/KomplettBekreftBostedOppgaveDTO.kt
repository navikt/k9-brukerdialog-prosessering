package no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.oppgavebekreftelse

import no.nav.k9.oppgave.bekreftelse.Bekreftelse
import no.nav.k9.oppgave.bekreftelse.ung.inntekt.InntektBekreftelse
import java.util.*

data class KomplettBekreftBostedOppgaveDTO(
    override val oppgaveReferanse: String,
    override val uttalelse: AktivitetspengerOppgaveUttalelseDTO,
) : KomplettAktivitetspengerOppgaveDTO(oppgaveReferanse, uttalelse) {
    override fun somK9Format(): Bekreftelse {
        val bostedsbekreftelse = InntektBekreftelse.builder()
            .medOppgaveReferanse(UUID.fromString(oppgaveReferanse))
            .medHarUttalelse(uttalelse.harUttalelse)

        if (!uttalelse.uttalelseFraDeltaker.isNullOrBlank()) {
            bostedsbekreftelse.medUttalelseFraBruker(uttalelse.uttalelseFraDeltaker)
        }

        return bostedsbekreftelse.build()
    }

    override fun dokumentTittelSuffix(): String = "bekreftelse av bosted"
}
