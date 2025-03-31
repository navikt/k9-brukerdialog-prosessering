package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.inntektsrapportering

import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.constraints.AssertTrue
import no.nav.k9.søknad.felles.type.Periode
import java.math.BigDecimal
import java.time.LocalDate
import no.nav.k9.søknad.ytelse.ung.v1.inntekt.OppgittInntektForPeriode as UngOppgittInntektForPeriode

data class OppgittInntektForPeriode(
    val arbeidstakerOgFrilansInntekt: Int? = null,
    val inntektFraYtelse: Int? = null,
    val periodeForInntekt: UngPeriode,
) {

    @Hidden
    @AssertTrue(message = "Må enten ha oppgitt inntekt fra arbeidstaker/frilans eller inntekt fra ytelse")
    fun harOppgittInntekt(): Boolean = arbeidstakerOgFrilansInntekt != null || inntektFraYtelse != null

    fun somUngOppgittInntektForPeriode(): UngOppgittInntektForPeriode = UngOppgittInntektForPeriode(
        arbeidstakerOgFrilansInntekt?.let { BigDecimal.valueOf(it.toLong()) },
        BigDecimal.ZERO,
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
