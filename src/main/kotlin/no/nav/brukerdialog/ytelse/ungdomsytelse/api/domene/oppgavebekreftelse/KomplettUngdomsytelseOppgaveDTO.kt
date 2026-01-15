package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.k9.oppgave.bekreftelse.Bekreftelse
import no.nav.k9.oppgave.bekreftelse.ung.inntekt.InntektBekreftelse
import no.nav.k9.oppgave.bekreftelse.ung.periodeendring.EndretPeriodeBekreftelse
import no.nav.k9.oppgave.bekreftelse.ung.periodeendring.EndretSluttdatoBekreftelse
import no.nav.k9.oppgave.bekreftelse.ung.periodeendring.EndretStartdatoBekreftelse
import no.nav.k9.søknad.felles.type.Periode
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.OppgaveDTO
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.PeriodeDTO
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.RegisterinntektDTO
import java.time.LocalDate
import java.util.*

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = KomplettEndretStartdatoUngdomsytelseOppgaveDTO::class, name = Bekreftelse.UNG_ENDRET_STARTDATO),
    JsonSubTypes.Type(value = KomplettEndretSluttdatoUngdomsytelseOppgaveDTO::class, name = Bekreftelse.UNG_ENDRET_SLUTTDATO),
    JsonSubTypes.Type(value = KomplettEndretPeriodeUngdomsytelseOppgaveDTO::class, name = Bekreftelse.UNG_ENDRET_PERIODE),
    JsonSubTypes.Type(value = KomplettKontrollerRegisterInntektOppgaveTypeDataDTO::class, name = Bekreftelse.UNG_AVVIK_REGISTERINNTEKT),
)
sealed class KomplettUngdomsytelseOppgaveDTO(
    open val oppgaveReferanse: String,
    open val uttalelse: UngdomsytelseOppgaveUttalelseDTO,
) {
    abstract fun somK9Format(): Bekreftelse
    abstract fun dokumentTittelSuffix(): String
    abstract fun somKomplettOppgave(oppgaveDTO: OppgaveDTO): KomplettUngdomsytelseOppgaveDTO
}

data class KomplettEndretStartdatoUngdomsytelseOppgaveDTO(
    override val oppgaveReferanse: String,
    override val uttalelse: UngdomsytelseOppgaveUttalelseDTO,
    val nyStartdato: LocalDate,
) : KomplettUngdomsytelseOppgaveDTO(oppgaveReferanse, uttalelse) {
    override fun somK9Format(): Bekreftelse {
        val endretFomDatoBekreftelse =
            EndretStartdatoBekreftelse(
                UUID.fromString(oppgaveReferanse),
                nyStartdato,
                uttalelse.harUttalelse
            )

        if (!uttalelse.uttalelseFraDeltaker.isNullOrBlank()) {
            endretFomDatoBekreftelse.medUttalelseFraBruker(uttalelse.uttalelseFraDeltaker)
        }

        return endretFomDatoBekreftelse
    }

    override fun dokumentTittelSuffix(): String = "endret startdato"

    override fun somKomplettOppgave(oppgaveDTO: OppgaveDTO): KomplettUngdomsytelseOppgaveDTO {
        return KomplettEndretStartdatoUngdomsytelseOppgaveDTO(
            oppgaveReferanse = oppgaveReferanse,
            nyStartdato = nyStartdato,
            uttalelse = uttalelse
        )
    }
}

data class KomplettEndretSluttdatoUngdomsytelseOppgaveDTO(
    override val oppgaveReferanse: String,
    override val uttalelse: UngdomsytelseOppgaveUttalelseDTO,
    val nySluttdato: LocalDate,
) : KomplettUngdomsytelseOppgaveDTO(oppgaveReferanse, uttalelse) {
    override fun somK9Format(): Bekreftelse {
        val endretFomDatoBekreftelse =
            EndretSluttdatoBekreftelse(
                UUID.fromString(oppgaveReferanse),
                nySluttdato,
                uttalelse.harUttalelse
            )

        if (!uttalelse.uttalelseFraDeltaker.isNullOrBlank()) {
            endretFomDatoBekreftelse.medUttalelseFraBruker(uttalelse.uttalelseFraDeltaker)
        }

        return endretFomDatoBekreftelse
    }

    override fun dokumentTittelSuffix(): String = "endret sluttdato"

    override fun somKomplettOppgave(oppgaveDTO: OppgaveDTO): KomplettUngdomsytelseOppgaveDTO {
        return KomplettEndretSluttdatoUngdomsytelseOppgaveDTO(
            oppgaveReferanse = oppgaveReferanse,
            nySluttdato = nySluttdato,
            uttalelse = uttalelse
        )
    }
}

data class KomplettEndretPeriodeUngdomsytelseOppgaveDTO(
    override val oppgaveReferanse: String,
    override val uttalelse: UngdomsytelseOppgaveUttalelseDTO,
    val nyPeriode: PeriodeDTO?,
) : KomplettUngdomsytelseOppgaveDTO(oppgaveReferanse, uttalelse) {
    override fun somK9Format(): Bekreftelse {
        val endretPeriodeBekreftelse =
            EndretPeriodeBekreftelse(
                UUID.fromString(oppgaveReferanse),
                nyPeriode?.let { Periode(nyPeriode.fom, nyPeriode.tom) },
                uttalelse.harUttalelse
            )

        if (!uttalelse.uttalelseFraDeltaker.isNullOrBlank()) {
            endretPeriodeBekreftelse.medUttalelseFraBruker(uttalelse.uttalelseFraDeltaker)
        }

        return endretPeriodeBekreftelse
    }

    override fun dokumentTittelSuffix(): String = "endret periode"

    override fun somKomplettOppgave(oppgaveDTO: OppgaveDTO): KomplettUngdomsytelseOppgaveDTO {
        return KomplettEndretPeriodeUngdomsytelseOppgaveDTO(
            oppgaveReferanse = oppgaveReferanse,
            nyPeriode = nyPeriode,
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
            .medHarUttalelse(uttalelse.harUttalelse)

        if (!uttalelse.uttalelseFraDeltaker.isNullOrBlank()) {
            inntektBekreftelse.medUttalelseFraBruker(uttalelse.uttalelseFraDeltaker)
        }

        return inntektBekreftelse.build()
    }

    override fun dokumentTittelSuffix(): String = "kontroll av lønn"

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
