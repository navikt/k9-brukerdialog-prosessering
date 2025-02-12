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
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Barn
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.BarnRelasjon
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Beredskap
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.FerieuttakIPerioden
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Frilans
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.FrilansType
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Medlemskap
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Nattevåk
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.OpptjeningIUtlandet
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.SelvstendigNæringsdrivende
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.StønadGodtgjørelse
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.UtenlandskNæring
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.UtenlandsoppholdIPerioden
import no.nav.brukerdialog.pdf.konverterBooleanTilSvar
import no.nav.brukerdialog.pdf.lagVerdiElement
import no.nav.brukerdialog.pdf.normalArbeidstid
import no.nav.brukerdialog.pdf.somMapPerMnd
import no.nav.brukerdialog.utils.DateUtils.somNorskDag
import no.nav.brukerdialog.utils.DurationUtils.somTekst
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.helse.felles.Omsorgstilbud
import java.time.LocalDate
import java.time.ZonedDateTime

object PSBSøknadPdfDataMapper {
    fun mapPSBSøknadPdfData(
        ytelseTittel: String,
        søknad: PSBMottattSøknad,
    ): FeltMap =
        FeltMap(
            label = ytelseTittel,
            verdiliste =
                listOfNotNull(
                    mapInnsendingsdetaljer(søknad.mottatt),
                    mapSøker(søknad.søker, søknad.barn),
                    mapRelasjonTilBarnet(søknad.barnRelasjon, søknad.barnRelasjonBeskrivelse),
                    mapPerioder(søknad.fraOgMed, søknad.tilOgMed),
                    mapArbeidsgivere(søknad.arbeidsgivere, søknad.fraOgMed),
                    mapStønadGodtgjørelse(søknad.stønadGodtgjørelse),
                    mapFrilans(søknad.frilans),
                    mapSelvstendigNæringsdrivende(søknad.selvstendigNæringsdrivende),
                    mapJobbISøknadsperioden(
                        søknad.harMinstEtArbeidsforhold(),
                        søknad.arbeidsgivere,
                        søknad.frilans,
                        søknad.selvstendigNæringsdrivende.arbeidsforhold,
                    ),
                    mapOpptjeningIUtlandet(søknad.opptjeningIUtlandet),
                    maputenlandskNæring(søknad.utenlandskNæring),
                    mapVerneplikt(søknad.harVærtEllerErVernepliktig),
                    mapOmsorgstilbud(søknad.omsorgstilbud),
                    mapNattevåk(søknad.nattevåk),
                    mapBeredskap(søknad.beredskap),
                    mapUtenlandsopphold(søknad.utenlandsoppholdIPerioden, søknad.ferieuttakIPerioden),
                    mapMedlemskap(søknad.medlemskap),
                    mapVedlegg(søknad.vedleggId, søknad.barn, søknad.fødselsattestVedleggId),
                    mapSamtykke(søknad.harForståttRettigheterOgPlikter, søknad.harBekreftetOpplysninger),
                ),
            pdfConfig = PdfConfig(true, "nb"),
        )

