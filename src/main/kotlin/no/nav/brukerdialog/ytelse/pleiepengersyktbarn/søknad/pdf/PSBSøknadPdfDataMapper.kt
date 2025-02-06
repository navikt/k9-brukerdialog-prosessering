package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf
import no.nav.brukerdialog.common.Constants.DATE_FORMATTER
import no.nav.brukerdialog.common.Constants.DATE_TIME_FORMATTER
import no.nav.brukerdialog.common.Constants.OSLO_ZONE_ID
import no.nav.brukerdialog.common.FeltMap
import no.nav.brukerdialog.common.PdfConfig
import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.PSBMottattSøknad
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Arbeidsforhold
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Arbeidsgiver
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.BarnRelasjon
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Beredskap
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Frilans
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Nattevåk
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.OpptjeningIUtlandet
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.SelvstendigNæringsdrivende
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.StønadGodtgjørelse
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.UtenlandsoppholdIPerioden
import no.nav.brukerdialog.utils.DateUtils.somNorskDag
import no.nav.helse.felles.Enkeltdag
import no.nav.helse.felles.Omsorgstilbud
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

object PSBSøknadPdfDataMapper {
    fun mapPSBSøknadPdfData(
        ytelseTittel: String,
        søknad: PSBMottattSøknad,
    ): FeltMap {
        val innsendingsdetaljer =
            mapInnsendingsdetaljer(
                søknad.mottatt
                    .withZoneSameInstant(OSLO_ZONE_ID)
                    .somNorskDag() + DATE_TIME_FORMATTER.format(søknad.mottatt),
            )

        val søker =
            mapSøker(
                søknad.søker.formatertNavn(),
                søknad.søker.fødselsnummer,
                søknad.barn.navn,
                søknad.barn.fødselsnummer.toString(),
            )

        val relasjonTilBarnet = mapRelasjonTilBarnet(søknad.barnRelasjon, søknad.barnRelasjonBeskrivelse)
        val perioder = mapPerioder(søknad.fraOgMed, søknad.tilOgMed)
        val arbeidsgivere = mapArbeidsgivere(søknad.arbeidsgivere, søknad.fraOgMed)
        val stønadGodtgjørelse = mapStønadGodtgjørelse(søknad.stønadGodtgjørelse)
        val frilans = mapFrilans(søknad.frilans)
        val selvstendig = mapSelvstendigNæringsdrivende(søknad.selvstendigNæringsdrivende)
        val jobbISøknadsperioden =
            mapJobbISøknadsperioden(
                søknad.harMinstEtArbeidsforhold(),
                søknad.arbeidsgivere,
                søknad.frilans,
                søknad.selvstendigNæringsdrivende.arbeidsforhold,
            )
        val opptjeningIUtlandet = mapOpptjeningIUtlandet(søknad.opptjeningIUtlandet)
        val verneplikt = mapVerneplikt(søknad.harVærtEllerErVernepliktig)
        val omsorgstilbud = mapOmsorgstilbud(søknad.omsorgstilbud)
        val nattevåk = mapNattevåk(søknad.nattevåk)
        val beredskap = mapBeredskap(søknad.beredskap)
        val utenlandsopphold = mapUtenlandsopphold(søknad.utenlandsoppholdIPerioden)

//        utenlandskNæring,

//        omsorgsstønad,
//        medlemskap,
//        vedlegg,
//        samtykke,

        return FeltMap(
            label = ytelseTittel,
            verdiliste =
                listOf(
                    innsendingsdetaljer,
                    søker,
                ),
            pdfConfig = PdfConfig(true, "nb"),
        )
    }

    fun mapInnsendingsdetaljer(
        tidspunkt: String,
        språk: String? = "nb",
    ): VerdilisteElement =
        VerdilisteElement(
            label = "Innsendingsdetaljer",
            verdiliste =
                listOf(
                    VerdilisteElement(
                        label = "Sendt til Nav",
                        verdi = tidspunkt,
                    ),
                ),
        )

    fun mapSøker(
        navnSøker: String,
        fødselsnummerSøker: String,
        navnBarn: String,
        fødselsnummerBarn: String,
    ) = VerdilisteElement(
        label = "Søker",
        verdiliste =
            listOf(
                VerdilisteElement(label = "Navn", verdi = navnSøker),
                VerdilisteElement(label = "Fødselsnummer", verdi = fødselsnummerSøker),
                VerdilisteElement(label = "Navn på barn", verdi = navnBarn),
                VerdilisteElement(label = "Fødselsnummer på barn", verdi = fødselsnummerBarn),
            ),
    )

