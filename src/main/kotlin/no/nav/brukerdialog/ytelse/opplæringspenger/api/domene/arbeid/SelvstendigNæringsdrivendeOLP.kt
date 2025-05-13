package no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid

import jakarta.validation.Valid
import no.nav.brukerdialog.ytelse.fellesdomene.Virksomhet
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid.ArbeidsforholdOLP.Companion.somK9ArbeidstidInfo
import no.nav.k9.søknad.felles.type.Periode

data class SelvstendigNæringsdrivendeOLP(
    @field:Valid val virksomhet: Virksomhet,
    @field:Valid val arbeidsforhold: ArbeidsforholdOLP,
) {

    fun somK9SelvstendigNæringsdrivende() = virksomhet.somK9SelvstendigNæringsdrivende()
    fun somK9ArbeidstidInfo(perioder: List<Periode>) =
        arbeidsforhold.somK9ArbeidstidInfo(perioder)
}
