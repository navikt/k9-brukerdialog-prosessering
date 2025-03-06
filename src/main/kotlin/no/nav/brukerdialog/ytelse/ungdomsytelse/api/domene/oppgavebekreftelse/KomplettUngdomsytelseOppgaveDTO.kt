package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.LocalDate

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = KomplettEndretStartdatoUngdomsytelseOppgaveDTO::class, name = "KomplettEndretStartdatoUngdomsytelseOppgaveDTO"),
    JsonSubTypes.Type(value = KomplettEndretSluttdatoUngdomsytelseOppgaveDTO::class, name = "KomplettEndretSluttdatoUngdomsytelseOppgaveDTO"),
)
open class KomplettUngdomsytelseOppgaveDTO(
    open val oppgaveId: String,
    open val veilederRef: String,
    open val meldingFraVeileder: String? = null,
)

data class KomplettEndretStartdatoUngdomsytelseOppgaveDTO(
    override val oppgaveId: String,
    override val veilederRef: String,
    override val meldingFraVeileder: String? = null,
    val nyStartdato: LocalDate,
    val bekreftelseSvar: BekreftelseSvar,
    val ikkeGodkjentResponse: UngdomsytelseIkkeGodkjentResponse? = null,
) : KomplettUngdomsytelseOppgaveDTO(oppgaveId, veilederRef, meldingFraVeileder)

data class KomplettEndretSluttdatoUngdomsytelseOppgaveDTO(
    override val oppgaveId: String,
    override val veilederRef: String,
    override val meldingFraVeileder: String? = null,
    val nySluttdato: LocalDate,
    val bekreftelseSvar: BekreftelseSvar,
    val ikkeGodkjentResponse: UngdomsytelseIkkeGodkjentResponse? = null,
) : KomplettUngdomsytelseOppgaveDTO(oppgaveId, veilederRef, meldingFraVeileder)
