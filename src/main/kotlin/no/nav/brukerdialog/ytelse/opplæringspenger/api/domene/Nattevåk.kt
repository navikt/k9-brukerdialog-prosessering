package no.nav.brukerdialog.ytelse.opplæringspenger.api.domene

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotNull
//TODO fix imports for riktig søknad ytelse
import no.nav.k9.søknad.ytelse.psb.v1.Nattevåk.NattevåkPeriodeInfo
import no.nav.brukerdialog.utils.StringUtils
import no.nav.k9.søknad.ytelse.psb.v1.Nattevåk as K9Nattevåk

data class Nattevåk(
    @field:NotNull(message = "Kan ikke være null") val harNattevåk: Boolean? = null,
    val tilleggsinformasjon: String?,
) {

    companion object {
        const val MAX_FRITEKST_TEGN = 1000
    }

    override fun toString(): String {
        return "Nattevåk(harNattevåk=${harNattevåk})"
    }

    fun tilK9Nattevåk(
        periode: no.nav.k9.søknad.felles.type.Periode,
    ): no.nav.k9.søknad.ytelse.psb.v1.Nattevåk? = K9Nattevåk().medPerioder(
        mapOf(
            periode to NattevåkPeriodeInfo().medTilleggsinformasjon(tilleggsinformasjon?.let { StringUtils.saniter(it) })
        )
    )

    @AssertTrue(message = "Dersom harNattevåk er satt, må tilleggsinformasjon være satt")
    private fun isTilleggsinformasjon(): Boolean {
        if (harNattevåk == true) {
            return tilleggsinformasjon.isNullOrBlank().not()
        }
        return true
    }

    @AssertTrue(message = "Kan ikke være over $MAX_FRITEKST_TEGN tegn")
    private fun isTilleggsinformasjon_lengde(): Boolean {
        if (harNattevåk == true) {
            return tilleggsinformasjon != null && tilleggsinformasjon.length <= MAX_FRITEKST_TEGN
        }
        return true
    }
}
