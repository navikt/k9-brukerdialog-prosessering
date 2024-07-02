package no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase.domene


import no.nav.k9brukerdialogprosessering.utils.erLikEllerEtter
import no.nav.k9brukerdialogprosessering.utils.krever
import java.time.LocalDate

class OpptjeningIUtlandet(
    private val navn: String,
    private val opptjeningType: OpptjeningType,
    private val land: no.nav.k9brukerdialogapi.ytelse.fellesdomene.Land,
    private val fraOgMed: LocalDate,
    private val tilOgMed: LocalDate
) {
    companion object {
        internal fun List<OpptjeningIUtlandet>.valider(felt: String = "opptjeningIUtlandet") = flatMapIndexed { index, opptjeningIUtlandet ->
            opptjeningIUtlandet.valider("$felt[$index]")
        }
    }

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        krever(tilOgMed.erLikEllerEtter(fraOgMed), "$felt.tilOgMed må være lik eller etter fraOgMed.")
        addAll(land.valider("$felt.land"))
    }
}

enum class OpptjeningType { ARBEIDSTAKER, FRILANSER }