    private fun mapRelasjonTilBarnet(
        barnRelasjon: BarnRelasjon?,
        barnRelasjonBeskrivelse: String?,
    ): VerdilisteElement? =
        barnRelasjon?.let {
            VerdilisteElement(
                label = "Relasjon til barnet",
                verdiliste =
                    listOfNotNull(
                        VerdilisteElement(
                            label = "Hvilken relasjon har du til barnet?",
                            verdi = barnRelasjon.toString(),
                        ),
                        barnRelasjonBeskrivelse?.takeIf { it.isNotBlank() }?.let {
                            VerdilisteElement(
                                label = "Din beskrivelse av relasjon og tilsynsrolle for barnet:",
                                verdi = it,
                            )
                        },
                    ),
            )
        }

    private fun mapPerioder(
        fraOgMed: LocalDate,
        tilOgMed: LocalDate,
    ): VerdilisteElement {
        val fraDato = DATE_FORMATTER.format(fraOgMed)
        val tilDato = DATE_FORMATTER.format(tilOgMed)

        return VerdilisteElement(label = "Perioder du søker om pleiepenger", verdi = "$fraDato - $tilDato")
    }

    private fun mapArbeidsgivere(
        arbeidsgivere: List<Arbeidsgiver>,
        fraOgMed: LocalDate,
    ): VerdilisteElement? =
        if (arbeidsgivere.isNotEmpty()) {
            VerdilisteElement(
                label = "Arbeidsgivere",
                verdiliste =
                    arbeidsgivere.map { arbeidsgiver ->
                        VerdilisteElement(
                            label = arbeidsgiver.navn.toString(),
                            verdiliste =
                                listOfNotNull(
                                    VerdilisteElement(label = "Orgnr:", verdi = arbeidsgiver.organisasjonsnummer),
                                    VerdilisteElement(
                                        label = "Jobber du her nå?",
                                        verdi = konverterBooleanTilSvar(arbeidsgiver.erAnsatt),
                                    ),
                                    arbeidsgiver.sluttetFørSøknadsperiode?.takeIf { it }?.let {
                                        VerdilisteElement(
                                            label = "Sluttet du hos ${arbeidsgiver.navn} før $fraOgMed?",
                                            verdi = konverterBooleanTilSvar(arbeidsgiver.sluttetFørSøknadsperiode),
                                        )
                                    },
                                    VerdilisteElement(
                                        label = "Hvor mange timer jobber du normalt per uke?",
                                        verdi =
                                            arbeidsgiver.arbeidsforhold
                                                ?.normalarbeidstid
                                                ?.timerPerUkeISnitt
                                                .toString(),
                                    ),
                                ),
                        )
                    },
            )
        } else {
            null
        }

    fun mapStønadGodtgjørelse(stønadGodtgjørelse: StønadGodtgjørelse?): VerdilisteElement? =
        stønadGodtgjørelse?.takeIf { it.mottarStønadGodtgjørelse == true }?.let {
            VerdilisteElement(
                label = "Omsorgsstønad eller fosterhjemsgodtgjørelse",
                verdiliste =
                    listOf(
                        VerdilisteElement(
                            label = "Mottar du omsorgsstønad eller fosterhjemsgodtgjørelse?",
                            verdi = konverterBooleanTilSvar(stønadGodtgjørelse.mottarStønadGodtgjørelse!!),
                        ),
                        VerdilisteElement(
                            label = "Startet du å motta dette underveis i perioden du søker for?",
                            verdi =
                                stønadGodtgjørelse.startdato?.let { "${konverterBooleanTilSvar(true)} Startet $it" }
                                    ?: konverterBooleanTilSvar(false),
                        ),
                        VerdilisteElement(
                            label = "Slutter du å motta dette underveis i perioden du søker for?",
                            verdi =
                                stønadGodtgjørelse.sluttdato?.let { "${konverterBooleanTilSvar(true)} Sluttet $it" }
                                    ?: konverterBooleanTilSvar(false),
                        ),
                    ),
            )
        }

