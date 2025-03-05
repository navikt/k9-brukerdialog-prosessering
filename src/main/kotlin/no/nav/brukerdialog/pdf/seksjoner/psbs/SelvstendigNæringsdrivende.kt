package no.nav.brukerdialog.pdf.seksjoner.psbs

import no.nav.brukerdialog.common.Constants.DATE_FORMATTER
import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.SelvstendigNæringsdrivende
import no.nav.brukerdialog.pdf.SpørsmålOgSvar
import no.nav.brukerdialog.pdf.lagVerdiElement
import no.nav.brukerdialog.pdf.normalArbeidstid
import no.nav.brukerdialog.pdf.tilSpørsmålOgSvar

data class SelvstendigNæringsdrivendeSpørsmålOgSvar(
    val næringsinntekt: SpørsmålOgSvar? = null,
    val datoDuBleYrkesaktiv: SpørsmålOgSvar? = null,
    val timerPerUkeNormalt: SpørsmålOgSvar? = null,
    val datoForVarigEndring: SpørsmålOgSvar? = null,
    val næringsinntektEtterEndring: SpørsmålOgSvar? = null,
    val forklaringVarigEndring: SpørsmålOgSvar? = null,
    val flereEnnEnVirksomhet: SpørsmålOgSvar? = null,
    val virksomhetNavn: String? = null,
    val virksomhetType: SpørsmålOgSvar? = null,
    val registrertINorge: SpørsmålOgSvar? = null,
    val registrertIUtlandet: SpørsmålOgSvar? = null,
    val regnskapsførerNavn: SpørsmålOgSvar? = null,
    val regnskapsførerTlfnummer: SpørsmålOgSvar? = null,
    val ikkeSelvstendigNæringsdrivende: SpørsmålOgSvar? = null,
)

fun strukturerSelvstendigNæringsdrivendeSeksjon(søknadSvarSelvstendigNæringsdrivende: SelvstendigNæringsdrivende): VerdilisteElement {
    val selvstendigNæringsdrivende = mapSelvstendigNæringsdrivendeTilSpørsmålOgSvar(søknadSvarSelvstendigNæringsdrivende)
    return VerdilisteElement(
        label = "Selvstendig næringsdrivende",
        verdiliste =
            listOfNotNull(
                lagVerdiElement(selvstendigNæringsdrivende.næringsinntekt),
                lagVerdiElement(selvstendigNæringsdrivende.datoDuBleYrkesaktiv),
                lagVerdiElement(selvstendigNæringsdrivende.timerPerUkeNormalt),
                lagVerdiElement(selvstendigNæringsdrivende.datoForVarigEndring),
                lagVerdiElement(selvstendigNæringsdrivende.næringsinntektEtterEndring),
                lagVerdiElement(selvstendigNæringsdrivende.forklaringVarigEndring),
                lagVerdiElement(selvstendigNæringsdrivende.flereEnnEnVirksomhet),
                lagVerdiElement(selvstendigNæringsdrivende.ikkeSelvstendigNæringsdrivende),
                selvstendigNæringsdrivende.virksomhetNavn?.let {
                    VerdilisteElement(
                        label = "Næringsvirksomhet som du har lagt inn:",
                        visningsVariant = "TABELL",
                        verdiliste =
                            listOfNotNull(
                                VerdilisteElement(
                                    label = selvstendigNæringsdrivende.virksomhetNavn,
                                    verdiliste =
                                        listOfNotNull(
                                            lagVerdiElement(selvstendigNæringsdrivende.virksomhetType),
                                            lagVerdiElement(selvstendigNæringsdrivende.registrertINorge),
                                            lagVerdiElement(selvstendigNæringsdrivende.registrertIUtlandet),
                                            lagVerdiElement(selvstendigNæringsdrivende.regnskapsførerNavn),
                                            lagVerdiElement(selvstendigNæringsdrivende.regnskapsførerTlfnummer),
                                        ),
                                ),
                            ),
                    )
                },
            ),
    )
}

