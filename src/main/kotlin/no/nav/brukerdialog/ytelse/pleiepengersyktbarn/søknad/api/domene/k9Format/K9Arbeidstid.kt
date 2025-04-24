package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.k9Format

import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.Arbeidsgiver
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.PleiepengerSyktBarnSøknad
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstaker
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstid
import java.time.LocalDate

internal fun PleiepengerSyktBarnSøknad.byggK9Arbeidstid(): Arbeidstid {
    val arbeidstid = Arbeidstid().apply {

        if(arbeidsgivere.isNotEmpty()) medArbeidstaker(arbeidsgivere.tilK9Arbeidstaker(fraOgMed, tilOgMed))

        val frilansArbeidstidInfo = frilans.k9ArbeidstidInfo(fraOgMed, tilOgMed)
        val omsorgsstønadArbeidstidInfo = omsorgsstønad?.k9ArbeidstidInfo(Periode(fraOgMed, tilOgMed))
        if (omsorgsstønadArbeidstidInfo == null) {
            medFrilanserArbeidstid(frilansArbeidstidInfo)
        } else {
            // TODO: Slå sammen arbeidstidene til frilans og omsorgsstønad
            medFrilanserArbeidstid(frilansArbeidstidInfo)
        }

        selvstendigNæringsdrivende.arbeidsforhold?.let {
            medSelvstendigNæringsdrivendeArbeidstidInfo(selvstendigNæringsdrivende.k9ArbeidstidInfo(fraOgMed, tilOgMed))
        }
    }
    return arbeidstid
}

fun List<Arbeidsgiver>.tilK9Arbeidstaker(
    fraOgMed: LocalDate,
    tilOgMed: LocalDate
): List<Arbeidstaker> {
    return this.map {
            Arbeidstaker()
                .medOrganisasjonsnummer(Organisasjonsnummer.of(it.organisasjonsnummer))
                .medOrganisasjonsnavn(it.navn)
                .medArbeidstidInfo(it.k9ArbeidstidInfo(fraOgMed, tilOgMed))
    }
}
