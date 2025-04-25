package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.k9Format

import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.Arbeidsgiver
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.PleiepengerSyktBarnSøknad
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstaker
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstid

internal fun PleiepengerSyktBarnSøknad.byggK9Arbeidstid(): Arbeidstid {
    val søknadsperiode = Periode(fraOgMed, tilOgMed)
    val arbeidstid = Arbeidstid().apply {

        if (arbeidsgivere.isNotEmpty()) medArbeidstaker(arbeidsgivere.tilK9Arbeidstaker(søknadsperiode))

        val frilansArbeidstidInfo = frilans.k9ArbeidstidInfo(søknadsperiode)
        val omsorgsstønadArbeidstidInfo = omsorgsstønad?.k9ArbeidstidInfo(søknadsperiode)

        medFrilanserArbeidstid(
            ArbeidstidInfoUtleder(
                førsteArbeidstidInfo = frilansArbeidstidInfo,
                andreArbeidstidInfo = omsorgsstønadArbeidstidInfo,
                totalPeriode = søknadsperiode
            ).utled()
        )

        selvstendigNæringsdrivende.arbeidsforhold?.let {
            medSelvstendigNæringsdrivendeArbeidstidInfo(selvstendigNæringsdrivende.k9ArbeidstidInfo(fraOgMed, tilOgMed))
        }
    }
    return arbeidstid
}

fun List<Arbeidsgiver>.tilK9Arbeidstaker(søknadsperiode: Periode): List<Arbeidstaker> {
    return this.map {
        Arbeidstaker()
            .medOrganisasjonsnummer(Organisasjonsnummer.of(it.organisasjonsnummer))
            .medOrganisasjonsnavn(it.navn)
            .medArbeidstidInfo(it.k9ArbeidstidInfo(søknadsperiode.fraOgMed, søknadsperiode.tilOgMed))
    }
}
