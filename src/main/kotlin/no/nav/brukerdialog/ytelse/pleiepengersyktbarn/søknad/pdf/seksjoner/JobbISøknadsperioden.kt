package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner

import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Arbeidsforhold
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Arbeidsgiver
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Frilans
import no.nav.brukerdialog.pdf.SpørsmålOgSvar
import no.nav.brukerdialog.pdf.lagVerdiElement3
import no.nav.brukerdialog.pdf.tilSpørsmålOgSvar

data class ArbeidsGiverISøknadsperiodeSpørsmålOgSvar(
    val arbeidsgiverNavnOgOrgnr: SpørsmålOgSvar? = null,
)

data class JobbISøknadsperiodenSpørsmålOgSvar(
    val ingenArbeidsforhold: SpørsmålOgSvar? = null,
    val arbeidsgivere: List<ArbeidsGiverISøknadsperiodeSpørsmålOgSvar>? = emptyList(),
    val frilans: SpørsmålOgSvar? = null,
    val selvstendigNæringsdrivendeArbeidsforhold: SpørsmålOgSvar? = null,
)

fun strukturerJobbISøknadsperiodenSeksjon(
    søknadSvarArbeidsgivere: List<Arbeidsgiver>,
    søknadSvarFrilans: Frilans?,
    søknadSvarSelvstendigNæringsdrivende: Arbeidsforhold?,
): VerdilisteElement {
    val jobbISøknadsperioden =
        mapJobbISøknadsperiodenTilSpørsmålOgSvar(søknadSvarArbeidsgivere, søknadSvarFrilans, søknadSvarSelvstendigNæringsdrivende)

    return VerdilisteElement(
        label = "Jobb i søknadsperioden",
        verdiliste =
            (
                jobbISøknadsperioden.arbeidsgivere?.mapNotNull { lagVerdiElement3(it.arbeidsgiverNavnOgOrgnr) }
                    ?: emptyList()
            ).plus(
                listOfNotNull(
                    lagVerdiElement3(jobbISøknadsperioden.ingenArbeidsforhold),
                    lagVerdiElement3(jobbISøknadsperioden.frilans),
                    lagVerdiElement3(jobbISøknadsperioden.selvstendigNæringsdrivendeArbeidsforhold),
                ),
            ),
    )
}

fun mapJobbISøknadsperiodenTilSpørsmålOgSvar(
    arbeidsgivere: List<Arbeidsgiver>,
    frilans: Frilans?,
    selvstendigNæringsdrivendeArbeidsforhold: Arbeidsforhold?,
): JobbISøknadsperiodenSpørsmålOgSvar =
    JobbISøknadsperiodenSpørsmålOgSvar(
        ingenArbeidsforhold =
            if (frilans?.arbeidsforhold == null &&
                selvstendigNæringsdrivendeArbeidsforhold == null &&
                arbeidsgivere.any { it.arbeidsforhold != null }
            ) {
                tilSpørsmålOgSvar("Arbeidsforhold i søknadsperioden:", "Ingen arbeidsforhold er registrert i søknadsperioden")
            } else {
                null
            },
        arbeidsgivere =
            arbeidsgivere.map { arbeidsgiver ->
                ArbeidsGiverISøknadsperiodeSpørsmålOgSvar(
                    arbeidsgiverNavnOgOrgnr =
                        arbeidsgiver.sluttetFørSøknadsperiode
                            .takeIf {
                                it != true
                            }?.let {
                                tilSpørsmålOgSvar(
                                    "${arbeidsgiver.navn} (orgnr: ${arbeidsgiver.organisasjonsnummer}",
                                    arbeidsgiver.arbeidsforhold,
                                )
                            },
                )
            },
        frilans = frilans?.arbeidsforhold?.let { tilSpørsmålOgSvar("Frilans", frilans.arbeidsforhold) },
        selvstendigNæringsdrivendeArbeidsforhold =
            selvstendigNæringsdrivendeArbeidsforhold?.let {
                tilSpørsmålOgSvar("Selvstendig næringsdrivende", selvstendigNæringsdrivendeArbeidsforhold)
            },
    )
