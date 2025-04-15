package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.fosterhjemgodtgjørelse

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.constraints.AssertTrue
import java.time.LocalDate


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true, // for å gjøre type synlig ved deserialisering
)
@JsonSubTypes(
    JsonSubTypes.Type(value = FosterhjemsgodtgjørelseMottarIkke::class, name = "MOTTAR_IKKE"),
    JsonSubTypes.Type(value = FosterhjemsgodtgjørelseFrikjøpt::class, name = "MOTTAR_FRIKJØPT"),
    JsonSubTypes.Type(value = FosterhjemsgodtgjørelseIkkeFrikjøpt::class, name = "MOTTAR_I_DELER_AV_PERIODEN"),
    JsonSubTypes.Type(value = FosterhjemsgodtgjørelseIkkeFrikjøpt::class, name = "MOTTAR_I_HELE_PERIODEN")
)
sealed class Fosterhjemgodtgjørelse(
    open val type: FosterhjemgodtgjørelseType,
    open val mottarFosterhjemsgodtgjørelse: Boolean,
) {
    @Hidden
    @AssertTrue(message = "Dersom type er MOTTAR_IKKE, må mottarFosterhjemsgodtgjørelse være false. Ellers må den være true.")
    fun isMottarFosterhjemsgodtgjørelse(): Boolean = when (type) {
        FosterhjemgodtgjørelseType.MOTTAR_IKKE -> !mottarFosterhjemsgodtgjørelse
        else -> mottarFosterhjemsgodtgjørelse
    }
}

data class FosterhjemsgodtgjørelseMottarIkke(
    override val type: FosterhjemgodtgjørelseType,
    override val mottarFosterhjemsgodtgjørelse: Boolean
) : Fosterhjemgodtgjørelse(type = type, mottarFosterhjemsgodtgjørelse = mottarFosterhjemsgodtgjørelse)

data class FosterhjemsgodtgjørelseFrikjøpt(
    override val type: FosterhjemgodtgjørelseType,
    override val mottarFosterhjemsgodtgjørelse: Boolean,
    val erFrikjøptFraJobb: Boolean,
    val frikjøptBeskrivelse: String,
) : Fosterhjemgodtgjørelse(type = type, mottarFosterhjemsgodtgjørelse = mottarFosterhjemsgodtgjørelse) {

    @Hidden
    @AssertTrue(message = "Dersom type er MOTTAR_FRIKJØPT, må erFrikjøptFraJobb være true. Ellers må den være false.")
    fun isErFrikjøptFraJobb(): Boolean = when (type) {
        FosterhjemgodtgjørelseType.MOTTAR_FRIKJØPT -> erFrikjøptFraJobb
        else -> !erFrikjøptFraJobb
    }

    @Hidden
    @AssertTrue(message = "Dersom type er MOTTAR_FRIKJØPT, må frikjøptBeskrivelse være utfylt.")
    fun isFrikjøptBeskrivelse(): Boolean = when (type) {
        FosterhjemgodtgjørelseType.MOTTAR_FRIKJØPT -> frikjøptBeskrivelse.isNotBlank()
        else -> frikjøptBeskrivelse.isBlank()
    }
}

data class FosterhjemsgodtgjørelseIkkeFrikjøpt(
    override val type: FosterhjemgodtgjørelseType,
    override val mottarFosterhjemsgodtgjørelse: Boolean,
    val erFrikjøptFraJobb: Boolean,
    val startdato: LocalDate? = null, // dato settes hvis bruker har valgt ja på at hen starter i perioden
    val sluttdato: LocalDate? = null, // dato settes hvis bruker har valgt nei på at hen starter i perioden
) : Fosterhjemgodtgjørelse(type = type, mottarFosterhjemsgodtgjørelse = mottarFosterhjemsgodtgjørelse) {

    @Hidden
    @AssertTrue(message = "Feltet erFrikjøptFraJobb kan ikke være true dersom type er annet enn MOTTAR_FRIKJØPT")
    fun isErFrikjøptFraJobb(): Boolean = when (type) {
        FosterhjemgodtgjørelseType.MOTTAR_FRIKJØPT -> erFrikjøptFraJobb
        else -> !erFrikjøptFraJobb
    }

    @Hidden
    @AssertTrue(message = "Dersom type er MOTTAR_I_DELER_AV_PERIODEN, må startdato eller sluttdato være satt")
    fun isStartdatoEllerSluttdatoSatt(): Boolean = when (type) {
        FosterhjemgodtgjørelseType.MOTTAR_I_DELER_AV_PERIODEN -> startdato != null || sluttdato != null
        else -> startdato == null && sluttdato == null
    }
}

enum class FosterhjemgodtgjørelseType {
    MOTTAR_IKKE,
    MOTTAR_FRIKJØPT,
    MOTTAR_I_DELER_AV_PERIODEN,
    MOTTAR_I_HELE_PERIODEN
}
