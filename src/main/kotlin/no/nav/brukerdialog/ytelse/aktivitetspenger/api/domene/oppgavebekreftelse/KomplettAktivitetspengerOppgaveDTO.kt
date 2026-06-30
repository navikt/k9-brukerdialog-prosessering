package no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.oppgavebekreftelse

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.KomplettEndretSluttdatoUngdomsytelseOppgaveDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.KomplettEndretStartdatoUngdomsytelseOppgaveDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.KomplettUngdomsytelseOppgaveDTO
import no.nav.k9.oppgave.bekreftelse.Bekreftelse
import no.nav.k9.oppgave.bekreftelse.ung.inntekt.InntektBekreftelse
import no.nav.ung.brukerdialog.kontrakt.oppgaver.BrukerdialogOppgaveDto
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.RegisterinntektDTO
import java.time.LocalDate
import java.util.UUID

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = KomplettKontrollerRegisterinntektOppgaveDTO::class, name = Bekreftelse.UNG_AVVIK_REGISTERINNTEKT),
    JsonSubTypes.Type(value = KomplettBekreftBostedOppgaveDTO::class, name = Bekreftelse.AVP_BOSTED_AVKLARING)
)
sealed class KomplettAktivitetspengerOppgaveDTO(
    open val oppgaveReferanse: String,
    open val uttalelse: AktivitetspengerOppgaveUttalelseDTO,
) {
    abstract fun somK9Format(): Bekreftelse
    abstract fun dokumentTittelSuffix(): String
}