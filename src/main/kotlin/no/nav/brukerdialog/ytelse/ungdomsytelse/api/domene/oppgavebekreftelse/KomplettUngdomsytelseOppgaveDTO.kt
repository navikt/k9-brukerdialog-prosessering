package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser.OppgaveDTO
import no.nav.k9.oppgave.bekreftelse.Bekreftelse
import no.nav.k9.oppgave.bekreftelse.ung.periodeendring.EndretFomDatoBekreftelse
import no.nav.k9.oppgave.bekreftelse.ung.periodeendring.EndretTomDatoBekreftelse
import java.time.LocalDate

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = KomplettEndretStartdatoUngdomsytelseOppgaveDTO::class, name = "BEKREFT_ENDRET_STARTDATO"),
    JsonSubTypes.Type(value = KomplettEndretSluttdatoUngdomsytelseOppgaveDTO::class, name = "BEKREFT_ENDRET_SLUTTDATO"),
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
    override fun somK9Format(): Bekreftelse = EndretFomDatoBekreftelse(nyStartdato, bekreftelseSvar.somBoolean())

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

    override fun somK9Format(): Bekreftelse = EndretTomDatoBekreftelse(nySluttdato, bekreftelseSvar.somBoolean())

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
