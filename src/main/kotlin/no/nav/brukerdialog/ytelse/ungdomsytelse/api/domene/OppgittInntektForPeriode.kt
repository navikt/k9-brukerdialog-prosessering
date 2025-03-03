package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene

import no.nav.k9.søknad.felles.type.Periode
import java.math.BigDecimal
import java.time.LocalDate
import no.nav.k9.søknad.ytelse.ung.v1.OppgittInntektForPeriode as UngOppgittInntektForPeriode

data class OppgittInntektForPeriode(
    val arbeidstakerOgFrilansInntekt: Int? = null,
    val næringsinntekt: Int? = null,
    val inntektFraYtelse: Int? = null,
    val periodeForInntekt: UngPeriode,
) {
    fun somUngOppgittInntektForPeriode(): UngOppgittInntektForPeriode = UngOppgittInntektForPeriode(
        arbeidstakerOgFrilansInntekt?.let { BigDecimal.valueOf(it.toLong()) },
        næringsinntekt?.let { BigDecimal.valueOf(it.toLong()) },
        inntektFraYtelse?.let { BigDecimal.valueOf(it.toLong()) },
        periodeForInntekt.somUngPeriode(),
    )
}

data class UngPeriode(
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
) {
    fun somUngPeriode(): Periode = Periode(fraOgMed, tilOgMed)
}
