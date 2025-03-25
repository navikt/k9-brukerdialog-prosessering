package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser.OppgaveDTO
import no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser.RegisterinntektDTO
import no.nav.k9.oppgave.bekreftelse.Bekreftelse
import no.nav.k9.oppgave.bekreftelse.ung.inntekt.InntektBekreftelse
import no.nav.k9.oppgave.bekreftelse.ung.inntekt.OppgittInntektForPeriode
import no.nav.k9.oppgave.bekreftelse.ung.periodeendring.EndretFomDatoBekreftelse
import no.nav.k9.oppgave.bekreftelse.ung.periodeendring.EndretTomDatoBekreftelse
import no.nav.k9.søknad.felles.type.Periode
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = KomplettEndretStartdatoUngdomsytelseOppgaveDTO::class, name = "BEKREFT_ENDRET_STARTDATO"),
    JsonSubTypes.Type(value = KomplettEndretSluttdatoUngdomsytelseOppgaveDTO::class, name = "BEKREFT_ENDRET_SLUTTDATO"),
    JsonSubTypes.Type(value = KomplettKontrollerRegisterInntektOppgaveTypeDataDTO::class, name = "BEKREFT_AVVIK_REGISTERINNTEKT"),
)
sealed class KomplettUngdomsytelseOppgaveDTO(
    open val oppgaveId: String,
    open val veilederRef: String,
    open val meldingFraVeileder: String? = null,
) {
    abstract fun somK9Format(): Bekreftelse

    abstract fun somKomplettOppgave(oppgaveDTO: OppgaveDTO): KomplettUngdomsytelseOppgaveDTO
}

data class KomplettEndretStartdatoUngdomsytelseOppgaveDTO(
    override val oppgaveId: String,
    override val veilederRef: String,
    override val meldingFraVeileder: String? = null,
    val nyStartdato: LocalDate,
    val bekreftelseSvar: BekreftelseSvar,
    val ikkeGodkjentResponse: UngdomsytelseIkkeGodkjentResponse? = null,
) : KomplettUngdomsytelseOppgaveDTO(oppgaveId, veilederRef, meldingFraVeileder) {
    override fun somK9Format(): Bekreftelse {
        val endretFomDatoBekreftelse =
            EndretFomDatoBekreftelse(UUID.fromString(oppgaveId), nyStartdato, bekreftelseSvar.somBoolean())

        if (ikkeGodkjentResponse != null) {
            endretFomDatoBekreftelse.medUttalelseFraBruker(ikkeGodkjentResponse.meldingFraDeltaker)
        }

        return endretFomDatoBekreftelse
    }

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

data class KomplettEndretSluttdatoUngdomsytelseOppgaveDTO(
    override val oppgaveId: String,
    override val veilederRef: String,
    override val meldingFraVeileder: String? = null,
    val nySluttdato: LocalDate,
    val bekreftelseSvar: BekreftelseSvar,
    val ikkeGodkjentResponse: UngdomsytelseIkkeGodkjentResponse? = null,
) : KomplettUngdomsytelseOppgaveDTO(oppgaveId, veilederRef, meldingFraVeileder) {

    override fun somK9Format(): Bekreftelse {
        val endretTomDatoBekreftelse =
            EndretTomDatoBekreftelse(UUID.fromString(oppgaveId), nySluttdato, bekreftelseSvar.somBoolean())

        if (ikkeGodkjentResponse != null) {
            endretTomDatoBekreftelse.medUttalelseFraBruker(ikkeGodkjentResponse.meldingFraDeltaker)
        }

        return endretTomDatoBekreftelse
    }

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

data class KomplettKontrollerRegisterInntektOppgaveTypeDataDTO(
    override val oppgaveId: String,
    override val veilederRef: String,
    override val meldingFraVeileder: String? = null,
    val fomDato: LocalDate,
    val tomDato: LocalDate,
    val registerinntekt: RegisterinntektDTO,
    val bekreftelseSvar: BekreftelseSvar,
    val ikkeGodkjentResponse: UngdomsytelseIkkeGodkjentResponse? = null,
) : KomplettUngdomsytelseOppgaveDTO(oppgaveId, veilederRef, meldingFraVeileder) {

    override fun somK9Format(): Bekreftelse = InntektBekreftelse.builder()
        .medOppgaveId(UUID.fromString(oppgaveId))
        .medOppgittePeriodeinntekter(registerinntekt.somK9Format())
        .medUttalelseFraBruker(meldingFraVeileder)
        .medHarBrukerGodtattEndringen(bekreftelseSvar.somBoolean())
        .build()

    override fun somKomplettOppgave(oppgaveDTO: OppgaveDTO): KomplettUngdomsytelseOppgaveDTO {
        return KomplettKontrollerRegisterInntektOppgaveTypeDataDTO(
            oppgaveId = oppgaveId,
            veilederRef = oppgaveDTO.oppgavetypeData.veilederRef,
            meldingFraVeileder = oppgaveDTO.oppgavetypeData.meldingFraVeileder,
            fomDato = fomDato,
            tomDato = tomDato,
            registerinntekt = registerinntekt,
            bekreftelseSvar = bekreftelseSvar,
            ikkeGodkjentResponse = ikkeGodkjentResponse,
        )
    }

    private fun RegisterinntektDTO.somK9Format(): MutableSet<OppgittInntektForPeriode> =
        mutableSetOf<OppgittInntektForPeriode>().apply {
            addAll(arbeidOgFrilansInntekter.map {
                OppgittInntektForPeriode.builder(Periode(fomDato, tomDato))
                    .medArbeidstakerOgFrilansinntekt(BigDecimal.valueOf(it.inntekt.toLong()))
                    .build()
            })
            addAll(ytelseInntekter.map {
                OppgittInntektForPeriode.builder(Periode(fomDato, tomDato))
                    .medYtelse(BigDecimal.valueOf(it.inntekt.toLong()))
                    .build()
            })
        }
}