    fun mapFrilans(frilans: Frilans?): VerdilisteElement? {
        val sisteTreMånederFørSøknadsperiodeStart = DATE_FORMATTER.format(frilans?.startdato?.minusMonths(3))
        return frilans?.takeIf { it.harInntektSomFrilanser }?.let {
            VerdilisteElement(
                label = "Frilans",
                verdiliste =
                    listOfNotNull(
                        VerdilisteElement(
                            label = "Jobber du som frilanser eller mottar du honorarer?",
                            verdi = konverterBooleanTilSvar(frilans.harInntektSomFrilanser),
                        ),
                        VerdilisteElement(
                            label = "Jobber du som frilanser?",
                            verdi = konverterBooleanTilSvar(frilans.jobberFortsattSomFrilans == true),
                        ),
                        if (frilans.startetFørSisteTreHeleMåneder == true) {
                            VerdilisteElement(
                                label = "Startet du som frilanser før $sisteTreMånederFørSøknadsperiodeStart?",
                                verdi = konverterBooleanTilSvar(true),
                            )
                        } else {
                            VerdilisteElement(
                                label = "Når startet du som frilanser?",
                                verdi = DATE_FORMATTER.format(frilans.startdato),
                            )
                        },
                        VerdilisteElement(
                            label = "Jobber du fremdeles som frilanser?",
                            verdi = konverterBooleanTilSvar(frilans.jobberFortsattSomFrilans == true),
                        ),
                        frilans.jobberFortsattSomFrilans.takeIf { it == false }?.let {
                            VerdilisteElement(
                                label = "Når sluttet du som frilanser?",
                                verdi = DATE_FORMATTER.format(frilans.sluttdato),
                            )
                        },
                        VerdilisteElement(
                            label = "Jeg jobber som frilanser og mottar honorar",
                            verdiliste =
                                listOfNotNull(
                                    if (frilans.startetFørSisteTreHeleMåneder == true) {
                                        VerdilisteElement(
                                            label =
                                                "Startet du som frilanser/startet å motta honorar " +
                                                    "før $sisteTreMånederFørSøknadsperiodeStart?",
                                            verdi = konverterBooleanTilSvar(frilans.startetFørSisteTreHeleMåneder),
                                        )
                                    } else {
                                        VerdilisteElement(
                                            label = "Når begynte du å jobbe som frilanser/startet å motta honorar?",
                                            verdi = DATE_FORMATTER.format(frilans.startdato),
                                        )
                                    },
                                    VerdilisteElement(
                                        label = "Jobber du fremdeles som frilanser/mottar honorar?",
                                        verdi = konverterBooleanTilSvar(frilans.jobberFortsattSomFrilans == true),
                                    ),
                                    frilans.jobberFortsattSomFrilans.takeIf { it == false }?.let {
                                        VerdilisteElement(
                                            label = "Når sluttet du som frilanser/sluttet å motta honorar?",
                                            verdi = DATE_FORMATTER.format(frilans.sluttdato),
                                        )
                                    },
                                ),
                        ),
                        VerdilisteElement(
                            label = "Jeg mottar honorar",
                            verdiliste =
                                listOfNotNull(
                                    if (frilans.startetFørSisteTreHeleMåneder == true) {
                                        VerdilisteElement(
                                            label =
                                                "Startet du som frilanser/startet å motta honorar " +
                                                    "før $sisteTreMånederFørSøknadsperiodeStart?",
                                            verdi = konverterBooleanTilSvar(frilans.startetFørSisteTreHeleMåneder),
                                        )
                                    } else {
                                        VerdilisteElement(
                                            label = "Når begynte du å motta honorar?",
                                            verdi = DATE_FORMATTER.format(frilans.startdato),
                                        )
                                    },
                                    VerdilisteElement(
                                        label = "Mottar du fortsatt honorarer?",
                                        verdi = konverterBooleanTilSvar(frilans.jobberFortsattSomFrilans == true),
                                    ),
                                    frilans.jobberFortsattSomFrilans.takeIf { it == false }?.let {
                                        VerdilisteElement(
                                            label = "Når sluttet du å motta honorar?",
                                            verdi = DATE_FORMATTER.format(frilans.sluttdato),
                                        )
                                    },
                                    if (frilans.misterHonorar == true) {
                                        VerdilisteElement(
                                            label = "Jeg mister honorar i søknadsperioden",
                                        )
                                    } else {
                                        VerdilisteElement(
                                            label = "Jeg mister ikke honorar i søknadsperioden",
                                        )
                                    },
                                ),
                        ),
                        VerdilisteElement(
                            label = "Hvor mange timer jobber du normalt per uke?",
                            verdi =
                                frilans.arbeidsforhold
                                    ?.normalarbeidstid
                                    ?.timerPerUkeISnitt
                                    .toString(),
                        ),
                    ),
            )
        } ?: VerdilisteElement(
            label = "Frilans",
            verdi = "Har ikke vært frilanser eller mottatt honorar i perioden det søkes om.",
        )
    }

