package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene

import no.nav.brukerdialog.ytelse.fellesdomene.Land
import no.nav.k9.søknad.felles.opptjening.UtenlandskArbeidsforhold
import java.time.LocalDate

import no.nav.k9.søknad.felles.type.Periode

data class OpptjeningIUtlandet(
    val navn: String,
    val opptjeningType: OpptjeningType,
    val land: Land,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate
) {
    fun somUtenlandskArbeidsforhold() = UtenlandskArbeidsforhold()
        .medLand(land.somK9Landkode())
        .medArbeidsgiversnavn(navn)
        .medAnsettelsePeriode(Periode(fraOgMed, tilOgMed))
}

enum class OpptjeningType {
    ARBEIDSTAKER,
    FRILANSER
}

internal fun List<OpptjeningIUtlandet>.valider() = mutableListOf<String>().apply {
    this@valider.forEachIndexed { index, opptjeningIUtlandet -> addAll(opptjeningIUtlandet.valider("opptjeningIUtlandet[$index]")) }
}

private fun OpptjeningIUtlandet.valider(felt: String) = mutableListOf<String>().apply {
    this.addAll(land.valider("$felt.land"))
}
