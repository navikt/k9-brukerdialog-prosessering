package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.constraints.AssertTrue
import no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser.EndretSluttdatoOppgavetypeDataDTO
import no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser.EndretStartdatoOppgavetypeDataDTO
import no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser.KontrollerRegisterInntektOppgaveTypeDataDTO
import no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser.OppgaveDTO
import java.time.LocalDate

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = EndretStartdatoUngdomsytelseOppgaveDTO::class, name = "BEKREFT_ENDRET_STARTDATO"),
    JsonSubTypes.Type(value = EndretSluttdatoUngdomsytelseOppgaveDTO::class, name = "BEKREFT_ENDRET_SLUTTDATO"),
    JsonSubTypes.Type(value = KontrollerRegisterinntektOppgavetypeDataDTO::class, name = "BEKREFT_AVVIK_REGISTERINNTEKT"),
)
sealed class UngdomsytelseOppgaveDTO(
    @field:org.hibernate.validator.constraints.UUID(message = "Forventet gyldig UUID, men var '\${validatedValue}'")
    open val oppgaveId: String,
    open val bekreftelseSvar: BekreftelseSvar,
    open val ikkeGodkjentResponse: UngdomsytelseIkkeGodkjentResponse? = null,
) {

    abstract fun somKomplettOppgave(oppgaveDTO: OppgaveDTO): KomplettUngdomsytelseOppgaveDTO
}

data class EndretStartdatoUngdomsytelseOppgaveDTO(
    override val oppgaveId: String,
    override val bekreftelseSvar: BekreftelseSvar,
    override val ikkeGodkjentResponse: UngdomsytelseIkkeGodkjentResponse? = null,
) : UngdomsytelseOppgaveDTO(oppgaveId, bekreftelseSvar, ikkeGodkjentResponse) {

    @Hidden
    @AssertTrue(message = "Ikke godkjent respons må være satt hvis bekreftelseSvar er AVSLÅR")
    fun isIkkeGodkjentResponseValid(): Boolean {
        return if (bekreftelseSvar == BekreftelseSvar.AVSLÅR) {
            ikkeGodkjentResponse != null
        } else {
            true
        }
    }

    override fun somKomplettOppgave(oppgaveDTO: OppgaveDTO): KomplettUngdomsytelseOppgaveDTO {
        val endretStartdatoOppgavetypeDataDTO = oppgaveDTO.oppgavetypeData as EndretStartdatoOppgavetypeDataDTO

        return KomplettEndretStartdatoUngdomsytelseOppgaveDTO(
            oppgaveId = oppgaveId,
            veilederRef = oppgaveDTO.oppgavetypeData.veilederRef,
            meldingFraVeileder = oppgaveDTO.oppgavetypeData.meldingFraVeileder,
            nyStartdato = endretStartdatoOppgavetypeDataDTO.nyStartdato,
            bekreftelseSvar = bekreftelseSvar,
            ikkeGodkjentResponse = ikkeGodkjentResponse,
        )
    }
}

data class EndretSluttdatoUngdomsytelseOppgaveDTO(
    override val oppgaveId: String,
    override val bekreftelseSvar: BekreftelseSvar,
    override val ikkeGodkjentResponse: UngdomsytelseIkkeGodkjentResponse? = null
) : UngdomsytelseOppgaveDTO(oppgaveId, bekreftelseSvar, ikkeGodkjentResponse) {

    @Hidden
    @AssertTrue(message = "Ikke godkjent respons må være satt hvis bekreftelseSvar er AVSLÅR")
    fun isIkkeGodkjentResponseValid(): Boolean {
        return if (bekreftelseSvar == BekreftelseSvar.AVSLÅR) {
            ikkeGodkjentResponse != null
        } else {
            true
        }
    }

    override fun somKomplettOppgave(oppgaveDTO: OppgaveDTO): KomplettUngdomsytelseOppgaveDTO {
        val endretSluttdatoOppgavetypeDataDTO = oppgaveDTO.oppgavetypeData as EndretSluttdatoOppgavetypeDataDTO
        return KomplettEndretSluttdatoUngdomsytelseOppgaveDTO(
            oppgaveId = oppgaveId,
            veilederRef = oppgaveDTO.oppgavetypeData.veilederRef,
            meldingFraVeileder = oppgaveDTO.oppgavetypeData.meldingFraVeileder,
            nySluttdato = endretSluttdatoOppgavetypeDataDTO.nySluttdato,
            bekreftelseSvar = bekreftelseSvar,
            ikkeGodkjentResponse = ikkeGodkjentResponse,
        )
    }
}


data class KontrollerRegisterinntektOppgavetypeDataDTO(
    override val oppgaveId: String,
    override val bekreftelseSvar: BekreftelseSvar,
    override val ikkeGodkjentResponse: UngdomsytelseIkkeGodkjentResponse? = null,
) : UngdomsytelseOppgaveDTO(oppgaveId, bekreftelseSvar, ikkeGodkjentResponse) {

    @Hidden
    @AssertTrue(message = "Ikke godkjent respons må være satt hvis bekreftelseSvar er AVSLÅR")
    fun isIkkeGodkjentResponseValid(): Boolean {
        return if (bekreftelseSvar == BekreftelseSvar.AVSLÅR) {
            ikkeGodkjentResponse != null
        } else {
            true
        }
    }

    override fun somKomplettOppgave(oppgaveDTO: OppgaveDTO): KomplettUngdomsytelseOppgaveDTO {
        val kontrollerRegisterInntektOppgaveTypeDataDTO = oppgaveDTO.oppgavetypeData as KontrollerRegisterInntektOppgaveTypeDataDTO

        return KomplettKontrollerRegisterInntektOppgaveTypeDataDTO(
            oppgaveId = oppgaveId,
            veilederRef = oppgaveDTO.oppgavetypeData.veilederRef,
            meldingFraVeileder = oppgaveDTO.oppgavetypeData.meldingFraVeileder,
            fomDato = kontrollerRegisterInntektOppgaveTypeDataDTO.fomDato,
            tomDato = kontrollerRegisterInntektOppgaveTypeDataDTO.tomDato,
            registerinntekt = kontrollerRegisterInntektOppgaveTypeDataDTO.registerinntekt,
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
