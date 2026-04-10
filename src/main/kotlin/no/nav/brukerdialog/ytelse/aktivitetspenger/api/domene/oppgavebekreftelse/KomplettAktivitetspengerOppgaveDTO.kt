package no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.oppgavebekreftelse

import no.nav.k9.oppgave.bekreftelse.Bekreftelse
import no.nav.k9.oppgave.bekreftelse.ung.inntekt.InntektBekreftelse
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.RegisterinntektDTO
import java.time.LocalDate
import java.util.UUID

data class KomplettAktivitetspengerOppgaveDTO(
    val oppgaveReferanse: String,
    val uttalelse: AktivitetspengerOppgaveUttalelseDTO,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val registerinntekt: RegisterinntektDTO,
) {
    fun somK9Format(): Bekreftelse {
        val inntektBekreftelse = InntektBekreftelse.builder()
            .medOppgaveReferanse(UUID.fromString(oppgaveReferanse))
            .medHarUttalelse(uttalelse.harUttalelse)

        if (!uttalelse.uttalelseFraDeltaker.isNullOrBlank()) {
            inntektBekreftelse.medUttalelseFraBruker(uttalelse.uttalelseFraDeltaker)
        }

        return inntektBekreftelse.build()
    }

    fun dokumentTittelSuffix(): String = "kontroll av lønn"
}
