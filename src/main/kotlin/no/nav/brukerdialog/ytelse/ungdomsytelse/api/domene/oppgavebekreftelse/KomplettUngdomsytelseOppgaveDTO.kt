package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.k9.oppgave.bekreftelse.Bekreftelse
import no.nav.k9.oppgave.bekreftelse.ung.inntekt.InntektBekreftelse
import no.nav.k9.oppgave.bekreftelse.ung.periodeendring.EndretProgramperiodeBekreftelse
import no.nav.k9.søknad.felles.type.Periode
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.OppgaveDTO
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.RegisterinntektDTO
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = KomplettEndretPeriodeUngdomsytelseOppgaveDTO::class, name = "BEKREFT_ENDRET_PROGRAMPERIODE"),
    JsonSubTypes.Type(value = KomplettKontrollerRegisterInntektOppgaveTypeDataDTO::class, name = "BEKREFT_AVVIK_REGISTERINNTEKT"),
)
sealed class KomplettUngdomsytelseOppgaveDTO(
    open val oppgaveReferanse: String,
    open val uttalelse: UngdomsytelseOppgaveUttalelseDTO
) {
    abstract fun somK9Format(): Bekreftelse

    abstract fun somKomplettOppgave(oppgaveDTO: OppgaveDTO): KomplettUngdomsytelseOppgaveDTO
}

data class KomplettEndretPeriodeUngdomsytelseOppgaveDTO(
    override val oppgaveReferanse: String,
    override val uttalelse: UngdomsytelseOppgaveUttalelseDTO,
    val nyStartdato: LocalDate,
    val nySluttdato: LocalDate? = null,
) : KomplettUngdomsytelseOppgaveDTO(oppgaveReferanse, uttalelse) {
    override fun somK9Format(): Bekreftelse {
        val endretFomDatoBekreftelse =
            EndretProgramperiodeBekreftelse(
                UUID.fromString(oppgaveReferanse),
                Periode(nyStartdato, nySluttdato),
                uttalelse.bekreftelseSvar.somBoolean()
            )

        if (!uttalelse.meldingFraDeltaker.isNullOrBlank()) {
            endretFomDatoBekreftelse.medUttalelseFraBruker(uttalelse.meldingFraDeltaker)
        }

        return endretFomDatoBekreftelse
    }

    override fun somKomplettOppgave(oppgaveDTO: OppgaveDTO): KomplettUngdomsytelseOppgaveDTO {
        return KomplettEndretPeriodeUngdomsytelseOppgaveDTO(
            oppgaveReferanse = oppgaveReferanse,
            nyStartdato = nyStartdato,
            uttalelse = uttalelse
        )
    }
}

data class KomplettKontrollerRegisterInntektOppgaveTypeDataDTO(
    override val oppgaveReferanse: String,
    override val uttalelse: UngdomsytelseOppgaveUttalelseDTO,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val registerinntekt: RegisterinntektDTO,
) : KomplettUngdomsytelseOppgaveDTO(oppgaveReferanse, uttalelse) {

    override fun somK9Format(): Bekreftelse {
        val inntektBekreftelse = InntektBekreftelse.builder()
            .medOppgaveReferanse(UUID.fromString(oppgaveReferanse))
            .medHarBrukerGodtattEndringen(uttalelse.bekreftelseSvar.somBoolean())

        if (!uttalelse.meldingFraDeltaker.isNullOrBlank()) {
            inntektBekreftelse.medUttalelseFraBruker(uttalelse.meldingFraDeltaker)
        }

        return inntektBekreftelse.build()
    }

    override fun somKomplettOppgave(oppgaveDTO: OppgaveDTO): KomplettUngdomsytelseOppgaveDTO {
        return KomplettKontrollerRegisterInntektOppgaveTypeDataDTO(
            oppgaveReferanse = oppgaveReferanse,
            fraOgMed = fraOgMed,
            tilOgMed = tilOgMed,
            registerinntekt = registerinntekt,
            uttalelse = uttalelse
        )
    }
}
