package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse

import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import no.nav.ung.brukerdialog.kontrakt.oppgaver.BrukerdialogOppgaveDto
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.endretperiode.EndretPeriodeDataDto
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.endretsluttdato.EndretSluttdatoDataDto
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.endretstartdato.EndretStartdatoDataDto
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.KontrollerRegisterinntektOppgavetypeDataDto
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.EndretPeriodeDataDTO
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

    fun somKomplettOppgave(oppgaveDTO: BrukerdialogOppgaveDto): KomplettUngdomsytelseOppgaveDTO {
        return when (val oppgavetypeData = oppgaveDTO.oppgavetypeData) {
            is EndretStartdatoDataDto -> {
                return KomplettEndretStartdatoUngdomsytelseOppgaveDTO(
                    oppgaveReferanse = oppgaveReferanse,
                    nyStartdato = oppgavetypeData.nyStartdato,
                    uttalelse = uttalelse
                )
            }

            is EndretSluttdatoDataDto -> {
                return KomplettEndretSluttdatoUngdomsytelseOppgaveDTO(
                    oppgaveReferanse = oppgaveReferanse,
                    nySluttdato = oppgavetypeData.nySluttdato,
                    uttalelse = uttalelse
                )
            }

            is EndretPeriodeDataDto -> {
                return KomplettEndretPeriodeUngdomsytelseOppgaveDTO(
                    oppgaveReferanse = oppgaveReferanse,
                    nyPeriode = oppgavetypeData.nyPeriode,
                    uttalelse = uttalelse
                )
            }

            is KontrollerRegisterinntektOppgavetypeDataDto -> {
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