    private fun mapInnsendingsdetaljer(mottatt: ZonedDateTime): VerdilisteElement {
        val tidspunkt = "${mottatt.withZoneSameInstant(OSLO_ZONE_ID).somNorskDag()} ${DATE_TIME_FORMATTER.format(mottatt)}"

        return VerdilisteElement(
            label = "Innsendingsdetaljer",
            verdiliste =
                listOfNotNull(
                    lagVerdiElement("Sendt til Nav", tidspunkt),
                ),
        )
    }

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
                        lagVerdiElement("Hvilken relasjon har du til barnet?", barnRelasjon.utskriftsvennlig),
                        lagVerdiElement("Din beskrivelse av relasjon og tilsynsrolle for barnet:", barnRelasjonBeskrivelse),
                    ),
            )
        }

    private fun mapPerioder(
        fraOgMed: LocalDate,
        tilOgMed: LocalDate,
    ): VerdilisteElement =
        VerdilisteElement(
            label = "Perioder du søker om pleiepenger",
            verdiliste = (
                listOfNotNull(
                    lagVerdiElement(
                        "Hvilke periode har du søkt om pleiepenger?",
                        "${DATE_FORMATTER.format(fraOgMed)} - ${DATE_FORMATTER.format(tilOgMed)}",
                    ),
                )
            ),
        )

    private fun mapArbeidsgivere(
        arbeidsgivere: List<Arbeidsgiver>,
        fraOgMed: LocalDate,
    ): VerdilisteElement =
        arbeidsgivere.takeIf { it.isNotEmpty() }?.let {
            VerdilisteElement(
                label = "Arbeidsgivere",
                visningsVariant = "TABELL",
                verdiliste =
                    arbeidsgivere.map { arbeidsgiver ->
                        VerdilisteElement(
                            label = "${arbeidsgiver.navn} (orgnr: ${arbeidsgiver.organisasjonsnummer})",
                            verdiliste =
                                listOfNotNull(
                                    lagVerdiElement("Jobber du her nå?", if (arbeidsgiver.erAnsatt) "Er ansatt" else "Er ikke ansatt"),
                                    lagVerdiElement(
                                        "Sluttet du hos ${arbeidsgiver.navn} før ${DATE_FORMATTER.format(fraOgMed)}?",
                                        arbeidsgiver.sluttetFørSøknadsperiode,
                                    ),
                                    lagVerdiElement(
                                        "Hvor mange timer jobber du normalt per uke?",
                                        normalArbeidstid(arbeidsgiver.arbeidsforhold?.normalarbeidstid?.timerPerUkeISnitt),
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
            val startetÅMottaUnderveisTekst = stønadGodtgjørelse.startdato?.let { "Ja. Startet $it" } ?: "Nei"
            val sluttetÅMottaUnderveisTekst = stønadGodtgjørelse.sluttdato?.let { "Ja. Sluttet $it" } ?: "Nei"

            VerdilisteElement(
                label = "Omsorgsstønad eller fosterhjemsgodtgjørelse",
                verdiliste =
                    listOfNotNull(
                        lagVerdiElement(
                            "Mottar du omsorgsstønad eller fosterhjemsgodtgjørelse?",
                            stønadGodtgjørelse.mottarStønadGodtgjørelse,
                        ),
                        lagVerdiElement("Startet du å motta dette underveis i perioden du søker for?", startetÅMottaUnderveisTekst),
                        lagVerdiElement("Slutter du å motta dette underveis i perioden du søker for?", sluttetÅMottaUnderveisTekst),
                    ),
            )
        }

    private fun mapFrilans(frilans: Frilans?): VerdilisteElement? {
        val sisteTreMånederFørSøknadsperiodeStart = DATE_FORMATTER.format(frilans?.startdato?.minusMonths(3))
        return frilans?.takeIf { it.harInntektSomFrilanser }?.let {
            VerdilisteElement(
                label = "Frilans",
                verdiliste =
                    listOfNotNull(
                        lagVerdiElement("Jobber du som frilanser eller mottar du honorarer?", frilans.harInntektSomFrilanser),
                        when (frilans.type) {
                            FrilansType.FRILANS -> {
                                VerdilisteElement(
                                    label = "Jobber som frilanser",
                                    verdiliste =
                                        listOfNotNull(
                                            frilans.startetFørSisteTreHeleMåneder.takeIf { it == true }?.let {
                                                lagVerdiElement(
                                                    "Startet du som frilanser før $sisteTreMånederFørSøknadsperiodeStart?",
                                                    frilans.startetFørSisteTreHeleMåneder,
                                                )
                                            } ?: lagVerdiElement("Når startet du som frilanser?", frilans.startdato),
                                            lagVerdiElement("Jobber du fremdeles som frilanser?", frilans.jobberFortsattSomFrilans),
                                            lagVerdiElement(
                                                "Når sluttet du som frilanser?",
                                                frilans.sluttdato,
                                                typeSomSkalSjekkes = (frilans.jobberFortsattSomFrilans?.not()),
                                            ),
                                        ),
                                )
                            } FrilansType.FRILANS_HONORAR -> {
                                VerdilisteElement(
                                    label = "Jobber som frilanser og mottar honorar",
                                    verdiliste =
                                        listOfNotNull(
                                            frilans.startetFørSisteTreHeleMåneder.takeIf { it == true }?.let {
                                                lagVerdiElement(
                                                    "Startet du som frilanser/startet å motta honorar før $sisteTreMånederFørSøknadsperiodeStart?",
                                                    frilans.startetFørSisteTreHeleMåneder,
                                                )
                                            } ?: lagVerdiElement(
                                                "Når begynte du å jobbe som frilanser/startet å motta honorar?",
                                                frilans.startdato,
                                            ),
                                            lagVerdiElement(
                                                "Jobber du fremdeles som frilanser/mottar honorar?",
                                                frilans.jobberFortsattSomFrilans,
                                            ),
                                            lagVerdiElement(
                                                "Når sluttet du som frilanser/sluttet å motta honorar?",
                                                frilans.sluttdato,
                                                (frilans.jobberFortsattSomFrilans?.not()),
                                            ),
                                        ),
                                )
                            } FrilansType.HONORAR -> {
                                VerdilisteElement(
                                    label = "Mottar honorar",
                                    verdiliste =
                                        listOfNotNull(
                                            frilans.startetFørSisteTreHeleMåneder.takeIf { it == true }?.let {
                                                lagVerdiElement(
                                                    "Startet du å motta honorar før $sisteTreMånederFørSøknadsperiodeStart?",
                                                    frilans.startetFørSisteTreHeleMåneder,
                                                )
                                            } ?: lagVerdiElement("Når begynte du å motta honorar?", frilans.startdato),
                                            lagVerdiElement("Mottar du fortsatt honorarer?", frilans.jobberFortsattSomFrilans),
                                            lagVerdiElement(
                                                "Når sluttet du å motta honorar?",
                                                frilans.sluttdato,
                                                frilans.jobberFortsattSomFrilans?.not(),
                                            ),
                                            frilans.misterHonorar?.let {
                                                lagVerdiElement(
                                                    "Mister du honorar i søknadsperioden?",
                                                    if (it) "Jeg mister honorar i søknadsperioden" else "Jeg mister ikke honorar i søknadsperioden",
                                                )
                                            },
                                        ),
                                )
                            }
                            else -> null
                        },
                        frilans.arbeidsforhold?.let {
                            lagVerdiElement(
                                "",
                                normalArbeidstid(frilans.arbeidsforhold.normalarbeidstid.timerPerUkeISnitt),
                            )
                        },
                    ),
            )
        }
            ?: lagVerdiElement("Frilans", "Har ikke vært frilanser eller mottatt honorar i perioden det søkes om.")
    }

    private fun mapSelvstendigNæringsdrivende(selvstendigNæringsdrivende: SelvstendigNæringsdrivende): VerdilisteElement =
        selvstendigNæringsdrivende.takeIf { it.harInntektSomSelvstendig }?.let {
            VerdilisteElement(
                label = "Selvstendig næringsdrivende",
                verdiliste =
                    listOfNotNull(
                        lagVerdiElement("Næringsinntekt:", selvstendigNæringsdrivende.virksomhet?.næringsinntekt),
                        lagVerdiElement(
                            "Oppgi dato for når du ble yrkesaktiv: ",
                            selvstendigNæringsdrivende.virksomhet?.yrkesaktivSisteTreFerdigliknedeÅrene?.oppstartsdato,
                        ),
                        lagVerdiElement(
                            "Hvor mange timer jobber du normalt per uke?",
                            normalArbeidstid(selvstendigNæringsdrivende.arbeidsforhold?.normalarbeidstid?.timerPerUkeISnitt),
                        ),
                        selvstendigNæringsdrivende.virksomhet?.varigEndring?.let {
                            lagVerdiElement(
                                "Dato for når varig endring oppsto:",
                                selvstendigNæringsdrivende.virksomhet.varigEndring.dato,
                            )
                            lagVerdiElement(
                                "Næringsinntekt etter endringen:",
                                selvstendigNæringsdrivende.virksomhet.varigEndring.inntektEtterEndring,
                            )
                            lagVerdiElement(
                                "Din forklaring om varig endring:",
                                selvstendigNæringsdrivende.virksomhet.varigEndring.forklaring,
                            )
                        },
                        lagVerdiElement(
                            "Har du flere enn én næringsvirksomhet som er aktiv?",
                            selvstendigNæringsdrivende.virksomhet?.harFlereAktiveVirksomheter,
                        ),
                        VerdilisteElement(
                            label = "Næringsvirksomhet som du har lagt inn:",
                            visningsVariant = "TABELL",
                            verdiliste =
                                listOfNotNull(
                                    selvstendigNæringsdrivende.virksomhet?.let { virksomhet ->
                                        val virksomhetTittel =
                                            "${virksomhet.navnPåVirksomheten} " +
                                                "(startet ${DATE_FORMATTER.format(virksomhet.fraOgMed)}, " +
                                                "${if (virksomhet.tilOgMed == null) {
                                                    "er pågående)"
                                                } else {
                                                    "Avsluttet ${DATE_FORMATTER.format(
                                                        virksomhet.tilOgMed,
                                                    )})"
                                                } },"
                                        val næringstypeTekst =
                                            "${virksomhet.næringstype.beskrivelse} " +
                                                "(${virksomhet.fiskerErPåBladB?.let { if (it) "blad B" else "ikke blad B" } ?: ""})"
                                        VerdilisteElement(
                                            label = virksomhetTittel,
                                            verdiliste =
                                                listOfNotNull(
                                                    lagVerdiElement("Næringstype: ", næringstypeTekst),
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
                                                    virksomhet.regnskapsfører?.let {
                                                        lagVerdiElement("Regnskapsfører", it.navn)
                                                        lagVerdiElement("Telefon:", it.telefon)
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
    ): VerdilisteElement {
        val verdilisteElementer = mutableListOf<VerdilisteElement?>()
        verdilisteElementer.add(
            lagVerdiElement(
                "Arbeidsgivere",
                if (!ingenArbeidsforhold) "Ingen arbeidsforhold er registrert i søknadsperioden" else null,
            ),
        )
        arbeidsgivere.takeIf { it.isNotEmpty() }?.map { arbeidsgiver ->
            if (arbeidsgiver.erAnsatt) {
                verdilisteElementer.add(
                    lagVerdiElement(
                        "${arbeidsgiver.navn} (orgnr: ${arbeidsgiver.organisasjonsnummer})",
                        arbeidsgiver.arbeidsforhold,
                    ),
                )
            }
        }
        verdilisteElementer.add(
            frilans?.harInntektSomFrilanser.takeIf { it == true }?.let {
                lagVerdiElement("Frilans", frilans?.arbeidsforhold)
            },
        )
        verdilisteElementer.add(
            selvstendigNæringsdrivendeArbeidsforhold?.let {
                lagVerdiElement("Selvstendig næringsdrivende", selvstendigNæringsdrivendeArbeidsforhold)
            },
        )

        return VerdilisteElement(
            label = "Jobb i søknadsperioden",
            verdiliste = verdilisteElementer.filterNotNull(),
        )
    }

    private fun mapOpptjeningIUtlandet(opptjeningUtland: List<OpptjeningIUtlandet>): VerdilisteElement =
        VerdilisteElement(
            label = "Jobbet i annet EØS-land",
            verdiliste =
                if (opptjeningUtland.isNotEmpty()) {
                    opptjeningUtland.flatMap { opptjent ->
                        listOfNotNull(
                            lagVerdiElement(
                                "${opptjent.land.landnavn}:",
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
                visningsVariant = "TABELL",
                verdiliste =
                    utenlandskNæring.map { næring ->
                        VerdilisteElement(
                            label = "${næring.navnPåVirksomheten} (${DATE_FORMATTER.format(
                                næring.fraOgMed,
                            )} - ${næring.tilOgMed?.let { DATE_FORMATTER.format(næring.tilOgMed) } ?: ""})",
                            verdiliste =
                                listOfNotNull(
                                    lagVerdiElement("Land:", "${næring.land.landnavn} ${næring.land.landkode}"),
                                    lagVerdiElement("Organisasjonsnummer:", næring.organisasjonsnummer),
                                    lagVerdiElement("Næringstype:", næring.næringstype.beskrivelse),
                                ),
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
                    listOfNotNull(lagVerdiElement("Utøvde du verneplikt på tidspunktet du søker pleiepenger fra?", verneplikt)),
            )
        }

    private fun mapOmsorgstilbud(omsorgstilbud: Omsorgstilbud?): VerdilisteElement =
        VerdilisteElement(
            label = "Omsorgstilbud",
            verdiliste =
                if (omsorgstilbud != null) {
                    listOfNotNull(
                        lagVerdiElement("Har barnet vært fast og regelmessig i et omsorgstilbud?", omsorgstilbud.svarFortid),
                        lagVerdiElement("Skal barnet være fast og regelmessig i et omsorgstilbud?", omsorgstilbud.svarFremtid),
                        lagVerdiElement("Er tiden i omsorgstilbudet lik hver uke?", omsorgstilbud.erLiktHverUke),
                        omsorgstilbud.enkeltdager.takeIf { it != null }?.let {
                            VerdilisteElement(
                                label = "Tid barnet er i omsorgstilbud:",
                                verdiliste =
                                    it.somMapPerMnd().map { måned ->
                                        VerdilisteElement(
                                            label = "${måned.navnPåMåned} ${måned.år}",
                                            visningsVariant = "TABELL",
                                            verdiliste =
                                                måned.uker.map { ukeData ->
                                                    VerdilisteElement(
                                                        label = "Uke ${ukeData.uke}",
                                                        verdiliste =
                                                            ukeData.dager.map { dagData ->
                                                                VerdilisteElement(label = dagData.dato, verdi = dagData.tid)
                                                            },
                                                    )
                                                },
                                        )
                                    },
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

    private fun mapNattevåk(nattevåk: Nattevåk?): VerdilisteElement? =
        nattevåk.takeIf { it != null }?.let {
            VerdilisteElement(
                label = "Nattevåk",
                verdiliste =
                    listOfNotNull(
                        lagVerdiElement(
                            "Må du være våken om natten for å pleie barnet, og derfor må være borte fra jobb dagen etter?",
                            it.harNattevåk,
                        ),
                        lagVerdiElement("Dine tilleggsopplysninger:", it.tilleggsinformasjon),
                    ),
            )
        }

    private fun mapBeredskap(beredskap: Beredskap?): VerdilisteElement? =
        beredskap?.let {
            VerdilisteElement(
                label = "Beredskap",
                verdiliste =
                    listOfNotNull(
                        lagVerdiElement("Må du være i beredskap også når barnet er i et omsorgstilbud?", beredskap.beredskap),
                        lagVerdiElement("Dine tilleggsopplysninger:", beredskap.tilleggsinformasjon),
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
                                visningsVariant = "TABELL",
                                verdiliste =
                                    utenlandsopphold.opphold.map { opphold ->
                                        VerdilisteElement(
                                            label =
                                                "${opphold.landnavn}${if (opphold.erUtenforEøs == true) " (utenfor EØS)" else ""}: " +
                                                    "${DATE_FORMATTER.format(
                                                        opphold.fraOgMed,
                                                    )} - ${DATE_FORMATTER.format(opphold.tilOgMed)}",
                                            verdiliste =
                                                listOfNotNull(
                                                    lagVerdiElement("Er barnet sammen med deg?", opphold.erSammenMedBarnet),
                                                    lagVerdiElement("Er barnet innlagt?", opphold.erBarnetInnlagt, opphold.erUtenforEøs),
                                                    lagVerdiElement(
                                                        "Perioder:",
                                                        opphold.perioderBarnetErInnlagt.joinToString(", ") {
                                                            "${DATE_FORMATTER.format(it.fraOgMed)} - ${DATE_FORMATTER.format(it.tilOgMed)}"
                                                        },
                                                    ),
                                                    lagVerdiElement("Årsak:", opphold.årsak?.beskrivelse),
                                                ),
                                        )
                                    },
                            )
                        },
                        ferieuttakIPerioden?.let {
                            lagVerdiElement(
                                "Skal du ha ferie i perioden du søker om pleiepenger?",
                                "Du opplyser at du skal ha ferie \n - ${ferieuttakIPerioden.ferieuttak.joinToString("\n - ")
                                    {"${DATE_FORMATTER.format(it.fraOgMed)} - ${DATE_FORMATTER.format(it.tilOgMed)}" }}",
                            )
                        } ?: lagVerdiElement("Skal du ha ferie i perioden du søker om pleiepenger?", ferieuttakIPerioden),
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
                            medlemskap.utenlandsoppholdSiste12Mnd.takeIf { it.isNotEmpty() }?.flatMap { opphold ->
                                listOfNotNull(
                                    lagVerdiElement(opphold.landnavn, "${opphold.fraOgMed} - ${opphold.tilOgMed}"),
                                )
                            },
                    ),
                    VerdilisteElement(
                        label = "Skal du bo i utlandet de neste 12 månedene?",
                        verdi = konverterBooleanTilSvar(medlemskap.skalBoIUtlandetNeste12Mnd),
                        verdiliste =
                            medlemskap.utenlandsoppholdNeste12Mnd.takeIf { it.isNotEmpty() }?.flatMap { opphold ->
                                listOfNotNull(
                                    lagVerdiElement(opphold.landnavn, "${opphold.fraOgMed} - ${opphold.tilOgMed}"),
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
}

private fun PSBMottattSøknad.harMinstEtArbeidsforhold(): Boolean {
    if (frilans.arbeidsforhold != null) return true

    if (selvstendigNæringsdrivende.arbeidsforhold != null) return true

    if (arbeidsgivere.any { it.arbeidsforhold != null }) return true

    return false
}
