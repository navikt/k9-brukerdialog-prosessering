package no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene

import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Land
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Næringstype
import no.nav.k9brukerdialogprosessering.utils.krever
import java.time.LocalDate

class UtenlandskNæring(
    val næringstype: Næringstype,
    val navnPåVirksomheten: String,
    val land: Land,
    val organisasjonsnummer: String? = null,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate? = null,
) {
    companion object {
        internal fun List<UtenlandskNæring>.valider(felt: String) = this.flatMapIndexed { index, utenlandskNæring ->
            utenlandskNæring.valider("$felt[$index]")
        }
    }

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        addAll(land.valider("$felt.land"))
        tilOgMed?.let { krever(tilOgMed.erLikEllerEtter(fraOgMed), "$felt.tilOgMed må være lik eller etter fraOgMed") }
    }
}

internal fun LocalDate.erLikEllerEtter(tilOgMedDato: LocalDate) =
    this.isEqual(tilOgMedDato) || this.isAfter(tilOgMedDato)