    fun mapSelvstendigNæringsdrivende(selvstendigNæringsdrivende: SelvstendigNæringsdrivende): VerdilisteElement =
        selvstendigNæringsdrivende.takeIf { it.harInntektSomSelvstendig }?.let {
            VerdilisteElement(
                label = "Selvstendig næringsdrivende",
                verdiliste =
                    listOfNotNull(
                        selvstendigNæringsdrivende.virksomhet?.næringsinntekt.takeIf { it != null }?.let {
                            VerdilisteElement(
                                label = "Næringsinntekt: ${selvstendigNæringsdrivende.virksomhet?.næringsinntekt},-",
                            )
                        },
                        selvstendigNæringsdrivende.virksomhet?.yrkesaktivSisteTreFerdigliknedeÅrene.takeIf { it != null }?.let {
                            VerdilisteElement(
                                label =
                                    "Oppgi dato for når du ble yrkesaktiv:" +
                                        "  ${selvstendigNæringsdrivende.virksomhet?.yrkesaktivSisteTreFerdigliknedeÅrene?.oppstartsdato}",
                            )
                        },
                        selvstendigNæringsdrivende.arbeidsforhold.takeIf { it != null }?.let {
                            VerdilisteElement(
                                label = "Hvor mange timer jobber du normalt per uke?",
                                verdi =
                                    selvstendigNæringsdrivende.arbeidsforhold
                                        ?.normalarbeidstid
                                        ?.timerPerUkeISnitt
                                        .toString(),
                            )
                        },
                        selvstendigNæringsdrivende.virksomhet?.varigEndring.takeIf { it != null }?.let {
                            VerdilisteElement(
                                label = "Varig endring",
                                visningsVariant = "PUNKTLISTE",
                                verdiliste =
                                    listOf(
                                        VerdilisteElement(
                                            label =
                                                "Dato for når varig endring. oppsto: ${selvstendigNæringsdrivende.virksomhet?.varigEndring?.dato}",
                                        ),
                                        VerdilisteElement(
                                            label =
                                                "Næringsinntekt etter varig endring:" +
                                                    " ${selvstendigNæringsdrivende.virksomhet?.varigEndring?.inntektEtterEndring}",
                                        ),
                                        VerdilisteElement(
                                            label = "Din forklaring om varig endring:",
                                            verdi = selvstendigNæringsdrivende.virksomhet?.varigEndring?.forklaring,
                                        ),
                                    ),
                            )
                        },
                        selvstendigNæringsdrivende.virksomhet?.harFlereAktiveVirksomheter?.takeIf { it }.let {
                            VerdilisteElement(
                                label = "Har du flere enn én næringsvirksomhet som er aktiv?",
                                verdi = konverterBooleanTilSvar(selvstendigNæringsdrivende.virksomhet?.harFlereAktiveVirksomheter!!),
                            )
                        },
                        VerdilisteElement(
                            label = "Næringsvirksomhet som du har lagt inn:",
                            verdiliste =
                                listOfNotNull(
                                    selvstendigNæringsdrivende.virksomhet?.let { it1 ->
                                        VerdilisteElement(
                                            label = "${it1.navnPåVirksomheten} startet ${it1.fraOgMed}",
                                            verdiliste =
                                                listOfNotNull(
                                                    if (it1.tilOgMed != null) {
                                                        VerdilisteElement(
                                                            label = "Avsluttet ${it1.tilOgMed}",
                                                        )
                                                    } else {
                                                        VerdilisteElement(
                                                            label = "Er pågående",
                                                        )
                                                    },
                                                ),
                                        )
                                    },
                                    VerdilisteElement(
                                        label = "Næringstype: ${selvstendigNæringsdrivende.virksomhet?.næringstype?.beskrivelse}",
                                        verdiliste =
                                            listOfNotNull(
                                                selvstendigNæringsdrivende.virksomhet?.næringstype?.let { it1 ->
                                                    VerdilisteElement(
                                                        label = it1.beskrivelse,
                                                        verdi =
                                                            if (selvstendigNæringsdrivende.virksomhet.fiskerErPåBladB == true
                                                            ) {
                                                                "Blad B"
                                                            } else {
                                                                "Ikke blad B"
                                                            },
                                                    )
                                                },
                                            ),
                                    ),
                                    if (selvstendigNæringsdrivende.virksomhet?.registrertINorge == true) {
                                        VerdilisteElement(
                                            label = "Registrert i Norge",
                                            verdi = "Organisasjonsnummer: ${selvstendigNæringsdrivende.virksomhet.organisasjonsnummer}",
                                        )
                                    } else {
                                        VerdilisteElement(
                                            label = "Registrert i land: ${selvstendigNæringsdrivende.virksomhet?.organisasjonsnummer}",
                                            verdi = selvstendigNæringsdrivende.virksomhet?.registrertIUtlandet?.landkode,
                                        )
                                    },
                                    selvstendigNæringsdrivende.virksomhet?.regnskapsfører.takeIf { it != null }.let {
                                        VerdilisteElement(
                                            label = "Regnskapsfører",
                                            visningsVariant = "PUNKTLISTE",
                                            verdiliste =
                                                listOf(
                                                    VerdilisteElement(
                                                        label = "Navn: ${it?.navn}",
                                                    ),
                                                    VerdilisteElement(
                                                        label = "Telefonnummer: ${it?.telefon}",
                                                    ),
                                                ),
                                        )
                                    },
                                ),
                        ),
                    ),
            )
        } ?: VerdilisteElement(
            label = "Selvstendig næringsdrivende",
            verdi = "Har ikke vært selvstendig næringsdrivende i perioden det søkes om.",
        )

