package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.tilgang

import com.fasterxml.jackson.annotation.JsonValue

/**
 * Respons fra GET /pleiepenger-sykt-barn/endringsmelding/tilgang.
 *
 * [årsaker] er tom når [kanBruke] er true. Rekkefølgen på årsakene er en del av kontrakten:
 * frontend viser melding for første årsak.
 */
data class TilgangResultat(
    val kanBruke: Boolean,
    val årsaker: List<IngenTilgangÅrsak>,
) {
    companion object {
        fun tilgang() = TilgangResultat(kanBruke = true, årsaker = emptyList())
        fun ingenTilgang(årsaker: List<IngenTilgangÅrsak>): TilgangResultat {
            require(årsaker.isNotEmpty()) { "Avslag uten årsak." }
            return TilgangResultat(kanBruke = false, årsaker = årsaker)
        }
    }
}

/**
 * [kode] er verdien i JSON-kontrakten og må være identisk med `IngenTilgangÅrsak` i frontend, som har egen
 * melding per verdi. Konstantnavnene følger Kotlin-konvensjonen og er ikke en del av kontrakten.
 */
enum class IngenTilgangÅrsak(@get:JsonValue val kode: String) {
    HAR_INGEN_SAK("harIngenSak"),
    SØKNADSPERIODER_UTENFOR_TILLATT_ENDRINGSPERIODE("søknadsperioderUtenforTillattEndringsperiode"),
    HAR_MER_ENN_EN_SAK("harMerEnnEnSak"),
    HAR_INGEN_PERIODER("harIngenPerioder"),
    HAR_ARBEIDSTID_SOM_SELVSTENDIG_NÆRINGSDRIVENDE("harArbeidstidSomSelvstendigNæringsdrivende"),
    EN_ARBEIDSGIVER_TO_ANSETTELSER_SAMME_UKE_MED_OPPHOLD("enArbeidsgiverToAnsettelserSammeUkeMedOpphold"),
    HAR_FLERE_ANSETTELSESFORHOLD_HOS_UKJENT_ARBEIDSGIVER("harFlereAnsettelsesforholdHosUkjentArbeidsgiver"),
}
