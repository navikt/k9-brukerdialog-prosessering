package no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.k9Format

import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstaker
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstid
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.Arbeidsgiver
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.PleiepengerSyktBarnSøknad
import java.time.LocalDate

internal fun PleiepengerSyktBarnSøknad.byggK9Arbeidstid(): Arbeidstid {
    val arbeidstid = Arbeidstid().apply {

        if(arbeidsgivere.isNotEmpty()) medArbeidstaker(arbeidsgivere.tilK9Arbeidstaker(fraOgMed, tilOgMed))

        medFrilanserArbeidstid(frilans.k9ArbeidstidInfo(fraOgMed, tilOgMed))
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
