package no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.oppgavebekreftelse

import jakarta.validation.Valid

data class AktivitetspengerOppgavebekreftelse(
    @field:Valid val oppgave: AktivitetspengerOppgaveDTO,
)
