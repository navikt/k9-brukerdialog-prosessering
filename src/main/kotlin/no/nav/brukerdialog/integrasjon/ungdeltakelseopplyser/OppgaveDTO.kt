package no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

data class OppgaveDTO(
    val oppgaveReferanse: UUID,
    val oppgavetype: Oppgavetype,
    val oppgavetypeData: OppgavetypeDataDTO,
    val status: OppgaveStatus,
    val opprettetDato: ZonedDateTime,
    val løstDato: ZonedDateTime?,
)

enum class Oppgavetype {
    BEKREFT_ENDRET_STARTDATO,
    BEKREFT_ENDRET_SLUTTDATO,
    BEKREFT_AVVIK_REGISTERINNTEKT
}


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = EndretStartdatoOppgavetypeDataDTO::class, name = "BEKREFT_ENDRET_STARTDATO"),
    JsonSubTypes.Type(value = EndretSluttdatoOppgavetypeDataDTO::class, name = "BEKREFT_ENDRET_SLUTTDATO"),
    JsonSubTypes.Type(value = KontrollerRegisterInntektOppgaveTypeDataDTO::class, name = "BEKREFT_AVVIK_REGISTERINNTEKT"),
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

data class KontrollerRegisterInntektOppgaveTypeDataDTO(
    @JsonProperty(defaultValue = "n/a") val registerinntekt: RegisterinntektDTO,
    @JsonProperty(defaultValue = "n/a") val fraOgMed: LocalDate,
    @JsonProperty(defaultValue = "n/a") val tilOgMed: LocalDate,
) : OppgavetypeDataDTO("n/a", null)


data class RegisterinntektDTO(
    @JsonProperty("arbeidOgFrilansInntekter") val arbeidOgFrilansInntekter: List<ArbeidOgFrilansRegisterInntektDTO>,
    @JsonProperty("ytelseInntekter") val ytelseInntekter: List<YtelseRegisterInntektDTO>
)

data class ArbeidOgFrilansRegisterInntektDTO(
    @JsonProperty("inntekt") val inntekt: Int,
    @JsonProperty("arbeidsgiver") val arbeidsgiver: String
)


data class YtelseRegisterInntektDTO(
    @JsonProperty("inntekt") val inntekt: Int,
    @JsonProperty("ytelsetype") val ytelsetype: String
)

enum class OppgaveStatus {
    LØST,
    ULØST,
    AVBRUTT
}
