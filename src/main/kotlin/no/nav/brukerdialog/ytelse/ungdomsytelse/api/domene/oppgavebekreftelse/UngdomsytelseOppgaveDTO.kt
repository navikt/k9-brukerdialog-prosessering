package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse

import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.EndretProgramperiodeDataDTO
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.KontrollerRegisterinntektOppgavetypeDataDTO
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.OppgaveDTO
import org.hibernate.validator.constraints.UUID


data class UngdomsytelseOppgaveDTO(
    @field:UUID(message = "Forventet gyldig UUID, men var '\${validatedValue}'")
    val oppgaveReferanse: String,
    @field:Valid val uttalelse: UngdomsytelseOppgaveUttalelseDTO,
) {

    fun somKomplettOppgave(oppgaveDTO: OppgaveDTO): KomplettUngdomsytelseOppgaveDTO {
        return when (oppgaveDTO.oppgavetypeData) {
            is EndretProgramperiodeDataDTO -> {
                val endretProgramperiodeDataDTO = oppgaveDTO.oppgavetypeData as EndretProgramperiodeDataDTO
                return KomplettEndretPeriodeUngdomsytelseOppgaveDTO(
                    oppgaveReferanse = oppgaveReferanse,
                    nyStartdato = endretProgramperiodeDataDTO.fraOgMed,
                    nySluttdato = endretProgramperiodeDataDTO.tilOgMed,
                    uttalelse = uttalelse
                )
            }

            is KontrollerRegisterinntektOppgavetypeDataDTO -> {
                KomplettKontrollerRegisterInntektOppgaveTypeDataDTO(
                    oppgaveReferanse = oppgaveReferanse,
                    fraOgMed = (oppgaveDTO.oppgavetypeData as KontrollerRegisterinntektOppgavetypeDataDTO).fraOgMed,
                    tilOgMed = (oppgaveDTO.oppgavetypeData as KontrollerRegisterinntektOppgavetypeDataDTO).tilOgMed,
                    registerinntekt = (oppgaveDTO.oppgavetypeData as KontrollerRegisterinntektOppgavetypeDataDTO).registerinntekt,
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