    fun mapJobbISøknadsperioden(
        ingenArbeidsforhold: Boolean,
        arbeidsgivere: List<Arbeidsgiver>,
        frilans: Frilans?,
        selvstendigNæringsdrivendeArbeidsforhold: Arbeidsforhold?,
    ): VerdilisteElement =
        VerdilisteElement(
            label = "Jobb i søknadsperioden",
            verdiliste =
                listOfNotNull(
                    ingenArbeidsforhold.takeIf { it }?.let {
                        VerdilisteElement(
                            label = "Ingen arbeidsforhold er registrert i søknadsperioden",
                        )
                    },
                    arbeidsgivere.takeIf { it.isNotEmpty() }.let {
                        VerdilisteElement(
                            label = "Arbeidsgivere",
                            verdiliste =
                                arbeidsgivere.map { arbeidsgiver ->
                                    arbeidsgiver.arbeidsforhold.takeIf { it != null }.let {
                                        VerdilisteElement(
                                            label = "${arbeidsgiver.navn} Orgnr: ${arbeidsgiver.organisasjonsnummer}",
                                            verdi =
                                                "Hvor mange timer jobber du normalt per uke? " +
                                                    "${arbeidsgiver.arbeidsforhold?.normalarbeidstid?.timerPerUkeISnitt}",
                                        )
                                    }
                                },
                        )
                    },
                    frilans?.harInntektSomFrilanser.takeIf { it == true }.let {
                        VerdilisteElement(
                            label = "Frilans",
                            verdiliste =
                                listOfNotNull(
                                    frilans?.arbeidsforhold.takeIf { it != null }.let {
                                        VerdilisteElement(
                                            label = "Hvor mange timer jobber du normalt per uke?",
                                            verdi =
                                                frilans
                                                    ?.arbeidsforhold
                                                    ?.normalarbeidstid
                                                    ?.timerPerUkeISnitt
                                                    .toString(),
                                        )
                                    },
                                ),
                        )
                    },
                    selvstendigNæringsdrivendeArbeidsforhold?.let {
                        VerdilisteElement(
                            label = "Selvstendig næringsdrivende",
                            verdiliste =
                                listOfNotNull(
                                    VerdilisteElement(
                                        label = "Hvor mange timer jobber du normalt per uke?",
                                        verdi =
                                            selvstendigNæringsdrivendeArbeidsforhold
                                                .normalarbeidstid
                                                .timerPerUkeISnitt
                                                .toString(),
                                    ),
                                ),
                        )
                    },
                ),
        )

