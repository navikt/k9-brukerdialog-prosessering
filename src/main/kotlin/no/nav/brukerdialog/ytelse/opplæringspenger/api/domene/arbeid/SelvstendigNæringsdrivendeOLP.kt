package no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid

import jakarta.validation.Valid
import no.nav.brukerdialog.ytelse.fellesdomene.Virksomhet
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid.ArbeidsforholdOLP.Companion.somK9ArbeidstidInfo
import java.time.LocalDate

data class SelvstendigNæringsdrivendeOLP(
    @field:Valid val virksomhet: Virksomhet,
    @field:Valid val arbeidsforhold: ArbeidsforholdOLP,
) {

    fun somK9SelvstendigNæringsdrivende() = virksomhet.somK9SelvstendigNæringsdrivende()
    fun somK9ArbeidstidInfo(fraOgMed: LocalDate, tilOgMed: LocalDate) =
        arbeidsforhold.somK9ArbeidstidInfo(fraOgMed, tilOgMed)
}
