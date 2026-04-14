package no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.oppgavebekreftelse

import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import no.nav.ung.brukerdialog.kontrakt.oppgaver.BrukerdialogOppgaveDto
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.KontrollerRegisterinntektOppgavetypeDataDto
import org.hibernate.validator.constraints.UUID

data class AktivitetspengerOppgaveDTO(
    @field:UUID(message = "Forventet gyldig UUID, men var '\${validatedValue}'")
    val oppgaveReferanse: String,
    @field:Valid val uttalelse: AktivitetspengerOppgaveUttalelseDTO,
) {
    fun somKomplettOppgave(oppgaveDTO: BrukerdialogOppgaveDto): KomplettAktivitetspengerOppgaveDTO {
        return when (val oppgavetypeData = oppgaveDTO.oppgavetypeData) {
            is KontrollerRegisterinntektOppgavetypeDataDto -> KomplettAktivitetspengerOppgaveDTO(
                oppgaveReferanse = oppgaveReferanse,
                fraOgMed = oppgavetypeData.fraOgMed,
                tilOgMed = oppgavetypeData.tilOgMed,
                registerinntekt = oppgavetypeData.registerinntekt,
                uttalelse = uttalelse,
            )
            else -> throw IllegalArgumentException("Ugyldig oppgavetypeData for aktivitetspenger: ${oppgaveDTO.oppgavetypeData}")
        }
    }
}

data class AktivitetspengerOppgaveUttalelseDTO(
    val harUttalelse: Boolean,
    val uttalelseFraDeltaker: String? = null,
) {
    @Hidden
    @AssertTrue(message = "'uttalelseFraDeltaker' må være satt hvis 'harUttalelse' er true")
    fun isGyldigUttalelse(): Boolean {
        return if (harUttalelse) {
            !uttalelseFraDeltaker.isNullOrBlank()
        } else {
            true
        }
    }
}
