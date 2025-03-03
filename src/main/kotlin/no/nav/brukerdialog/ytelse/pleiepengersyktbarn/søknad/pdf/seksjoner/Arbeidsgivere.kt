package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner

import no.nav.brukerdialog.common.Constants.DATE_FORMATTER
import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Arbeidsgiver
import no.nav.brukerdialog.pdf.SpørsmålOgSvar
import no.nav.brukerdialog.pdf.lagVerdiElement
import no.nav.brukerdialog.pdf.normalArbeidstid
import no.nav.brukerdialog.pdf.tilSpørsmålOgSvar
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.PdfTekster
import java.time.LocalDate

data class ArbeidsgiverSpørsmålOgSvar(
    val arbeidsgiver: String,
    val erAnsatt: SpørsmålOgSvar? = null,
    val sluttetFørSøknadsperiode: SpørsmålOgSvar? = null,
    val timerPerUkeNormalt: SpørsmålOgSvar? = null,
)

data class ArbeidsgivereSpørsmålOgSvar(
    val arbeidsgivere: List<ArbeidsgiverSpørsmålOgSvar> = emptyList(),
    val ingenArbeidsforhold: SpørsmålOgSvar? = null,
)

fun strukturerArbeidsgivereSeksjon(
    søknadSvarArbeidsgivere: List<Arbeidsgiver>,
    søknadSvarFraOgMed: LocalDate,
): VerdilisteElement {
    val arbeidsgivere = mapArbeidsgivereTilSpørsmålOgSvar(søknadSvarArbeidsgivere, søknadSvarFraOgMed)
    return arbeidsgivere.takeIf { it.arbeidsgivere.isNotEmpty() }?.let {
        VerdilisteElement(
            label = PdfTekster.getValue("arbeidsgivere.tittel"),
            visningsVariant = "TABELL",
            verdiliste =
                arbeidsgivere.arbeidsgivere.map { arbeidsgiver ->
                    VerdilisteElement(
                        label = arbeidsgiver.arbeidsgiver,
                        verdiliste =
                            listOfNotNull(
                                lagVerdiElement(arbeidsgiver.erAnsatt),
                                lagVerdiElement(arbeidsgiver.sluttetFørSøknadsperiode),
                                lagVerdiElement(arbeidsgiver.timerPerUkeNormalt),
                            ),
                    )
                },
        )
    } ?: VerdilisteElement(
        label = "Arbeidsgivere",
        verdiliste = listOfNotNull(lagVerdiElement(arbeidsgivere.ingenArbeidsforhold)),
    )
}

fun mapArbeidsgivereTilSpørsmålOgSvar(
    arbeidsgivere: List<Arbeidsgiver>,
    fraOgMed: LocalDate,
): ArbeidsgivereSpørsmålOgSvar =
    ArbeidsgivereSpørsmålOgSvar(
        arbeidsgivere =
            arbeidsgivere.map { arbeidsgiver ->
                ArbeidsgiverSpørsmålOgSvar(
                    arbeidsgiver =
                        arbeidsgiver.navn?.let { "${arbeidsgiver.navn} (orgnr: ${arbeidsgiver.organisasjonsnummer})" }
                            ?: "Orgnr: ${arbeidsgiver.organisasjonsnummer}",
                    erAnsatt = tilSpørsmålOgSvar("Jobber du her nå?", arbeidsgiver.erAnsatt),
                    sluttetFørSøknadsperiode =
                        tilSpørsmålOgSvar(
                            "Sluttet du hos ${arbeidsgiver.navn ?: ""} før ${DATE_FORMATTER.format(fraOgMed)}?",
                            arbeidsgiver.sluttetFørSøknadsperiode,
                        ),
                    timerPerUkeNormalt =
                        tilSpørsmålOgSvar(
                            "Hvor mange timer jobber du normalt per uke?",
                            normalArbeidstid(arbeidsgiver.arbeidsforhold?.normalarbeidstid?.timerPerUkeISnitt),
                        ),
                )
            },
        ingenArbeidsforhold =
            arbeidsgivere.isEmpty().let {
                tilSpørsmålOgSvar("Arbeidsforhold", "Ingen arbeidsforhold registrert i AA-registeret.")
            },
    )
