package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.omsorgsstønad

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.math.BigDecimal
import java.time.LocalDate

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true, // for å gjøre type synlig ved deserialisering
)
@JsonSubTypes(
    JsonSubTypes.Type(value = OmsorgsstønadMottarIkke::class, name = "MOTTAR_IKKE"),
    JsonSubTypes.Type(value = OmsorgsstønadMottarDelerAvPerioden::class, name = "MOTTAR_I_DELER_AV_PERIODEN"),
    JsonSubTypes.Type(value = OmsorgsstønadMottarHelePerioden::class, name = "MOTTAR_I_HELE_PERIODEN")
)
sealed class Omsorgsstønad(
    open val type: OmsorgsstønadType,
    open val mottarOmsorgsstønad: Boolean,
)

data class OmsorgsstønadMottarIkke(
    override val type: OmsorgsstønadType,
    override val mottarOmsorgsstønad: Boolean
) : Omsorgsstønad(type = type, mottarOmsorgsstønad = mottarOmsorgsstønad)

data class OmsorgsstønadMottarDelerAvPerioden(
    override val type: OmsorgsstønadType,
    override val mottarOmsorgsstønad: Boolean,
    val startdato: LocalDate? = null, // dato settes hvis bruker har valgt ja på at hen starter i perioden
    val sluttdato: LocalDate? = null, // dato settes hvis bruker har valgt nei på at hen starter i perioden
    val antallTimer: Int, // Antall timer i uken en mottar omsorgsstønad
) : Omsorgsstønad(type = type, mottarOmsorgsstønad = mottarOmsorgsstønad)

data class OmsorgsstønadMottarHelePerioden(
    override val type: OmsorgsstønadType,
    override val mottarOmsorgsstønad: Boolean,
    val antallTimer: Int, // Antall timer i uken en mottar omsorgsstønad
): Omsorgsstønad(type = type, mottarOmsorgsstønad = mottarOmsorgsstønad)

enum class OmsorgsstønadType {
    MOTTAR_IKKE,
    MOTTAR_I_DELER_AV_PERIODEN,
    MOTTAR_I_HELE_PERIODEN
}
