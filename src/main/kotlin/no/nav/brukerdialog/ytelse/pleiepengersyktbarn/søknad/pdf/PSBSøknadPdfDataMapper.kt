package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf
import no.nav.brukerdialog.common.VerdilisteElement
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.PSBMottattSøknad
import no.nav.brukerdialog.pdf.lagVerdiElement
import no.nav.brukerdialog.pdf.lagVerdiElement3
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner.strukturerArbeidsgivereSeksjon
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner.strukturerBeredskapSeksjon
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner.strukturerFrilansSeksjon
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner.strukturerInnsendingsdetaljerSeksjon
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner.strukturerJobbISøknadsperiodenSeksjon
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner.strukturerMedlemskapSeksjon
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner.strukturerNattevåkSeksjon
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner.strukturerOpptjeningIUtlandetSeksjon
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner.strukturerPerioderSeksjon
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner.strukturerRelasjonTilBarnetSeksjon
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner.strukturerSamtykkeSeksjon
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner.strukturerSelvstendigNæringsdrivendeSeksjon
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner.strukturerStønadGodtgjørelseSeksjon
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner.strukturerSøkerSeksjon
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner.strukturerUtenlandskNæringSeksjon
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner.strukturerUtenlandsoppholdSeksjon
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner.strukturerVedleggSeksjon
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.seksjoner.strukturerVernepliktSeksjon
import no.nav.helse.felles.Omsorgstilbud

object PSBSøknadPdfDataMapper {
    fun mapPSBSøknadPdfData(søknad: PSBMottattSøknad): List<VerdilisteElement> =
        listOfNotNull(
            strukturerInnsendingsdetaljerSeksjon(søknad.mottatt),
            strukturerSøkerSeksjon(søknad.søker, søknad.barn),
            strukturerRelasjonTilBarnetSeksjon(søknad.barnRelasjon, søknad.barnRelasjonBeskrivelse),
            strukturerPerioderSeksjon(søknad.fraOgMed, søknad.tilOgMed),
            strukturerArbeidsgivereSeksjon(søknad.arbeidsgivere, søknad.fraOgMed),
            strukturerStønadGodtgjørelseSeksjon(søknad.stønadGodtgjørelse),
            strukturerFrilansSeksjon(søknad.frilans),
            strukturerSelvstendigNæringsdrivendeSeksjon(søknad.selvstendigNæringsdrivende),
            strukturerJobbISøknadsperiodenSeksjon(
                søknad.arbeidsgivere,
                søknad.frilans,
                søknad.selvstendigNæringsdrivende.arbeidsforhold,
            ),
            strukturerOpptjeningIUtlandetSeksjon(søknad.opptjeningIUtlandet),
            strukturerUtenlandskNæringSeksjon(søknad.utenlandskNæring),
            strukturerVernepliktSeksjon(søknad.harVærtEllerErVernepliktig),
            strukturerOmsorgstilbudSeksjon(søknad.omsorgstilbud),
            strukturerNattevåkSeksjon(søknad.nattevåk),
            strukturerBeredskapSeksjon(søknad.beredskap),
            strukturerUtenlandsoppholdSeksjon(søknad.utenlandsoppholdIPerioden, søknad.ferieuttakIPerioden),
            strukturerMedlemskapSeksjon(søknad.medlemskap),
            strukturerVedleggSeksjon(søknad.vedleggId, søknad.barn, søknad.fødselsattestVedleggId),
            strukturerSamtykkeSeksjon(søknad.harForståttRettigheterOgPlikter, søknad.harBekreftetOpplysninger),
        )

    // TODO påVent
    private fun strukturerOmsorgstilbudSeksjon(omsorgstilbud: Omsorgstilbud?): VerdilisteElement {
        val test = mapOmsorgstilbudTilSpørsmålOgSvarSeksjon(omsorgstilbud)
        return VerdilisteElement(
            label = "Omsorgstilbud",
            verdiliste =
                if (omsorgstilbud != null) {
                    listOfNotNull(
                        lagVerdiElement3(test.harVærtIOmsorgstilbud),
                        lagVerdiElement3(test.skalVæreIOmsorgstilbud),
                        lagVerdiElement3(test.erLiktHverUke),
                    ).plus(
                        test.tidIOmsorgstilbud?.map {
                            VerdilisteElement(
                                label = "Tid barnet er i Omsorgstilbud",
                                verdiliste = listOfNotNull(VerdilisteElement(label = it.månedOgÅr ?: "")),
                            )
                        } ?: emptyList(),
                    )
//                        omsorgstilbud.enkeltdager.takeIf { it != null }?.let {
//                            VerdilisteElement(
//                                label = "Tid barnet er i omsorgstilbud:",
//                                verdiliste =
//                                    it.somMapPerMnd().map { måned ->
//                                        VerdilisteElement(
//                                            label = "${måned.navnPåMåned} ${måned.år}",
//                                            visningsVariant = "TABELL",
//                                            verdiliste =
//                                                måned.uker.map { ukeData ->
//                                                    VerdilisteElement(
//                                                        label = "Uke ${ukeData.uke}",
//                                                        verdiliste =
//                                                            ukeData.dager.map { dagData ->
//                                                                VerdilisteElement(
//                                                                    label = dagData.dato,
//                                                                    verdi = dagData.tid,
//                                                                )
//                                                            },
//                                                    )
//                                                },
//                                        )
//                                    },
//                            )
//                        },
//                        omsorgstilbud.ukedager.takeIf { it != null }.let { ukedag ->
//                            VerdilisteElement(
//                                label = "Faste dager barnet er i omsorgtilbud: ",
//                                verdiliste =
//                                    listOfNotNull(
//                                        lagVerdiElement(
//                                            "Mandager: ",
//                                            omsorgstilbud.ukedager?.mandag?.somTekst(true),
//                                            ukedag?.mandag,
//                                        ),
//                                        lagVerdiElement(
//                                            "Tirsdager: ",
//                                            omsorgstilbud.ukedager?.tirsdag?.somTekst(true),
//                                            ukedag?.tirsdag,
//                                        ),
//                                        lagVerdiElement(
//                                            "Onsdager: ",
//                                            omsorgstilbud.ukedager?.onsdag?.somTekst(true),
//                                            ukedag?.onsdag,
//                                        ),
//                                        lagVerdiElement(
//                                            "Torsdager: ",
//                                            omsorgstilbud.ukedager?.torsdag?.somTekst(true),
//                                            ukedag?.torsdag,
//                                        ),
//                                        lagVerdiElement(
//                                            "Fredager: ",
//                                            omsorgstilbud.ukedager?.fredag?.somTekst(true),
//                                            ukedag?.fredag,
//                                        ),
//                                    ),
//                            )
//                        },
//                    )
                } else {
                    listOfNotNull(
                        lagVerdiElement("Har barnet vært fast og regelmessig i et omsorgstilbud?", "Nei"),
                    )
                },
        )
    }
}
