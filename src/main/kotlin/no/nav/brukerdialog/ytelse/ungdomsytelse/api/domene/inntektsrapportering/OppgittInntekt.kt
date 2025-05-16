package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.inntektsrapportering

import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.constraints.AssertTrue

data class OppgittInntekt(
    val arbeidstakerOgFrilansInntekt: Int? = null,
) {

    @Hidden
    @AssertTrue(message = "Må ha oppgitt inntekt fra arbeidstaker/frilans")
    fun harOppgittInntekt(): Boolean = arbeidstakerOgFrilansInntekt != null
}
