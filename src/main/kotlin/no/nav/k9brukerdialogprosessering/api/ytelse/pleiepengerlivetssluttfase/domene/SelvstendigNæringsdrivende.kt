package no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase.domene

import jakarta.validation.Valid
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Virksomhet
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase.domene.Arbeidsforhold.Companion.somK9ArbeidstidInfo
import java.time.LocalDate

class SelvstendigNæringsdrivende(
    @field:Valid val virksomhet: Virksomhet,
    @field:Valid val arbeidsforhold: Arbeidsforhold
) {
    internal fun valider(felt: String = "selvstendigNæringsdrivende") = mutableListOf<String>().apply {
        addAll(arbeidsforhold.valider("$felt.arbeidsforhold"))
    }

    fun somK9SelvstendigNæringsdrivende() = virksomhet.somK9SelvstendigNæringsdrivende()
    fun somK9ArbeidstidInfo(fraOgMed: LocalDate, tilOgMed: LocalDate) =
        arbeidsforhold.somK9ArbeidstidInfo(fraOgMed, tilOgMed)
}
