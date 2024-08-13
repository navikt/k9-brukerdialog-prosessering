package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene

import no.nav.brukerdialog.ytelse.fellesdomene.Land
import no.nav.brukerdialog.ytelse.fellesdomene.Næringstype
import no.nav.brukerdialog.utils.krever
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
