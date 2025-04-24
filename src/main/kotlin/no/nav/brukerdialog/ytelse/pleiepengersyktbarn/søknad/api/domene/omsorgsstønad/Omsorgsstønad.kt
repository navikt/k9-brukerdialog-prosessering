package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.omsorgsstønad

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.arbeid.NULL_TIMER
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import java.time.Duration
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
) {
    abstract fun k9ArbeidstidInfo(søknadsperiode: Periode): ArbeidstidInfo
}

data class OmsorgsstønadMottarIkke(
    override val type: OmsorgsstønadType,
    override val mottarOmsorgsstønad: Boolean,
) : Omsorgsstønad(type = type, mottarOmsorgsstønad = mottarOmsorgsstønad) {
    override fun k9ArbeidstidInfo(søknadsperiode: Periode): ArbeidstidInfo = ArbeidstidInfo().medPerioder(
        mapOf(
            søknadsperiode to ArbeidstidPeriodeInfo()
                .medFaktiskArbeidTimerPerDag(NULL_TIMER)
                .medJobberNormaltTimerPerDag(NULL_TIMER)
        )
    )
}


data class OmsorgsstønadMottarDelerAvPerioden(
    override val type: OmsorgsstønadType,
    override val mottarOmsorgsstønad: Boolean,
    val startdato: LocalDate? = null, // dato settes hvis bruker har valgt ja på at hen starter i perioden
    val sluttdato: LocalDate? = null, // dato settes hvis bruker har valgt nei på at hen starter i perioden
    val antallTimer: Duration, // Antall timer i uken en mottar omsorgsstønad
) : Omsorgsstønad(type = type, mottarOmsorgsstønad = mottarOmsorgsstønad) {
    override fun k9ArbeidstidInfo(søknadsperiode: Periode): ArbeidstidInfo {
        val startdato = startdato ?: søknadsperiode.fraOgMed
        val sluttdato = sluttdato ?: søknadsperiode.tilOgMed

        return ArbeidstidInfo().medPerioder(
            mapOf(
                Periode(startdato, sluttdato) to ArbeidstidPeriodeInfo()
                    .medFaktiskArbeidTimerPerDag(antallTimer)
                    .medJobberNormaltTimerPerDag(antallTimer)
            )
        )
    }

}

data class OmsorgsstønadMottarHelePerioden(
    override val type: OmsorgsstønadType,
    override val mottarOmsorgsstønad: Boolean,
    val antallTimer: Duration, // Antall timer i uken en mottar omsorgsstønad
) : Omsorgsstønad(type = type, mottarOmsorgsstønad = mottarOmsorgsstønad) {
    override fun k9ArbeidstidInfo(søknadsperiode: Periode): ArbeidstidInfo = ArbeidstidInfo().medPerioder(
        mapOf(
            søknadsperiode to ArbeidstidPeriodeInfo()
                .medFaktiskArbeidTimerPerDag(antallTimer)
                .medJobberNormaltTimerPerDag(antallTimer)
        )
    )
}

enum class OmsorgsstønadType {
    MOTTAR_IKKE,
    MOTTAR_I_DELER_AV_PERIODEN,
    MOTTAR_I_HELE_PERIODEN
}
