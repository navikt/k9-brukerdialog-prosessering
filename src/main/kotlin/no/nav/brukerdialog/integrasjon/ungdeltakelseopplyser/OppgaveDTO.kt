package no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

data class OppgaveDTO(
    val id: UUID,
    val oppgavetype: Oppgavetype,
    val oppgavetypeData: OppgavetypeDataDTO,
    val status: OppgaveStatus,
    val opprettetDato: ZonedDateTime,
    val løstDato: ZonedDateTime?,
)

enum class Oppgavetype {
    BEKREFT_ENDRET_STARTDATO,
    BEKREFT_ENDRET_SLUTTDATO,
}


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = EndretStartdatoOppgavetypeDataDTO::class, name = "BEKREFT_ENDRET_STARTDATO"),
    JsonSubTypes.Type(value = EndretSluttdatoOppgavetypeDataDTO::class, name = "BEKREFT_ENDRET_SLUTTDATO"),
)
sealed class OppgavetypeDataDTO(
    open val veilederRef: String,
    open val meldingFraVeileder: String?,
)

data class EndretStartdatoOppgavetypeDataDTO(
    val nyStartdato: LocalDate,
    override val veilederRef: String,
    override val meldingFraVeileder: String?,
) : OppgavetypeDataDTO(veilederRef, meldingFraVeileder)

data class EndretSluttdatoOppgavetypeDataDTO(
    val nySluttdato: LocalDate,
    override val veilederRef: String,
    override val meldingFraVeileder: String?,
) : OppgavetypeDataDTO(veilederRef, meldingFraVeileder)

enum class OppgaveStatus {
    LØST,
    ULØST,
    KANSELLERT
}
