package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.arbeid

import java.time.Duration

internal val NULL_TIMER = Duration.ZERO
internal const val DAGER_PER_UKE = 5L

enum class ArbeidIPeriodeType {
    ARBEIDER_VANLIG,
    ARBEIDER_REDUSERT,
    ARBEIDER_IKKE,
    IKKE_BESVART
}

enum class RedusertArbeidstidType {
    PROSENT_AV_NORMALT,
    TIMER_I_SNITT_PER_UKE,
    ULIKE_UKER_TIMER
}
