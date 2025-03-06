package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse

import jakarta.validation.Valid
import java.util.*

data class UngdomsytelseOppgavebekreftelse(
    val deltakelseId: UUID,
    @field:Valid val oppgave: UngdomsytelseOppgaveDTO,
)
