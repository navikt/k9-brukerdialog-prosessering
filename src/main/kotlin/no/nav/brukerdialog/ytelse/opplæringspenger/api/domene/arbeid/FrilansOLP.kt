package no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotNull
import no.nav.brukerdialog.utils.erLikEllerEtter
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid.ArbeidsforholdOLP.Companion.somK9ArbeidstidInfo
import no.nav.k9.søknad.felles.opptjening.Frilanser
import java.time.LocalDate

data class FrilansOLP(
    @JsonFormat(pattern = "yyyy-MM-dd")
    val startdato: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val sluttdato: LocalDate? = null,
    val jobberFortsattSomFrilans: Boolean,
    @field:Valid val arbeidsforhold: ArbeidsforholdOLP? = null,
    @field:NotNull(message = "Kan ikke være null") val harHattInntektSomFrilanser: Boolean? = null,
) {

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

    @AssertTrue(message = "'Sluttdato' må være lik eller etter 'startdato'")
    fun isSluttdatoEtterStartdato(): Boolean {
        if (sluttdato != null) {
            return sluttdato.erLikEllerEtter(startdato)
        }
        return true
    }

    internal fun somK9Frilanser() = Frilanser().apply {
        medStartdato(this@FrilansOLP.startdato)
        this@FrilansOLP.sluttdato?.let { medSluttdato(this@FrilansOLP.sluttdato) }
    }

    internal fun somK9Arbeidstid(fraOgMed: LocalDate, tilOgMed: LocalDate) =
        arbeidsforhold.somK9ArbeidstidInfo(fraOgMed, tilOgMed)

}
