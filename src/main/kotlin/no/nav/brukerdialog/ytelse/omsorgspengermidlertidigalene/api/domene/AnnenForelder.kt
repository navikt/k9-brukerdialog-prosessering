package no.nav.brukerdialog.ytelse.omsorgspengermidlertidigalene.api.domene

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.brukerdialog.ytelse.omsorgspengermidlertidigalene.api.domene.Situasjon.ANNET
import no.nav.brukerdialog.ytelse.omsorgspengermidlertidigalene.api.domene.Situasjon.FENGSEL
import no.nav.brukerdialog.ytelse.omsorgspengermidlertidigalene.api.domene.Situasjon.INNLAGT_I_HELSEINSTITUSJON
import no.nav.brukerdialog.ytelse.omsorgspengermidlertidigalene.api.domene.Situasjon.SYKDOM
import no.nav.brukerdialog.ytelse.omsorgspengermidlertidigalene.api.domene.Situasjon.UTØVER_VERNEPLIKT
import no.nav.brukerdialog.utils.StringUtils
import no.nav.brukerdialog.utils.erLikEllerEtter
import no.nav.brukerdialog.validation.fritekst.ValidFritekst
import java.time.LocalDate
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.AnnenForelder as K9AnnenForelder

data class AnnenForelder(
    @field:Size(min = 11, max = 11)
    @field:Pattern(regexp = "^\\d+$", message = "'\${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    val fnr: String,

    @field:NotBlank(message = "Kan ikke være tomt eller blankt") val navn: String,

    val situasjon: Situasjon,

    @field:ValidFritekst
    val situasjonBeskrivelse: String? = null,

    val periodeOver6Måneder: Boolean? = null,

    @JsonFormat(pattern = "yyyy-MM-dd")
    val periodeFraOgMed: LocalDate,

    @JsonFormat(pattern = "yyyy-MM-dd")
    val periodeTilOgMed: LocalDate? = null,
) {

    internal fun somK9AnnenForelder(): K9AnnenForelder {
        return K9AnnenForelder()
            .medNorskIdentitetsnummer(NorskIdentitetsnummer.of(fnr))
            .medSituasjon(situasjon.somK9SituasjonType(), situasjonBeskrivelse?.let { StringUtils.saniter(it) })
            .apply {
                if (periodeTilOgMed != null) this.medPeriode(Periode(periodeFraOgMed, periodeTilOgMed))
            }
    }

    @Hidden
    @AssertTrue(message = "Derom 'periodeTilOgMed' er satt må den være lik eller etter 'periodeFraOgMed'")
    fun isPeriodeTilOgMed(): Boolean {
        if (periodeTilOgMed != null) {
            return periodeTilOgMed.erLikEllerEtter(periodeFraOgMed)
        }
        return true
    }

    @Hidden
    @AssertTrue(message = "Derom 'situasjon' er 'INNLAGT_I_HELSEINSTITUSJON', 'SYKDOM', eller 'ANNET' må 'periodeTilOgMed' eller 'periodeOver6Måneder' være satt")
    fun isSituasjon_innlagt_i_helseinstitusjon_sykdom_eller_annet(): Boolean {
        return when (situasjon) {
            INNLAGT_I_HELSEINSTITUSJON, SYKDOM, ANNET -> {
                periodeTilOgMed != null || periodeOver6Måneder != null
            }

            else -> true
        }
    }

    @Hidden
    @AssertTrue(message = "Derom 'situasjon' er 'SYKDOM', eller 'ANNET' må 'situasjonBeskrivelse' være satt")
    fun isSituasjonBeskrivelse(): Boolean {
        return when (situasjon) {
            SYKDOM, ANNET -> {
                !situasjonBeskrivelse.isNullOrBlank()
            }

            else -> true
        }
    }

    @Hidden
    @AssertTrue(message = "Derom 'situasjon' er 'UTØVER_VERNEPLIKT', 'SYKDOM', 'ANNET' eller 'FENGSEL' må 'periodeTilOgMed' være satt")
    fun isSituasjon_utøver_verneplikt_eller_fengsel(): Boolean {
        return when (situasjon) {
            UTØVER_VERNEPLIKT, FENGSEL -> {
                periodeTilOgMed != null
            }

            else -> true
        }
    }
}
