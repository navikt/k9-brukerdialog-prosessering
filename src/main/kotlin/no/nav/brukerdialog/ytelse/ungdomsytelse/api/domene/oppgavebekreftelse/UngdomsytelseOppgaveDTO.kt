package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser.EndretSluttdatoOppgavetypeDataDTO
import no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser.EndretStartdatoOppgavetypeDataDTO
import no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser.KontrollerRegisterInntektOppgaveTypeDataDTO
import no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser.OppgaveDTO

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = EndretStartdatoUngdomsytelseOppgaveDTO::class, name = "BEKREFT_ENDRET_STARTDATO"),
    JsonSubTypes.Type(value = EndretSluttdatoUngdomsytelseOppgaveDTO::class, name = "BEKREFT_ENDRET_SLUTTDATO"),
    JsonSubTypes.Type(
        value = KontrollerRegisterinntektOppgavetypeDataDTO::class,
        name = "BEKREFT_AVVIK_REGISTERINNTEKT"
    ),
)
sealed class UngdomsytelseOppgaveDTO(
    @field:org.hibernate.validator.constraints.UUID(message = "Forventet gyldig UUID, men var '\${validatedValue}'")
    open val oppgaveReferanse: String,
    @field:Valid open val uttalelse: UngdomsytelseOppgaveUttalelseDTO,
) {

    abstract fun somKomplettOppgave(oppgaveDTO: OppgaveDTO): KomplettUngdomsytelseOppgaveDTO
}

data class EndretStartdatoUngdomsytelseOppgaveDTO(
    override val oppgaveReferanse: String,
    @field:Valid override val uttalelse: UngdomsytelseOppgaveUttalelseDTO,
) : UngdomsytelseOppgaveDTO(oppgaveReferanse, uttalelse) {

    override fun somKomplettOppgave(oppgaveDTO: OppgaveDTO): KomplettUngdomsytelseOppgaveDTO {
        val endretStartdatoOppgavetypeDataDTO = oppgaveDTO.oppgavetypeData as EndretStartdatoOppgavetypeDataDTO

        return KomplettEndretStartdatoUngdomsytelseOppgaveDTO(
            oppgaveReferanse = oppgaveReferanse,
            nyStartdato = endretStartdatoOppgavetypeDataDTO.nyStartdato,
            uttalelse = uttalelse
        )
    }
}

data class EndretSluttdatoUngdomsytelseOppgaveDTO(
    override val oppgaveReferanse: String,
    @field:Valid override val uttalelse: UngdomsytelseOppgaveUttalelseDTO,
) : UngdomsytelseOppgaveDTO(oppgaveReferanse, uttalelse) {

    override fun somKomplettOppgave(oppgaveDTO: OppgaveDTO): KomplettUngdomsytelseOppgaveDTO {
        val endretSluttdatoOppgavetypeDataDTO = oppgaveDTO.oppgavetypeData as EndretSluttdatoOppgavetypeDataDTO
        return KomplettEndretSluttdatoUngdomsytelseOppgaveDTO(
            oppgaveReferanse = oppgaveReferanse,
            nySluttdato = endretSluttdatoOppgavetypeDataDTO.nySluttdato,
            uttalelse = uttalelse
        )
    }
}

data class KontrollerRegisterinntektOppgavetypeDataDTO(
    override val oppgaveReferanse: String,
    @field:Valid override val uttalelse: UngdomsytelseOppgaveUttalelseDTO,
) : UngdomsytelseOppgaveDTO(oppgaveReferanse, uttalelse) {

    override fun somKomplettOppgave(oppgaveDTO: OppgaveDTO): KomplettUngdomsytelseOppgaveDTO {
        val kontrollerRegisterInntektOppgaveTypeDataDTO =
            oppgaveDTO.oppgavetypeData as KontrollerRegisterInntektOppgaveTypeDataDTO

        return KomplettKontrollerRegisterInntektOppgaveTypeDataDTO(
            oppgaveReferanse = oppgaveReferanse,
            fraOgMed = kontrollerRegisterInntektOppgaveTypeDataDTO.fraOgMed,
            tilOgMed = kontrollerRegisterInntektOppgaveTypeDataDTO.tilOgMed,
            registerinntekt = kontrollerRegisterInntektOppgaveTypeDataDTO.registerinntekt,
            uttalelse = uttalelse
        )
    }
}

data class UngdomsytelseOppgaveUttalelseDTO(
    val bekreftelseSvar: BekreftelseSvar,
    val meldingFraDeltaker: String? = null,
) {
    @Hidden
    @AssertTrue(message = "'meldingFraDeltaker' må være satt hvis 'bekreftelseSvar' er AVSLÅR")
    fun isIkkeGodkjentResponseValid(): Boolean {
        return if (bekreftelseSvar == BekreftelseSvar.AVSLÅR) {
            !meldingFraDeltaker.isNullOrBlank()
        } else {
            true
        }
    }
}

enum class BekreftelseSvar {
    GODTAR,
    AVSLÅR;

    fun somBoolean(): Boolean = when (this) {
        GODTAR -> true
        AVSLÅR -> false
    }
}
