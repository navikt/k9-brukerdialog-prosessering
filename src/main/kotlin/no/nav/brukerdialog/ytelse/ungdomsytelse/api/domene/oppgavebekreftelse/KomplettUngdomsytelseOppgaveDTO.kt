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
    open val oppgaveReferanse: String,
    open val veilederRef: String,
    open val meldingFraVeileder: String? = null,
) {
    abstract fun somK9Format(): Bekreftelse

    abstract fun somKomplettOppgave(oppgaveDTO: OppgaveDTO): KomplettUngdomsytelseOppgaveDTO
}

data class KomplettEndretStartdatoUngdomsytelseOppgaveDTO(
    override val oppgaveReferanse: String,
    override val veilederRef: String,
    override val meldingFraVeileder: String? = null,
    val nyStartdato: LocalDate,
    val bekreftelseSvar: BekreftelseSvar,
    val ikkeGodkjentResponse: UngdomsytelseIkkeGodkjentResponse? = null,
) : KomplettUngdomsytelseOppgaveDTO(oppgaveReferanse, veilederRef, meldingFraVeileder) {
    override fun somK9Format(): Bekreftelse {
        val endretFomDatoBekreftelse =
            EndretFomDatoBekreftelse(UUID.fromString(oppgaveReferanse), nyStartdato, bekreftelseSvar.somBoolean())

        if (ikkeGodkjentResponse != null) {
            endretFomDatoBekreftelse.medUttalelseFraBruker(ikkeGodkjentResponse.meldingFraDeltaker)
        }

        return endretFomDatoBekreftelse
    }

    override fun somKomplettOppgave(oppgaveDTO: OppgaveDTO): KomplettUngdomsytelseOppgaveDTO {
        return KomplettEndretStartdatoUngdomsytelseOppgaveDTO(
            oppgaveReferanse = oppgaveReferanse,
            veilederRef = oppgaveDTO.oppgavetypeData.veilederRef,
            meldingFraVeileder = oppgaveDTO.oppgavetypeData.meldingFraVeileder,
            nyStartdato = nyStartdato,
            bekreftelseSvar = bekreftelseSvar,
            ikkeGodkjentResponse = ikkeGodkjentResponse,
        )
    }
}

data class KomplettEndretSluttdatoUngdomsytelseOppgaveDTO(
    override val oppgaveReferanse: String,
    override val veilederRef: String,
    override val meldingFraVeileder: String? = null,
    val nySluttdato: LocalDate,
    val bekreftelseSvar: BekreftelseSvar,
    val ikkeGodkjentResponse: UngdomsytelseIkkeGodkjentResponse? = null,
) : KomplettUngdomsytelseOppgaveDTO(oppgaveReferanse, veilederRef, meldingFraVeileder) {

    override fun somK9Format(): Bekreftelse {
        val endretTomDatoBekreftelse =
            EndretTomDatoBekreftelse(UUID.fromString(oppgaveReferanse), nySluttdato, bekreftelseSvar.somBoolean())

        if (ikkeGodkjentResponse != null) {
            endretTomDatoBekreftelse.medUttalelseFraBruker(ikkeGodkjentResponse.meldingFraDeltaker)
        }

        return endretTomDatoBekreftelse
    }

    override fun somKomplettOppgave(oppgaveDTO: OppgaveDTO): KomplettUngdomsytelseOppgaveDTO {
        return KomplettEndretSluttdatoUngdomsytelseOppgaveDTO(
            oppgaveReferanse = oppgaveReferanse,
            veilederRef = oppgaveDTO.oppgavetypeData.veilederRef,
            meldingFraVeileder = oppgaveDTO.oppgavetypeData.meldingFraVeileder,
            nySluttdato = nySluttdato,
            bekreftelseSvar = bekreftelseSvar,
            ikkeGodkjentResponse = ikkeGodkjentResponse,
        )
    }
}

data class KomplettKontrollerRegisterInntektOppgaveTypeDataDTO(
    override val oppgaveReferanse: String,
    override val veilederRef: String,
    override val meldingFraVeileder: String? = null,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val registerinntekt: RegisterinntektDTO,
    val bekreftelseSvar: BekreftelseSvar,
    val ikkeGodkjentResponse: UngdomsytelseIkkeGodkjentResponse? = null,
) : KomplettUngdomsytelseOppgaveDTO(oppgaveReferanse, veilederRef, meldingFraVeileder) {

    override fun somK9Format(): Bekreftelse {
        val inntektBekreftelse = InntektBekreftelse.builder()
            .medOppgaveId(UUID.fromString(oppgaveReferanse))
            .medOppgittePeriodeinntekter(registerinntekt.somK9Format())
            .medHarBrukerGodtattEndringen(bekreftelseSvar.somBoolean())

        if (ikkeGodkjentResponse != null) {
            inntektBekreftelse.medUttalelseFraBruker(ikkeGodkjentResponse.meldingFraDeltaker)
        }

        return inntektBekreftelse.build()
    }

    override fun somKomplettOppgave(oppgaveDTO: OppgaveDTO): KomplettUngdomsytelseOppgaveDTO {
        return KomplettKontrollerRegisterInntektOppgaveTypeDataDTO(
            oppgaveReferanse = oppgaveReferanse,
            veilederRef = oppgaveDTO.oppgavetypeData.veilederRef,
            meldingFraVeileder = oppgaveDTO.oppgavetypeData.meldingFraVeileder,
            fraOgMed = fraOgMed,
            tilOgMed = tilOgMed,
            registerinntekt = registerinntekt,
            bekreftelseSvar = bekreftelseSvar,
            ikkeGodkjentResponse = ikkeGodkjentResponse,
        )
    }

    private fun RegisterinntektDTO.somK9Format(): MutableSet<OppgittInntektForPeriode> =
        mutableSetOf<OppgittInntektForPeriode>().apply {
            addAll(arbeidOgFrilansInntekter.map {
                OppgittInntektForPeriode.builder(Periode(fraOgMed, tilOgMed))
                    .medArbeidstakerOgFrilansinntekt(BigDecimal.valueOf(it.inntekt.toLong()))
                    .build()
            })
            addAll(ytelseInntekter.map {
                OppgittInntektForPeriode.builder(Periode(fraOgMed, tilOgMed))
                    .medYtelse(BigDecimal.valueOf(it.inntekt.toLong()))
                    .build()
            })
        }
}
