package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import jakarta.validation.constraints.AssertTrue
import no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser.OppgaveDTO
import no.nav.k9.oppgave.bekreftelse.Bekreftelse
import no.nav.k9.oppgave.bekreftelse.ung.periodeendring.EndretFomDatoBekreftelse
import no.nav.k9.oppgave.bekreftelse.ung.periodeendring.EndretTomDatoBekreftelse
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
sealed class UngdomsytelseOppgaveDTO(
    @field:org.hibernate.validator.constraints.UUID(message = "Forventet gyldig UUID, men var '\${validatedValue}'")
    open val oppgaveId: String,
    open val bekreftelseSvar: BekreftelseSvar,
    open val ikkeGodkjentResponse: UngdomsytelseIkkeGodkjentResponse? = null,
) {
    abstract fun somK9Format(): Bekreftelse

    abstract fun somKomplettOppgave(oppgaveDTO: OppgaveDTO): KomplettUngdomsytelseOppgaveDTO
}

data class EndretStartdatoUngdomsytelseOppgaveDTO(
    override val oppgaveId: String,
    override val bekreftelseSvar: BekreftelseSvar,
    override val ikkeGodkjentResponse: UngdomsytelseIkkeGodkjentResponse? = null,
    val nyStartdato: LocalDate,
) : UngdomsytelseOppgaveDTO(oppgaveId, bekreftelseSvar, ikkeGodkjentResponse) {

    @AssertTrue(message = "Ikke godkjent respons må være satt hvis bekreftelseSvar er AVSLÅR")
    fun isIkkeGodkjentResponseValid(): Boolean {
        return if (bekreftelseSvar == BekreftelseSvar.AVSLÅR) {
            ikkeGodkjentResponse != null
        } else {
            true
        }
    }

    override fun somK9Format(): Bekreftelse = EndretFomDatoBekreftelse(nyStartdato, bekreftelseSvar.somBoolean())

    override fun somKomplettOppgave(oppgaveDTO: OppgaveDTO): KomplettUngdomsytelseOppgaveDTO {
        return KomplettEndretStartdatoUngdomsytelseOppgaveDTO(
            oppgaveId = oppgaveId,
            veilederRef = oppgaveDTO.oppgavetypeData.veilederRef,
            meldingFraVeileder = oppgaveDTO.oppgavetypeData.meldingFraVeileder,
            nyStartdato = nyStartdato,
            bekreftelseSvar = bekreftelseSvar,
            ikkeGodkjentResponse = ikkeGodkjentResponse,
        )
    }
}

data class EndretSluttdatoUngdomsytelseOppgaveDTO(
    override val oppgaveId: String,
    override val bekreftelseSvar: BekreftelseSvar,
    override val ikkeGodkjentResponse: UngdomsytelseIkkeGodkjentResponse? = null,
    val nySluttdato: LocalDate,
) : UngdomsytelseOppgaveDTO(oppgaveId, bekreftelseSvar, ikkeGodkjentResponse) {

    @AssertTrue(message = "Ikke godkjent respons må være satt hvis bekreftelseSvar er AVSLÅR")
    fun isIkkeGodkjentResponseValid(): Boolean {
        return if (bekreftelseSvar == BekreftelseSvar.AVSLÅR) {
            ikkeGodkjentResponse != null
        } else {
            true
        }
    }

    override fun somK9Format(): Bekreftelse = EndretTomDatoBekreftelse(nySluttdato, bekreftelseSvar.somBoolean())
    override fun somKomplettOppgave(oppgaveDTO: OppgaveDTO): KomplettUngdomsytelseOppgaveDTO {
        return KomplettEndretSluttdatoUngdomsytelseOppgaveDTO(
            oppgaveId = oppgaveId,
            veilederRef = oppgaveDTO.oppgavetypeData.veilederRef,
            meldingFraVeileder = oppgaveDTO.oppgavetypeData.meldingFraVeileder,
            nySluttdato = nySluttdato,
            bekreftelseSvar = bekreftelseSvar,
            ikkeGodkjentResponse = ikkeGodkjentResponse,
        )
    }
}

data class UngdomsytelseIkkeGodkjentResponse(
    val korrigertDato: LocalDate,
    val kontaktVeilederSvar: Boolean,
    val meldingFraDeltaker: String,
)

enum class BekreftelseSvar {
    GODTAR,
    AVSLÅR;

    fun somBoolean(): Boolean = when (this) {
        GODTAR -> true
        AVSLÅR -> false
    }
}

enum class Oppgavetype {
    BEKREFT_ENDRET_STARTDATO,
    BEKREFT_ENDRET_SLUTTDATO,
}
