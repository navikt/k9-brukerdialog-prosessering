package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf
import no.nav.brukerdialog.common.Constants.DATE_FORMATTER
import no.nav.brukerdialog.common.Constants.DATE_TIME_FORMATTER
import no.nav.brukerdialog.common.Constants.OSLO_ZONE_ID
import no.nav.brukerdialog.common.FeltMap
import no.nav.brukerdialog.common.PdfConfig
import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.PSBMottattSøknad
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.ArbeidIPeriodeType
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Arbeidsforhold
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Arbeidsgiver
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Barn
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.BarnRelasjon
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Beredskap
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.FerieuttakIPerioden
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Frilans
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Medlemskap
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Nattevåk
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.OpptjeningIUtlandet
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.RedusertArbeidstidType
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.SelvstendigNæringsdrivende
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.StønadGodtgjørelse
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.UtenlandskNæring
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.UtenlandsoppholdIPerioden
import no.nav.brukerdialog.utils.DateUtils.somNorskDag
import no.nav.brukerdialog.utils.DurationUtils.somTekst
import no.nav.brukerdialog.utils.DurationUtils.tilString
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.helse.felles.Enkeltdag
import no.nav.helse.felles.Omsorgstilbud
import java.time.Duration
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
                søknad.søker,
                søknad.barn,
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
        val utenlandskNæring = maputenlandskNæring(søknad.utenlandskNæring)
        val omsorgstilbud = mapOmsorgstilbud(søknad.omsorgstilbud)
        val nattevåk = mapNattevåk(søknad.nattevåk)
        val beredskap = mapBeredskap(søknad.beredskap)
        val utenlandsopphold = mapUtenlandsopphold(søknad.utenlandsoppholdIPerioden, søknad.ferieuttakIPerioden)
        val medlemskap = mapMedlemskap(søknad.medlemskap)
        val vedlegg = mapVedlegg(søknad.vedleggId, søknad.barn, søknad.fødselsattestVedleggId)
        val samtykke = mapSamtykke(søknad.harForståttRettigheterOgPlikter, søknad.harBekreftetOpplysninger)

        return FeltMap(
            label = ytelseTittel,
            verdiliste =
                listOfNotNull(
                    innsendingsdetaljer,
                    søker,
                    relasjonTilBarnet,
                    perioder,
                    arbeidsgivere,
                    stønadGodtgjørelse,
                    frilans,
                    selvstendig,
                    jobbISøknadsperioden,
                    opptjeningIUtlandet,
                    utenlandskNæring,
                    verneplikt,
                    omsorgstilbud,
                    nattevåk,
                    beredskap,
                    utenlandsopphold,
                    medlemskap,
                    vedlegg,
                    samtykke,
                ),
            pdfConfig = PdfConfig(true, "nb"),
        )
    }

    // TODO FELLES-FUN
    fun lagVerdiElement(
        spørsmålsTekst: String,
        svarVerdi: Any?,
        typeSomSkalSjekkes: Any? = svarVerdi,
    ): VerdilisteElement? =
        if (typeSomSkalSjekkes == null) {
            null
        } else {
            when (svarVerdi) {
                is String -> VerdilisteElement(label = spørsmålsTekst, verdi = svarVerdi)
                is Enum<*> -> VerdilisteElement(label = spørsmålsTekst, verdi = svarVerdi.toString())
                is Boolean -> VerdilisteElement(label = spørsmålsTekst, verdi = konverterBooleanTilSvar(svarVerdi))
                is Duration -> VerdilisteElement(label = spørsmålsTekst, verdi = svarVerdi.tilString())
                is LocalDate -> VerdilisteElement(label = spørsmålsTekst, verdi = DATE_FORMATTER.format(svarVerdi))
                is Int -> VerdilisteElement(label = spørsmålsTekst, verdi = svarVerdi.toString())
                is Arbeidsforhold -> VerdilisteElement(label = spørsmålsTekst, verdi = arbeidIPerioden(svarVerdi))
                else -> null
            }
        }

    // Sende inn et tredje param som er optional som er det man sjekker på, også kan verdien være noe annet. Defaulter til å være det samme med mindre den er med

    // TODO FELLES-FUN
    private fun arbeidIPerioden(arbeidsforhold: Arbeidsforhold?): String? {
        val arbeidIPeriode = arbeidsforhold?.arbeidIPeriode ?: return "Ingen arbeidsforhold registrert"

        return when (arbeidIPeriode.type) {
            ArbeidIPeriodeType.ARBEIDER_IKKE -> "Jobber ikke i perioden."
            ArbeidIPeriodeType.ARBEIDER_VANLIG -> "Jobber som normalt i perioden. Har ikke fravær fra jobb"
            ArbeidIPeriodeType.ARBEIDER_REDUSERT -> {
                val redusertArbeid = arbeidIPeriode.redusertArbeid ?: return "Jobber kombinerer pleiepeneger med arbeid i perioden."

                val redusertInfo =
                    when (redusertArbeid.type) {
                        RedusertArbeidstidType.PROSENT_AV_NORMALT -> "Jobber ${redusertArbeid.prosentAvNormalt} av normalt i perioden"
                        RedusertArbeidstidType.TIMER_I_SNITT_PER_UKE -> "Jobber ${redusertArbeid.timerPerUke?.toHours()} timer per uke"
                        RedusertArbeidstidType.ULIKE_UKER_TIMER ->
                            redusertArbeid.arbeidsuker?.joinToString(
                                "\n",
                            ) { uke -> "Periode: ${uke.periode}, Timer: ${uke.timer?.toHours()} timer" }
                                ?: "Ingen detaljer for ulike uker"
                    }
                "Jobber kombinerer pleiepenger med arbeid i perioden.\n$redusertInfo"
            }
        }
    }

    private fun mapInnsendingsdetaljer(tidspunkt: String): VerdilisteElement =
        VerdilisteElement(
            label = "Innsendingsdetaljer",
            verdiliste =
                listOfNotNull(
                    lagVerdiElement("Send til Nav", tidspunkt),
                ),
        )

    private fun mapSøker(
        søker: Søker,
        barn: Barn,
    ) = VerdilisteElement(
        label = "Søker",
        verdiliste =
            listOfNotNull(
                lagVerdiElement("Navn", søker.formatertNavn()),
                lagVerdiElement("Fødselsnummer", søker.fødselsnummer),
                lagVerdiElement("Navn på barn", barn.navn),
                lagVerdiElement("Fødselsnummer på barn", barn.fødselsnummer),
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
                        lagVerdiElement("Hvilken relasjon har du til barnet?", barnRelasjon),
                        lagVerdiElement("Din beskrivelse av relasjon og tilsynsrolle for barnet:", barnRelasjonBeskrivelse),
                    ),
            )
        }

    private fun mapPerioder(
        fraOgMed: LocalDate,
        tilOgMed: LocalDate,
    ): VerdilisteElement {
        val fraDato = DATE_FORMATTER.format(fraOgMed)
        val tilDato = DATE_FORMATTER.format(tilOgMed)

        return VerdilisteElement(
            label = "Perioder du søker om pleiepenger",
            verdiliste = (
                listOfNotNull(
                    lagVerdiElement(
                        "Hvilke periode har du søkt om pleiepenger?",
                        "$fraDato - $tilDato",
                    ),
                )
            ),
        )
    }

    private fun mapArbeidsgivere(
        arbeidsgivere: List<Arbeidsgiver>,
        fraOgMed: LocalDate,
    ): VerdilisteElement =
        arbeidsgivere.takeIf { it.isNotEmpty() }?.let {
            VerdilisteElement(
                label = "Arbeidsgivere",
                verdiliste =
                    arbeidsgivere.map { arbeidsgiver ->
                        VerdilisteElement(
                            label = arbeidsgiver.navn.toString(),
                            verdiliste =
                                listOfNotNull(
                                    lagVerdiElement("Orgnr:", arbeidsgiver.organisasjonsnummer),
                                    lagVerdiElement("Jobber du her nå?", arbeidsgiver.erAnsatt),
                                    lagVerdiElement(
                                        "Sluttet du hos ${arbeidsgiver.navn} før $fraOgMed?",
                                        arbeidsgiver.sluttetFørSøknadsperiode,
                                    ),
                                    lagVerdiElement(
                                        "Hvor mange timer jobber du normalt per uke?",
                                        arbeidsgiver.arbeidsforhold?.normalarbeidstid?.timerPerUkeISnitt,
                                    ),
                                ),
                        )
                    },
            )
        } ?: VerdilisteElement(
            label = "Arbeidsgivere",
            verdiliste = listOfNotNull(lagVerdiElement("", "Ingen arbeidsforhold registrert i AA-registeret.")),
        )

    private fun mapStønadGodtgjørelse(stønadGodtgjørelse: StønadGodtgjørelse?): VerdilisteElement? =
        stønadGodtgjørelse?.let {
            VerdilisteElement(
                label = "Omsorgsstønad eller fosterhjemsgodtgjørelse",
                verdiliste =
                    listOfNotNull(
                        stønadGodtgjørelse.mottarStønadGodtgjørelse?.let {
                            lagVerdiElement(
                                "Mottar du omsorgsstønad eller fosterhjemsgodtgjørelse?",
                                stønadGodtgjørelse.mottarStønadGodtgjørelse,
                            )
                        },
                        lagVerdiElement(
                            "Startet du å motta dette underveis i perioden du søker for?",
                            stønadGodtgjørelse.startdato?.let { "${konverterBooleanTilSvar(true)} Startet $it" }
                                ?: konverterBooleanTilSvar(false),
                        ),
                        lagVerdiElement(
                            "Slutter du å motta dette underveis i perioden du søker for?",
                            stønadGodtgjørelse.sluttdato?.let { "${konverterBooleanTilSvar(true)} Sluttet $it" }
                                ?: konverterBooleanTilSvar(false),
                        ),
                    ),
            )
        }

    // TODO logikken i denne må sees over, mye == true inne i konverter-funksjoner
    private fun mapFrilans(frilans: Frilans?): VerdilisteElement? {
        val sisteTreMånederFørSøknadsperiodeStart = DATE_FORMATTER.format(frilans?.startdato?.minusMonths(3))
        return frilans?.takeIf { it.harInntektSomFrilanser }?.let {
            VerdilisteElement(
                label = "Frilans",
                verdiliste =
                    listOfNotNull(
                        lagVerdiElement("Jobber du som frilanser eller mottar du honorarer?", frilans.harInntektSomFrilanser),
                        lagVerdiElement("Jobber du som frilanser?", frilans.jobberFortsattSomFrilans == true),
                        if (frilans.startetFørSisteTreHeleMåneder == true) {
                            lagVerdiElement(
                                "Startet du som frilanser før $sisteTreMånederFørSøknadsperiodeStart?",
                                frilans.startetFørSisteTreHeleMåneder,
                            )
                        } else {
                            lagVerdiElement("Når startet du som frilanser?", frilans.startdato)
                        },
                        lagVerdiElement("Jobber du fremdeles som frilanser?", frilans.jobberFortsattSomFrilans),
                        frilans.jobberFortsattSomFrilans.takeIf { it == false }?.let {
                            lagVerdiElement(
                                "Når sluttet du som frilanser?",
                                frilans.sluttdato,
                            )
                        },
                        VerdilisteElement(
                            label = "Jeg jobber som frilanser og mottar honorar",
                            verdiliste =
                                listOfNotNull(
                                    if (frilans.startetFørSisteTreHeleMåneder == true) {
                                        lagVerdiElement(
                                            "Startet du som frilanser/startet å motta honorar før $sisteTreMånederFørSøknadsperiodeStart?",
                                            frilans.startetFørSisteTreHeleMåneder,
                                        )
                                    } else {
                                        lagVerdiElement("Når begynte du å jobbe som frilanser/startet å motta honorar?", frilans.startdato)
                                    },
                                    lagVerdiElement("Jobber du fremdeles som frilanser/mottar honorar?", frilans.jobberFortsattSomFrilans),
                                    frilans.jobberFortsattSomFrilans.takeIf { it == false }?.let {
                                        lagVerdiElement("Når sluttet du som frilanser/sluttet å motta honorar?", frilans.sluttdato)
                                    },
                                ),
                        ),
                        VerdilisteElement(
                            label = "Jeg mottar honorar",
                            verdiliste =
                                listOfNotNull(
                                    if (frilans.startetFørSisteTreHeleMåneder == true) {
                                        lagVerdiElement(
                                            "Startet du som frilanser/startet å motta honorar før $sisteTreMånederFørSøknadsperiodeStart?",
                                            frilans.startetFørSisteTreHeleMåneder,
                                        )
                                    } else {
                                        lagVerdiElement("Når begynte du å motta honorar?", frilans.startdato)
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
                        lagVerdiElement(
                            "Hvor mange timer jobber du normalt per uke?",
                            frilans.arbeidsforhold?.normalarbeidstid?.timerPerUkeISnitt,
                        ),
                    ),
            )
        } ?: VerdilisteElement(
            label = "Frilans",
            verdi = "Har ikke vært frilanser eller mottatt honorar i perioden det søkes om.",
        )
    }

    private fun mapSelvstendigNæringsdrivende(selvstendigNæringsdrivende: SelvstendigNæringsdrivende): VerdilisteElement =
        selvstendigNæringsdrivende.takeIf { it.harInntektSomSelvstendig }?.let {
            VerdilisteElement(
                label = "Selvstendig næringsdrivende",
                verdiliste =
                    listOfNotNull(
                        lagVerdiElement(
                            "Næringsinntekt:",
                            selvstendigNæringsdrivende.virksomhet?.næringsinntekt,
                        ),
                        lagVerdiElement(
                            "Oppgi dato for når du ble yrkesaktiv: ",
                            selvstendigNæringsdrivende.virksomhet?.yrkesaktivSisteTreFerdigliknedeÅrene?.oppstartsdato,
                        ),
                        lagVerdiElement(
                            "Hvor mange timer jobber du normalt per uke?",
                            selvstendigNæringsdrivende.arbeidsforhold?.normalarbeidstid?.timerPerUkeISnitt,
                        ),
                        selvstendigNæringsdrivende.virksomhet?.varigEndring.takeIf { it != null }?.let {
                            VerdilisteElement(
                                label = "Varig endring",
                                verdiliste =
                                    listOfNotNull(
                                        lagVerdiElement(
                                            "Dato for når varig endring. oppsto:",
                                            selvstendigNæringsdrivende.virksomhet?.varigEndring?.dato,
                                        ),
                                        lagVerdiElement(
                                            "Næringsinntekt etter varig endring:",
                                            selvstendigNæringsdrivende.virksomhet?.varigEndring?.inntektEtterEndring,
                                        ),
                                        lagVerdiElement(
                                            "Din forklaring om varig endring:",
                                            selvstendigNæringsdrivende.virksomhet?.varigEndring?.forklaring,
                                        ),
                                    ),
                            )
                        },
                        lagVerdiElement(
                            "Har du flere enn én næringsvirksomhet som er aktiv?",
                            selvstendigNæringsdrivende.virksomhet?.harFlereAktiveVirksomheter,
                        ),
                        VerdilisteElement(
                            label = "Næringsvirksomhet som du har lagt inn:",
                            verdiliste =
                                listOfNotNull(
                                    selvstendigNæringsdrivende.virksomhet?.let { virksomhet ->
                                        VerdilisteElement(
                                            label =
                                                "${virksomhet.navnPåVirksomheten} (startet ${virksomhet.fraOgMed}, " +
                                                    "${if (virksomhet.tilOgMed == null) "er pågående)" else "Avsluttet ${virksomhet.tilOgMed})"},",
                                            verdiliste =
                                                listOfNotNull(
                                                    lagVerdiElement(
                                                        "Næringstype: ",
                                                        "${selvstendigNæringsdrivende.virksomhet.næringstype.beskrivelse} (${selvstendigNæringsdrivende.virksomhet.fiskerErPåBladB})",
                                                    ),
                                                    if (selvstendigNæringsdrivende.virksomhet.registrertINorge) {
                                                        lagVerdiElement(
                                                            "Registrert i Norge:",
                                                            "Organisasjonsnummer: ${selvstendigNæringsdrivende.virksomhet.organisasjonsnummer}",
                                                        )
                                                    } else {
                                                        lagVerdiElement(
                                                            "Registrert i land: ",
                                                            "${selvstendigNæringsdrivende.virksomhet.registrertIUtlandet?.landnavn} (${selvstendigNæringsdrivende.virksomhet.registrertIUtlandet?.landkode})",
                                                        )
                                                    },
                                                    selvstendigNæringsdrivende.virksomhet.regnskapsfører?.let {
                                                        VerdilisteElement(
                                                            label = "Regnskapsfører: ",
                                                            visningsVariant = "PUNKTLISTE",
                                                            verdiliste =
                                                                listOfNotNull(
                                                                    lagVerdiElement("Navn", it.navn),
                                                                    lagVerdiElement("Telefon: ", it.telefon),
                                                                ),
                                                        )
                                                    },
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

    private fun mapJobbISøknadsperioden(
        ingenArbeidsforhold: Boolean,
        arbeidsgivere: List<Arbeidsgiver>,
        frilans: Frilans?,
        selvstendigNæringsdrivendeArbeidsforhold: Arbeidsforhold?,
    ): VerdilisteElement =
        VerdilisteElement(
            label = "Jobb i søknadsperioden",
            verdiliste =
                listOfNotNull(
                    lagVerdiElement(
                        "Arbeidsgivere",
                        "${if (ingenArbeidsforhold) "Ingen arbeidsforhold er registrert i søknadsperioden" else null} ",
                    ),
                    arbeidsgivere.takeIf { it.isNotEmpty() }.let {
                        VerdilisteElement(
                            label = "Arbeidsgivere",
                            verdiliste =
                                arbeidsgivere.filter { it.erAnsatt }.map { arbeidsgiver ->
                                    VerdilisteElement(
                                        label = "${arbeidsgiver.navn} (orgnr: ${arbeidsgiver.organisasjonsnummer})",
                                        verdiliste =
                                            listOfNotNull(
                                                lagVerdiElement("Hvor mye jobber du normalt per uke? ", arbeidsgiver.arbeidsforhold),
                                            ),
                                    )
                                },
                        )
                    },
                    frilans?.harInntektSomFrilanser.takeIf { it == true }?.let {
                        VerdilisteElement(
                            label = "Frilans",
                            verdiliste =
                                listOfNotNull(
                                    lagVerdiElement("Hvor mye jobber du normalt per uke?", frilans?.arbeidsforhold),
                                ),
                        )
                    },
                    selvstendigNæringsdrivendeArbeidsforhold?.let {
                        VerdilisteElement(
                            label = "Selvstendig næringsdrivende",
                            verdiliste =
                                listOfNotNull(
                                    lagVerdiElement("Hvor mye jobber du normalt per uke?", selvstendigNæringsdrivendeArbeidsforhold),
                                ),
                        )
                    },
                ),
        )

    private fun mapOpptjeningIUtlandet(opptjeningUtland: List<OpptjeningIUtlandet>): VerdilisteElement =
        VerdilisteElement(
            label = "Jobbet i annet EØS-land",
            visningsVariant = "PUNKTLISTE",
            verdiliste =
                if (opptjeningUtland.isNotEmpty()) {
                    opptjeningUtland.flatMap { opptjent ->
                        listOfNotNull(
                            lagVerdiElement(
                                "Annet EØS-land",
                                "Jobbet i ${opptjent.land.landnavn} som ${opptjent.opptjeningType.pdfTekst} hos " +
                                    "${opptjent.navn}  ${DATE_FORMATTER.format(opptjent.fraOgMed)} " +
                                    "- ${DATE_FORMATTER.format(opptjent.tilOgMed)}",
                            ),
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

    private fun maputenlandskNæring(utenlandskNæring: List<UtenlandskNæring>): VerdilisteElement? =
        utenlandskNæring.takeIf { it.isNotEmpty() }?.let {
            VerdilisteElement(
                label = "Utenlandsk næring",
                verdiliste =
                    utenlandskNæring.flatMap { næring ->
                        listOfNotNull(
                            lagVerdiElement(næring.navnPåVirksomheten, "${næring.fraOgMed} - ${næring.tilOgMed ?: ""}"),
                            lagVerdiElement("Land:", "${næring.land.landnavn} ${næring.land.landkode}"),
                            lagVerdiElement("Organisasjonsnummer:", næring.organisasjonsnummer),
                            lagVerdiElement("Næringstype:", næring.næringstype.beskrivelse),
                        )
                    },
            )
        }

    // TODO mapnotnull ødelegger

    private fun mapVerneplikt(verneplikt: Boolean?): VerdilisteElement? =
        verneplikt?.let {
            VerdilisteElement(
                label = "Verneplikt",
                verdiliste =
                    listOfNotNull(
                        lagVerdiElement("Utøvde du verneplikt på tidspunktet du søker pleiepenger fra?", verneplikt),
                    ),
            )
        }

    private fun mapOmsorgstilbud(omsorgstilbud: Omsorgstilbud?): VerdilisteElement =
        VerdilisteElement(
            label = "Omsorgtilbud",
            verdiliste =
                if (omsorgstilbud != null) {
                    listOfNotNull(
                        lagVerdiElement("Har barnet vært fast og regelmessig i et omsorgstilbud?", omsorgstilbud.svarFortid),
                        lagVerdiElement("Skal barnet være fast og regelmessig i et omsorgstilbud?", omsorgstilbud.svarFremtid),
                        lagVerdiElement("Er tiden i omsorgstilbudet lik hver uke?", omsorgstilbud.svarFremtid),
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
                                        lagVerdiElement("Mandager: ", omsorgstilbud.ukedager?.mandag?.somTekst(true), ukedag?.mandag),
                                        lagVerdiElement("Tirsdager: ", omsorgstilbud.ukedager?.tirsdag?.somTekst(true), ukedag?.tirsdag),
                                        lagVerdiElement("Onsdager: ", omsorgstilbud.ukedager?.onsdag?.somTekst(true), ukedag?.onsdag),
                                        lagVerdiElement("Torsdager: ", omsorgstilbud.ukedager?.torsdag?.somTekst(true), ukedag?.torsdag),
                                        lagVerdiElement("Fredager: ", omsorgstilbud.ukedager?.fredag?.somTekst(true), ukedag?.fredag),
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

    private fun mapNattevåk(nattevåk: Nattevåk?): VerdilisteElement =
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

    private fun mapBeredskap(beredskap: Beredskap?): VerdilisteElement? =
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

    private fun mapUtenlandsopphold(
        utenlandsopphold: UtenlandsoppholdIPerioden?,
        ferieuttakIPerioden: FerieuttakIPerioden?,
    ): VerdilisteElement? =
        utenlandsopphold?.let {
            VerdilisteElement(
                label = "Perioder med utenlandsopphold og ferie",
                verdiliste =
                    listOfNotNull(
                        lagVerdiElement(
                            "Skal du reise til utlandet i perioden du søker om pleiepenger",
                            utenlandsopphold.skalOppholdeSegIUtlandetIPerioden,
                        ),
                        utenlandsopphold.takeIf { it.skalOppholdeSegIUtlandetIPerioden }?.let {
                            VerdilisteElement(
                                label = "Utenlandsopphold",
                                verdiliste =
                                    utenlandsopphold.opphold.map { opphold ->
                                        VerdilisteElement(
                                            label =
                                                "${opphold.landnavn}${if (opphold.erUtenforEøs == true) " (utenfor EØS)" else ""}: " +
                                                    "${opphold.fraOgMed} - ${opphold.tilOgMed}",
                                            verdiliste =
                                                listOfNotNull(
                                                    lagVerdiElement("Er barnet sammen med deg?", opphold.erSammenMedBarnet),
                                                    opphold.erUtenforEøs?.let {
                                                        lagVerdiElement("Er barnet innlagt?", opphold.erBarnetInnlagt)
                                                    },
                                                    VerdilisteElement(
                                                        label = "Perioder:",
                                                        verdiliste =
                                                            opphold.perioderBarnetErInnlagt.map {
                                                                VerdilisteElement(label = "${it.fraOgMed} - ${it.tilOgMed}")
                                                            },
                                                    ),
                                                    lagVerdiElement("Årsak:", opphold.årsak?.beskrivelse),
                                                ),
                                        )
                                    },
                            )
                        },
                        ferieuttakIPerioden?.let {
                            VerdilisteElement(
                                "Skal du ha ferie i perioden du søker om pleiepenger?",
                                verdiliste =
                                    listOfNotNull(
                                        if (ferieuttakIPerioden.skalTaUtFerieIPerioden) {
                                            VerdilisteElement(
                                                label = "Du opplyser at du skal ha ferie:",
                                                verdiliste =
                                                    ferieuttakIPerioden.ferieuttak.map { ferie ->
                                                        VerdilisteElement(label = "${ferie.fraOgMed} - ${ferie.tilOgMed}")
                                                    },
                                            )
                                        } else {
                                            lagVerdiElement("Du opplyser at du skal ha ferie", ferieuttakIPerioden.skalTaUtFerieIPerioden)
                                        },
                                    ),
                            )
                        },
                    ),
            )
        }

    private fun mapMedlemskap(medlemskap: Medlemskap): VerdilisteElement =
        VerdilisteElement(
            label = "Medlemskap i folketrygden",
            verdiliste =
                listOfNotNull(
                    VerdilisteElement(
                        label = "Har du bodd i utlandet de siste 12 månedene?",
                        verdi = konverterBooleanTilSvar(medlemskap.harBoddIUtlandetSiste12Mnd),
                        verdiliste =
                            medlemskap.utenlandsoppholdSiste12Mnd.takeIf { it.isNotEmpty() }?.map { opphold ->
                                VerdilisteElement(
                                    label = opphold.landnavn,
                                    verdi = "${opphold.fraOgMed} - ${opphold.tilOgMed}",
                                )
                            },
                    ),
                    VerdilisteElement(
                        label = "Skal du bo i utlandet de neste 12 månedene?",
                        verdi = konverterBooleanTilSvar(medlemskap.skalBoIUtlandetNeste12Mnd),
                        verdiliste =
                            medlemskap.utenlandsoppholdNeste12Mnd.takeIf { it.isNotEmpty() }?.map { opphold ->
                                VerdilisteElement(
                                    label = opphold.landnavn,
                                    verdi = "${opphold.fraOgMed} - ${opphold.tilOgMed}",
                                )
                            },
                    ),
                ),
        )

    private fun mapVedlegg(
        vedlegg: List<String>,
        barn: Barn,
        fødselsattestVedleggId: List<String>?,
    ): VerdilisteElement =
        VerdilisteElement(
            label = "Vedlegg",
            verdiliste =
                listOfNotNull(
                    vedlegg.takeIf { it.isEmpty() }?.let {
                        lagVerdiElement("Legeerklæring", "Ingen vedlegg er lastet opp.")
                    },
                    barn.fødselsnummer?.let {
                        lagVerdiElement(
                            "Fødselsattest",
                            if (fødselsattestVedleggId.isNullOrEmpty()) {
                                "Har ikke lastet opp kopi av fødselsattest til barnet."
                            } else {
                                "Har lastet opp kopi av fødselsattest til barnet."
                            },
                        )
                    },
                ),
        )

    private fun mapSamtykke(
        harForståttRettigheterOgPlikter: Boolean,
        harBekreftetOpplysninger: Boolean,
    ): VerdilisteElement =
        VerdilisteElement(
            label = "Samtykke fra deg",
            verdiliste =
                listOfNotNull(
                    lagVerdiElement("Har du forstått dine rettigheter og plikter?", harForståttRettigheterOgPlikter),
                    lagVerdiElement("Har du bekreftet at opplysningene du har gitt er riktige?", harBekreftetOpplysninger),
                ),
        )

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
