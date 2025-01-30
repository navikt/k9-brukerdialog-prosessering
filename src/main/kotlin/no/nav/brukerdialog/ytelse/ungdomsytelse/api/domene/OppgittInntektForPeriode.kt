package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene

import no.nav.k9.søknad.felles.type.Periode
import java.math.BigDecimal
import java.time.LocalDate
import no.nav.k9.søknad.ytelse.ung.v1.OppgittInntektForPeriode as UngOppgittInntektForPeriode

data class OppgittInntektForPeriode(
    val arbeidstakerOgFrilansInntekt: Double = 0.0,
    val næringsinntekt: Double = 0.0,
    val inntektFraYtelse: Double = 0.0,
    val periodeForInntekt: UngPeriode,
) {
    fun somUngOppgittInntektForPeriode(): UngOppgittInntektForPeriode = UngOppgittInntektForPeriode(
        BigDecimal.valueOf(arbeidstakerOgFrilansInntekt),
        BigDecimal.valueOf(næringsinntekt),
        BigDecimal.valueOf(inntektFraYtelse),
        periodeForInntekt.somUngPeriode(),
    )
}

data class UngPeriode(
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
) {
    fun somUngPeriode(): Periode = Periode(fraOgMed, tilOgMed)
}