fun mapSelvstendigNæringsdrivendeTilSpørsmålOgSvar(
    selvstendigNæringsdrivende: SelvstendigNæringsdrivende,
): SelvstendigNæringsdrivendeSpørsmålOgSvar =
    selvstendigNæringsdrivende
        .takeIf {
            it.harInntektSomSelvstendig
        }?.let {
            SelvstendigNæringsdrivendeSpørsmålOgSvar(
                næringsinntekt = tilSpørsmålOgSvar("Næringsinntekt: ", selvstendigNæringsdrivende.virksomhet?.næringsinntekt),
                datoDuBleYrkesaktiv =
                    tilSpørsmålOgSvar(
                        "Oppgi dato for når du ble yrkesaktiv: ",
                        selvstendigNæringsdrivende.virksomhet?.yrkesaktivSisteTreFerdigliknedeÅrene?.oppstartsdato,
                    ),
                timerPerUkeNormalt =
                    tilSpørsmålOgSvar(
                        "Hvor mange timer jobber du normalt per uke?",
                        normalArbeidstid(selvstendigNæringsdrivende.arbeidsforhold?.normalarbeidstid?.timerPerUkeISnitt),
                    ),
                datoForVarigEndring =
                    tilSpørsmålOgSvar(
                        "Dato for når varig endring oppsto: ",
                        selvstendigNæringsdrivende.virksomhet?.varigEndring?.dato,
                    ),
                næringsinntektEtterEndring =
                    tilSpørsmålOgSvar(
                        "Næringsinntekt etter endringen: ",
                        selvstendigNæringsdrivende.virksomhet?.varigEndring?.inntektEtterEndring,
                    ),
                forklaringVarigEndring =
                    tilSpørsmålOgSvar(
                        "Din forklaring om varig endring: ",
                        selvstendigNæringsdrivende.virksomhet?.varigEndring?.forklaring,
                    ),
                flereEnnEnVirksomhet =
                    tilSpørsmålOgSvar(
                        "Har du flere enn én næringsvirksomhet som er aktiv?",
                        selvstendigNæringsdrivende.virksomhet?.harFlereAktiveVirksomheter,
                    ),
                virksomhetNavn = "${selvstendigNæringsdrivende.virksomhet?.navnPåVirksomheten} (startet ${
                    DATE_FORMATTER.format(
                        selvstendigNæringsdrivende.virksomhet?.fraOgMed,
                    )}, ${if (selvstendigNæringsdrivende.virksomhet?.tilOgMed == null) {
                    "er pågående)"
                } else {
                    "Avsluttet ${
                        DATE_FORMATTER.format(
                            selvstendigNæringsdrivende.virksomhet.tilOgMed,
                        )}"
                }} ",
                virksomhetType =
                    tilSpørsmålOgSvar(
                        "Næringstype: ",
                        "${selvstendigNæringsdrivende.virksomhet?.næringstype?.beskrivelse} (${selvstendigNæringsdrivende.virksomhet
                            ?.fiskerErPåBladB
                            ?.let {
                                if (it) "blad B" else "ikke blad B"
                            }
                            ?: ""})",
                    ),
                registrertINorge =
                    selvstendigNæringsdrivende.virksomhet?.registrertINorge?.takeIf { it }?.let {
                        tilSpørsmålOgSvar(
                            "Registrert i Norge: ",
                            "Organisasjonsnummer: ${selvstendigNæringsdrivende.virksomhet.organisasjonsnummer}",
                        )
                    },
                registrertIUtlandet =
                    selvstendigNæringsdrivende.virksomhet?.registrertINorge?.takeIf { !it }?.let {
                        tilSpørsmålOgSvar(
                            "Registrert i: ",
                            "${selvstendigNæringsdrivende.virksomhet.registrertIUtlandet?.landnavn} (${selvstendigNæringsdrivende.virksomhet.registrertIUtlandet?.landkode})",
                        )
                    },
                regnskapsførerNavn =
                    selvstendigNæringsdrivende.virksomhet?.regnskapsfører?.let {
                        tilSpørsmålOgSvar("Regnskapsfører:", it.navn)
                    },
                regnskapsførerTlfnummer =
                    selvstendigNæringsdrivende.virksomhet?.regnskapsfører?.let {
                        tilSpørsmålOgSvar("Tlf. til regnskapsfører:", it.telefon)
                    },
            )
        }
        ?: SelvstendigNæringsdrivendeSpørsmålOgSvar(
            ikkeSelvstendigNæringsdrivende =
                tilSpørsmålOgSvar(
                    "Er du selvstendig næringsdrivende?",
                    "Har ikke vært selvstendig næringsdrivende i perioden det søkes om.",
                ),
        )
