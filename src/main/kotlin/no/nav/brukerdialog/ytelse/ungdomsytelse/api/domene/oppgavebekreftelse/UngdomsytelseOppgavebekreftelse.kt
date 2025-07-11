package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse

import jakarta.validation.Valid

data class UngdomsytelseOppgavebekreftelse(
    @field:Valid val oppgave: UngdomsytelseOppgaveDTO,
)
