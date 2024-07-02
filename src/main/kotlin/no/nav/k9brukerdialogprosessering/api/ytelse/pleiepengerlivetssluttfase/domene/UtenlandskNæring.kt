package no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase.domene

import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Land
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Næringstype
import no.nav.k9brukerdialogprosessering.utils.erLikEllerEtter
import no.nav.k9brukerdialogprosessering.utils.krever
import java.time.LocalDate

class UtenlandskNæring(
    private val næringstype: Næringstype,
    private val navnPåVirksomheten: String,
    private val land: Land,
    private val organisasjonsnummer: String? = null,
    private val fraOgMed: LocalDate,
    private val tilOgMed: LocalDate? = null,
) {

    companion object {
        internal fun List<UtenlandskNæring>.valider(felt: String = "utenlandskNæring") =
            flatMapIndexed { index, utenlandskNæring ->
                utenlandskNæring.valider("$felt[$index]")
            }
    }

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        tilOgMed?.let { krever(tilOgMed.erLikEllerEtter(fraOgMed), "$felt.tilOgMed må være lik eller etter fraOgMed.") }
        addAll(land.valider("$felt.land"))
    }

}
