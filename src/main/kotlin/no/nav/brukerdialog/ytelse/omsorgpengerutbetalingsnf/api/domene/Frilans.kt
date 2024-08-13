package no.nav.brukerdialog.ytelse.omsorgpengerutbetalingsnf.api.domene

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotNull
import no.nav.k9.søknad.felles.opptjening.Frilanser
import no.nav.brukerdialog.utils.erLikEllerEtter
import java.time.LocalDate

class Frilans(
    @JsonFormat(pattern = "yyyy-MM-dd") private val startdato: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") private val sluttdato: LocalDate? = null,
    @field:NotNull(message = "Kan ikke være null") private val jobberFortsattSomFrilans: Boolean
) {

    @AssertTrue(message = "'Sluttdato' må være lik eller etter 'startdato'")
    fun isStartdato(): Boolean {
        if (sluttdato != null) {
            return sluttdato.erLikEllerEtter(startdato)
        }
        return true
    }

    @AssertTrue(message = "Dersom 'jobberFortsattSomFrilans' er true, kan ikke 'sluttdato' være satt")
    fun isSluttdato(): Boolean {
        if (jobberFortsattSomFrilans) {
            return sluttdato == null
        }
        return true
    }

    @AssertTrue(message = "Dersom 'jobberFortsattSomFrilans' er false, må 'sluttdato' være satt")
    fun isJobberFortsattSomFrilans(): Boolean {
        if (!jobberFortsattSomFrilans) {
            return sluttdato != null
        }
        return true
    }

    fun somK9Frilanser() = Frilanser().apply {
        medStartdato(this@Frilans.startdato)
        this@Frilans.sluttdato?.let { medSluttdato(this@Frilans.sluttdato) }
    }
}
