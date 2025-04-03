package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse

import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser.EndretSluttdatoOppgavetypeDataDTO
import no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser.EndretStartdatoOppgavetypeDataDTO
import no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser.KontrollerRegisterInntektOppgaveTypeDataDTO
import no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser.OppgaveDTO
import org.hibernate.validator.constraints.UUID


data class UngdomsytelseOppgaveDTO(
    @field:UUID(message = "Forventet gyldig UUID, men var '\${validatedValue}'")
    val oppgaveReferanse: String,
    @field:Valid val uttalelse: UngdomsytelseOppgaveUttalelseDTO,
) {

    fun somKomplettOppgave(oppgaveDTO: OppgaveDTO): KomplettUngdomsytelseOppgaveDTO {
        return when (oppgaveDTO.oppgavetypeData) {
            is EndretStartdatoOppgavetypeDataDTO -> {
                return KomplettEndretStartdatoUngdomsytelseOppgaveDTO(
                    oppgaveReferanse = oppgaveReferanse,
                    nyStartdato = oppgaveDTO.oppgavetypeData.nyStartdato,
                    uttalelse = uttalelse
                )
            }

            is EndretSluttdatoOppgavetypeDataDTO -> {
                KomplettEndretSluttdatoUngdomsytelseOppgaveDTO(
                    oppgaveReferanse = oppgaveReferanse,
                    nySluttdato = oppgaveDTO.oppgavetypeData.nySluttdato,
                    uttalelse = uttalelse
                )
            }

            is KontrollerRegisterInntektOppgaveTypeDataDTO -> {
                KomplettKontrollerRegisterInntektOppgaveTypeDataDTO(
                    oppgaveReferanse = oppgaveReferanse,
                    fraOgMed = oppgaveDTO.oppgavetypeData.fraOgMed,
                    tilOgMed = oppgaveDTO.oppgavetypeData.tilOgMed,
                    registerinntekt = oppgaveDTO.oppgavetypeData.registerinntekt,
                    uttalelse = uttalelse
                )
            }
        }
    }
}

data class UngdomsytelseOppgaveUttalelseDTO(
    val bekreftelseSvar: BekreftelseSvar,
    val meldingFraDeltaker: String? = null,
) {
    @Hidden
    @AssertTrue(message = "'meldingFraDeltaker' må være satt hvis 'bekreftelseSvar' er AVSLÅR")
    fun isGyldigUttalelse(): Boolean {
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
