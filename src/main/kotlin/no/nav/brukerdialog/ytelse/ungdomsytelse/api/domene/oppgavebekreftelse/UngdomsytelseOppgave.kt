package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import jakarta.validation.constraints.AssertTrue
import java.time.LocalDate

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = EndretStartdatoUngdomsytelseOppgaveDTO::class, name = "BEKREFT_ENDRET_STARTDATO"),
    JsonSubTypes.Type(value = EndretSluttdatoUngdomsytelseOppgaveDTO::class, name = "BEKREFT_ENDRET_SLUTTDATO"),
)
open class UngdomsytelseOppgaveDTO(
    @field:org.hibernate.validator.constraints.UUID(message = "Forventet gyldig UUID, men var '\${validatedValue}'") open val oppgaveId: String,
    open val veilederRef: String,
    open val meldingFraVeileder: String?,
)

data class EndretStartdatoUngdomsytelseOppgaveDTO(
    override val oppgaveId: String,
    override val veilederRef: String,
    override val meldingFraVeileder: String? = null,
    val nyStartdato: LocalDate,
    val bekreftelseSvar: BekreftelseSvar,
    val ikkeGodkjentResponse: UngdomsytelseIkkeGodkjentResponse? = null,
) : UngdomsytelseOppgaveDTO(oppgaveId, veilederRef, meldingFraVeileder) {

    @AssertTrue(message = "Ikke godkjent respons må være satt hvis bekreftelseSvar er AVSLÅR")
    fun isIkkeGodkjentResponseValid(): Boolean {
        return if (bekreftelseSvar == BekreftelseSvar.AVSLÅR) {
            ikkeGodkjentResponse != null
        } else {
            true
        }
    }
}

data class EndretSluttdatoUngdomsytelseOppgaveDTO(
    override val oppgaveId: String,
    override val veilederRef: String,
    override val meldingFraVeileder: String? = null,
    val nySluttdato: LocalDate,
    val bekreftelseSvar: BekreftelseSvar,
    val ikkeGodkjentResponse: UngdomsytelseIkkeGodkjentResponse? = null,
) : UngdomsytelseOppgaveDTO(oppgaveId, veilederRef, meldingFraVeileder) {

    @AssertTrue(message = "Ikke godkjent respons må være satt hvis bekreftelseSvar er AVSLÅR")
    fun isIkkeGodkjentResponseValid(): Boolean {
        return if (bekreftelseSvar == BekreftelseSvar.AVSLÅR) {
            ikkeGodkjentResponse != null
        } else {
            true
        }
    }
}

data class UngdomsytelseIkkeGodkjentResponse(
    val korrigertDato: LocalDate,
    val kontaktVeilederSvar: Boolean,
    val meldingFraDeltaker: String,
)

enum class BekreftelseSvar {
    GODTAR,
    AVSLÅR,
}

enum class Oppgavetype {
    BEKREFT_ENDRET_STARTDATO,
    BEKREFT_ENDRET_SLUTTDATO,
}
