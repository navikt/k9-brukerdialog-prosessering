package no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid

import jakarta.validation.Valid
import no.nav.brukerdialog.ytelse.fellesdomene.Virksomhet
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid.Arbeidsforhold.Companion.somK9ArbeidstidInfo
import java.time.LocalDate

class SelvstendigNæringsdrivende(
    @field:Valid val virksomhet: Virksomhet,
    @field:Valid val arbeidsforhold: Arbeidsforhold,
) {

    fun somK9SelvstendigNæringsdrivende() = virksomhet.somK9SelvstendigNæringsdrivende()
    fun somK9ArbeidstidInfo(fraOgMed: LocalDate, tilOgMed: LocalDate) =
        arbeidsforhold.somK9ArbeidstidInfo(fraOgMed, tilOgMed)
}