    fun mapOpptjeningIUtlandet(opptjeningUtland: List<OpptjeningIUtlandet>): VerdilisteElement =
        VerdilisteElement(
            label = "Jobbet i annet EØS-land",
            visningsVariant = "PUNKTLISTE",
            verdiliste =
                if (opptjeningUtland.isNotEmpty()) {
                    opptjeningUtland.map { opptjent ->
                        VerdilisteElement(
                            label =
                                "Jobbet i ${opptjent.land.landnavn} som ${opptjent.opptjeningType.pdfTekst} " +
                                    "hos ${opptjent.navn}  ${DATE_TIME_FORMATTER.format(
                                        opptjent.fraOgMed,
                                    )} - ${DATE_TIME_FORMATTER.format(opptjent.tilOgMed)}",
                        )
                    }
                } else {
                    listOf(
                        VerdilisteElement(
                            label = "Nei",
                        ),
                    )
                },
        )

    fun mapVerneplikt(verneplikt: Boolean?): VerdilisteElement? =
        verneplikt?.let {
            VerdilisteElement(
                label = "Verneplikt",
                verdiliste =
                    listOf(
                        VerdilisteElement(
                            label = "Utøvde du verneplikt på tidspunktet du søker pleiepenger fra?",
                            verdi = konverterBooleanTilSvar(verneplikt),
                        ),
                    ),
            )
        }

    fun mapOmsorgstilbud(omsorgstilbud: Omsorgstilbud?): VerdilisteElement =
        VerdilisteElement(
            label = "Omsorgtilbud",
            verdiliste =
                if (omsorgstilbud != null) {
                    listOf(
                        omsorgstilbud.svarFortid.takeIf { it != null }.let {
                            VerdilisteElement(
                                label = "Har barnet vært fast og regelmessig i et omsorgstilbud?",
                                verdi = omsorgstilbud.svarFortid.toString(),
                            )
                        },
                        omsorgstilbud.svarFremtid.takeIf { it != null }.let {
                            VerdilisteElement(
                                label = "Skal barnet være fast og regelmessig i et omsorgstilbud?",
                                verdi = omsorgstilbud.svarFremtid.toString(),
                            )
                        },
                        omsorgstilbud.erLiktHverUke.takeIf { it == true }.let {
                            VerdilisteElement(
                                label = "Er tiden i omsorgstilbudet lik hver uke?",
                                verdi = omsorgstilbud.svarFremtid.toString(),
                            )
                        },
                        omsorgstilbud.enkeltdager.takeIf { it != null }.let {
                            VerdilisteElement(
                                label = "Tid barnet er i omsorgstilbud:",
                                verdiliste =
                                    mapEnkeltdagToVerdilisteElement(omsorgstilbud.enkeltdager!!),
                            )
                        },
                        omsorgstilbud.ukedager.takeIf { it != null }.let { ukedag ->
                            VerdilisteElement(
                                label = "Faste dager barnet er i omsorgtilbud: ",
                                verdiliste =
                                    listOfNotNull(
                                        ukedag?.mandag.takeIf { it != null }.let {
                                            VerdilisteElement(
                                                label = "Mandager: ",
                                                verdi = omsorgstilbud.ukedager?.mandag.toString(),
                                            )
                                        },
                                        ukedag?.tirsdag.takeIf { it != null }.let {
                                            VerdilisteElement(
                                                label = "Tirsdager: ",
                                                verdi = omsorgstilbud.ukedager?.tirsdag.toString(),
                                            )
                                        },
                                        ukedag?.onsdag.takeIf { it != null }.let {
                                            VerdilisteElement(
                                                label = "Onsdager: ",
                                                verdi = omsorgstilbud.ukedager?.onsdag.toString(),
                                            )
                                        },
                                        ukedag?.torsdag.takeIf { it != null }.let {
                                            VerdilisteElement(
                                                label = "Torsdager: ",
                                                verdi = omsorgstilbud.ukedager?.torsdag.toString(),
                                            )
                                        },
                                        ukedag?.fredag.takeIf { it != null }.let {
                                            VerdilisteElement(
                                                label = "Fredager: ",
                                                verdi = omsorgstilbud.ukedager?.fredag.toString(),
                                            )
                                        },
                                    ),
                            )
                        },
                    )
                } else {
                    listOf(
                        VerdilisteElement(
                            label = "Nei",
                        ),
                    )
                },
        )

    fun mapNattevåk(nattevåk: Nattevåk?): VerdilisteElement =
        nattevåk.takeIf { it != null }.let {
            VerdilisteElement(
                label = "Nattevåk",
                verdiliste =
                    listOf(
                        VerdilisteElement(
                            label = "Må du være våken om natten for å pleie barnet, og derfor må være borte fra jobb dagen etter?",
                            verdi = konverterBooleanTilSvar(nattevåk?.harNattevåk!!),
                        ),
                        nattevåk.tilleggsinformasjon.takeIf { it != null }.let {
                            VerdilisteElement(
                                label = "Dine tilleggsopplysninger:",
                                verdi = nattevåk.tilleggsinformasjon,
                            )
                        },
                    ),
            )
        }

    fun mapBeredskap(beredskap: Beredskap?): VerdilisteElement? =
        beredskap?.let {
            VerdilisteElement(
                label = "Beredskap",
                verdiliste =
                    listOfNotNull(
                        VerdilisteElement(
                            label = "Må du være i beredskap også når barnet er i et omsorgstilbud?",
                            verdi = konverterBooleanTilSvar(beredskap.beredskap),
                        ),
                        beredskap.tilleggsinformasjon?.let {
                            VerdilisteElement(
                                label = "Dine tilleggsopplysninger:",
                                verdi = beredskap.tilleggsinformasjon,
                            )
                        },
                    ),
            )
        }

    fun mapUtenlandsopphold(utenlandsopphold: UtenlandsoppholdIPerioden?): VerdilisteElement? =
        utenlandsopphold?.let {
            VerdilisteElement(
                label = "Perioder med utenlandsopphold og ferie",
                verdiliste =
                    listOfNotNull(
                        VerdilisteElement(
                            label = "Skal du reise til utlandet i perioden du søker om pleiepenger?",
                            verdi = konverterBooleanTilSvar(utenlandsopphold.skalOppholdeSegIUtlandetIPerioden),
                            verdiliste =
                                utenlandsopphold.skalOppholdeSegIUtlandetIPerioden.let {
                                    utenlandsopphold.opphold.mapNotNull { opphold ->
                                        VerdilisteElement(
                                            label = opphold.landnavn + if (opphold.erUtenforEøs == true) " utenfor EØS" else "",
                                        )
                                        opphold.erSammenMedBarnet?.let {
                                            VerdilisteElement(
                                                label = "Er barnet sammen med deg?",
                                                verdi = konverterBooleanTilSvar(opphold.erSammenMedBarnet),
                                            )
                                        }
                                        opphold.erUtenforEøs?.let {
                                            opphold.erBarnetInnlagt?.let {
                                                VerdilisteElement(
                                                    label = "Er barnet innlagt?",
                                                    verdi = konverterBooleanTilSvar(opphold.erBarnetInnlagt),
                                                    verdiliste =
                                                        opphold.perioderBarnetErInnlagt.map {
                                                            VerdilisteElement(
                                                                label = "Perioder:",
                                                                verdi = "${it.fraOgMed} - ${it.tilOgMed}",
                                                            )
                                                        },
                                                )
                                                opphold.årsak?.let {
                                                    VerdilisteElement(label = "Årsak:", verdi = it.beskrivelse)
                                                }
                                            }
                                        }
                                    }
                                },
                        ),
                    ),
            )
        }

    fun konverterBooleanTilSvar(svar: Boolean) =
        if (svar) {
            "Ja"
        } else {
            "Nei"
        }
}

private fun PSBMottattSøknad.harMinstEtArbeidsforhold(): Boolean {
    if (frilans.arbeidsforhold != null) return true

    if (selvstendigNæringsdrivende.arbeidsforhold != null) return true

    if (arbeidsgivere.any { it.arbeidsforhold != null }) return true

    return false
}

fun mapEnkeltdagToVerdilisteElement(enkeltdager: List<Enkeltdag>): List<VerdilisteElement> {
    val weekFields = WeekFields.of(Locale.getDefault())

    return enkeltdager
        .groupBy { it.dato.monthValue }
        .flatMap { (month, daysInMonth) ->
            daysInMonth
                .groupBy { it.dato.get(weekFields.weekOfMonth()) }
                .map { (week, daysInWeek) ->
                    VerdilisteElement(
                        label = "Month: $month, Week: $week",
                        verdiliste =
                            daysInWeek.map { day ->
                                VerdilisteElement(
                                    label = "Day: ${day.dato}",
                                    verdi = day.tid.toString(),
                                )
                            },
                    )
                }
        }
}
