package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse

import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.EndretSluttdatoDataDTO
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.EndretStartdatoDataDTO
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.KontrollerRegisterinntektOppgavetypeDataDTO
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.OppgaveDTO
import org.hibernate.validator.constraints.UUID


data class UngdomsytelseOppgaveDTO(
    @field:UUID(message = "Forventet gyldig UUID, men var '\${validatedValue}'")
    val oppgaveReferanse: String,
    @field:Valid val uttalelse: UngdomsytelseOppgaveUttalelseDTO,
) {

    fun somKomplettOppgave(oppgaveDTO: OppgaveDTO): KomplettUngdomsytelseOppgaveDTO {
        return when (val oppgavetypeData = oppgaveDTO.oppgavetypeData) {
            is EndretStartdatoDataDTO -> {
                return KomplettEndretStartdatoUngdomsytelseOppgaveDTO(
                    oppgaveReferanse = oppgaveReferanse,
                    nyStartdato = oppgavetypeData.nyStartdato,
                    uttalelse = uttalelse
                )
            }

            is EndretSluttdatoDataDTO -> {
                return KomplettEndretSluttdatoUngdomsytelseOppgaveDTO(
                    oppgaveReferanse = oppgaveReferanse,
                    nySluttdato = oppgavetypeData.nySluttdato,
                    uttalelse = uttalelse
                )
            }

            is KontrollerRegisterinntektOppgavetypeDataDTO -> {
                KomplettKontrollerRegisterInntektOppgaveTypeDataDTO(
                    oppgaveReferanse = oppgaveReferanse,
                    fraOgMed = oppgavetypeData.fraOgMed,
                    tilOgMed = oppgavetypeData.tilOgMed,
                    registerinntekt = oppgavetypeData.registerinntekt,
                    uttalelse = uttalelse
                )
            }

            else -> {
                throw IllegalArgumentException("Ugyldig oppgavetypeData: ${oppgaveDTO.oppgavetypeData}")
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
