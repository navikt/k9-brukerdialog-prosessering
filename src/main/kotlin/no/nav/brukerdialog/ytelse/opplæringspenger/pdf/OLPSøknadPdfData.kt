package no.nav.brukerdialog.ytelse.opplæringspenger.pdf

import no.nav.brukerdialog.common.Constants
import no.nav.brukerdialog.common.Constants.DATE_FORMATTER
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.pdf.PdfData
import no.nav.brukerdialog.utils.DateUtils
import no.nav.brukerdialog.utils.DateUtils.somNorskDag
import no.nav.brukerdialog.utils.DateUtils.somNorskMåned
import no.nav.brukerdialog.utils.DateUtils.ukeNummer
import no.nav.brukerdialog.utils.DurationUtils.somTekst
import no.nav.brukerdialog.utils.StringUtils.språkTilTekst
import no.nav.brukerdialog.utils.StringUtils.storForbokstav
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.OLPMottattSøknad
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.capitalizeName
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.felles.*
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.felles.type.Språk
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class OLPSøknadPdfData(private val søknad: OLPMottattSøknad) : PdfData() {
    override fun ytelse(): Ytelse = Ytelse.OPPLÆRINGSPENGER

    override fun språk(): Språk = Språk.NORSK_BOKMÅL

    override fun pdfData(): Map<String, Any?> {
        return mapOf(
            "tittel" to ytelse().utledTittel(språk()),
            "soknad_id" to søknad.søknadId,
            "soknad_mottatt_dag" to søknad.mottatt.withZoneSameInstant(Constants.OSLO_ZONE_ID).somNorskDag(),
            "soknad_mottatt" to Constants.DATE_TIME_FORMATTER.format(søknad.mottatt),
            "harIkkeVedlegg" to søknad.sjekkOmHarIkkeVedlegg(),
            "harLastetOppFødselsattest" to !søknad.fødselsattestVedleggId.isNullOrEmpty(),
            "soker" to søknad.søker.somMap(),
            "barn" to søknad.barn.somMap(),
            "periode" to mapOf(
                "fra_og_med" to Constants.DATE_FORMATTER.format(søknad.fraOgMed),
                "til_og_med" to Constants.DATE_FORMATTER.format(søknad.tilOgMed),
                "virkedager" to DateUtils.antallVirkedager(søknad.fraOgMed, søknad.tilOgMed)
            ),
            "kurs" to søknad.kurs.somMap(),
            "medlemskap" to mapOf(
                "har_bodd_i_utlandet_siste_12_mnd" to søknad.medlemskap.harBoddIUtlandetSiste12Mnd,
                "utenlandsopphold_siste_12_mnd" to søknad.medlemskap.utenlandsoppholdSiste12Mnd.somMapBosted(),
                "skal_bo_i_utlandet_neste_12_mnd" to søknad.medlemskap.skalBoIUtlandetNeste12Mnd,
                "utenlandsopphold_neste_12_mnd" to søknad.medlemskap.utenlandsoppholdNeste12Mnd.somMapBosted()
            ),
            "utenlandsoppholdIPerioden" to søknad.utenlandsoppholdIPerioden?.let {  mapOf(
                "skalOppholdeSegIUtlandetIPerioden" to (it.skalOppholdeSegIUtlandetIPerioden),
                "opphold" to it.opphold.somMapUtenlandsopphold()
            ) },
            "samtykke" to mapOf(
                "har_forstatt_rettigheter_og_plikter" to søknad.harForståttRettigheterOgPlikter,
                "har_bekreftet_opplysninger" to søknad.harBekreftetOpplysninger
            ),
            "hjelp" to mapOf(
                "ingen_arbeidsgivere" to søknad.arbeidsgivere.isEmpty(),
                "språk" to søknad.språk.språkTilTekst()
            ),
            "opptjeningIUtlandet" to søknad.opptjeningIUtlandet.somMapOpptjeningIUtlandet(),
            "utenlandskNæring" to søknad.utenlandskNæring.somMapUtenlandskNæring(),
            "ferieuttakIPerioden" to mapOf(
                "skalTaUtFerieIPerioden" to søknad.ferieuttakIPerioden?.skalTaUtFerieIPerioden,
                "ferieuttak" to søknad.ferieuttakIPerioden?.ferieuttak?.somMapFerieuttak()
            ),
            "harVærtEllerErVernepliktig" to søknad.harVærtEllerErVernepliktig,
            "frilans" to søknad.frilans?.somMap(),
            "selvstendigNæringsdrivende" to søknad.selvstendigNæringsdrivende?.somMap(),
            "arbeidsgivere" to søknad.arbeidsgivere.somMapAnsatt(),
            "ettersendingAvVedlegg" to søknad.ettersendingAvVedlegg?.let { mapOf(
                "skalEttersendeVedlegg" to it.skalEttersendeVedlegg,
                "vedleggSomSkalEttersendes" to it.vedleggSomSkalEttersendes?.map { it.beskrivelse }
            )},
            "hjelper" to mapOf(
                "harFlereAktiveVirksomheterErSatt" to søknad.harFlereAktiveVirksomehterSatt(),
                "harVærtEllerErVernepliktigErSatt" to erBooleanSatt(søknad.harVærtEllerErVernepliktig),
                "ingen_arbeidsforhold" to !søknad.harMinstEtArbeidsforhold()
            )
        )
    }

    private fun Barn.somMap() = mapOf<String, Any?>(
        "manglerNorskIdentitetsnummer" to (årsakManglerIdentitetsnummer == ÅrsakManglerIdentitetsnummer.BARNET_BOR_I_UTLANDET),
        "norskIdentitetsnummer" to norskIdentifikator,
        "navn" to navn.storForbokstav(),
        "fødselsdato" to if (fødselsdato != null) Constants.DATE_FORMATTER.format(fødselsdato) else null,
        "årsakManglerIdentitetsnummer" to årsakManglerIdentitetsnummer?.pdfTekst,
        "relasjonTilBarnet" to relasjonTilBarnet?.utskriftsvennlig,
        "relasjonTilBarnetBeskrivelse" to relasjonTilBarnetBeskrivelse,
    )

    private fun Kurs.somMap() = mapOf<String, Any?>(
        "institusjonsnavn" to kursholder.navn,
        "kursperioder" to kursperioder.somMap(),
        "reise" to reise.somMap()
    )

    private fun List<Periode>.somMap(): List<Map<String, Any?>> {
        return map {
            mapOf<String, Any?>(
                "fraOgMed" to Constants.DATE_FORMATTER.format(it.fraOgMed),
                "tilOgMed" to Constants.DATE_FORMATTER.format(it.tilOgMed)
            )
        }
    }

    private fun Reise.somMap() = mapOf<String, Any?>(
        "reiserUtenforKursdager" to reiserUtenforKursdager,
        "reisedager" to reisedager?.map { Constants.DATE_FORMATTER.format(it) },
        "reisedagerBeskrivelse" to reisedagerBeskrivelse
    )

    private fun List<Utenlandsopphold>.somMapUtenlandsopphold(): List<Map<String, Any?>> {
        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneId.of("Europe/Oslo"))
        return map {
            mapOf<String, Any?>(
                "landnavn" to it.landnavn,
                "landkode" to it.landkode,
                "fraOgMed" to dateFormatter.format(it.fraOgMed),
                "tilOgMed" to dateFormatter.format(it.tilOgMed),
            )
        }
    }

    private fun OLPMottattSøknad.harMinstEtArbeidsforhold(): Boolean {
        if (frilans?.arbeidsforhold != null) return true

        if (selvstendigNæringsdrivende?.arbeidsforhold != null) return true

        if (arbeidsgivere.any() { it.arbeidsforhold != null }) return true

        return false
    }

    private fun OLPMottattSøknad.harFlereAktiveVirksomehterSatt() =
        this.selvstendigNæringsdrivende?.virksomhet?.harFlereAktiveVirksomheter

    private fun erBooleanSatt(verdi: Boolean?) = verdi != null

    private fun List<OpptjeningIUtlandet>.somMapOpptjeningIUtlandet(): List<Map<String, Any?>>? {
        if (isEmpty()) return null
        return map {
            mapOf<String, Any?>(
                "navn" to it.navn,
                "land" to it.land.somMap(),
                "opptjeningType" to it.opptjeningType.pdfTekst,
                "fraOgMed" to Constants.DATE_FORMATTER.format(it.fraOgMed),
                "tilOgMed" to Constants.DATE_FORMATTER.format(it.tilOgMed)
            )
        }
    }

    private fun List<UtenlandskNæring>.somMapUtenlandskNæring(): List<Map<String, Any?>>? {
        if (isEmpty()) return null
        return map {
            mapOf(
                "næringstype" to it.næringstype.beskrivelse,
                "navnPåVirksomheten" to it.navnPåVirksomheten,
                "land" to it.land.somMap(),
                "organisasjonsnummer" to it.organisasjonsnummer,
                "fraOgMed" to Constants.DATE_FORMATTER.format(it.fraOgMed),
                "tilOgMed" to if (it.tilOgMed != null) Constants.DATE_FORMATTER.format(it.tilOgMed) else null
            )
        }
    }

    private fun Arbeidsforhold.somMap(): Map<String, Any?> = mapOf(
        "jobberNormaltTimer" to jobberNormaltTimer,
        "arbeidIPeriode" to arbeidIPeriode.somMap()
    )

    private fun ArbeidIPeriode.somMap(): Map<String, Any?> = mapOf(
        "jobberIPerioden" to jobberIPerioden.pdfTekst,
        "enkeltdagerPerMnd" to enkeltdager?.somMapPerMnd()
    )

    private fun List<Enkeltdag>.somMapEnkeltdag(): List<Map<String, Any?>> {
        return map {
            mapOf<String, Any?>(
                "dato" to DATE_FORMATTER.format(it.dato),
                "dag" to it.dato.dayOfWeek.somNorskDag(),
                "tid" to it.tid.somTekst(avkort = false)
            )
        }
    }

    private fun List<Enkeltdag>.somMapPerUke(): List<Map<String, Any>> {
        val perUke = grupperPerUke()
        return perUke.map {
            mapOf(
                "uke" to it.key,
                "dager" to it.value.somMapEnkeltdag()
            )
        }
    }

    private fun List<Enkeltdag>.grupperPerUke() = groupBy { it.dato.ukeNummer() }

    private fun List<Enkeltdag>.grupperPerMåned() = groupBy { it.dato.month }

    fun List<Enkeltdag>.somMapPerMnd(): List<Map<String, Any>> {
        val perMåned: Map<Month, List<Enkeltdag>> = grupperPerMåned()

        return perMåned.map {
            mapOf(
                "år" to it.value.first().dato.year,
                "måned" to it.key.somNorskMåned().capitalizeName(),
                "enkeltdagerPerUke" to it.value.somMapPerUke()
            )
        }
    }

    private fun List<Arbeidsgiver>.somMapAnsatt() = map {
        mapOf<String, Any?>(
            "navn" to it.navn,
            "organisasjonsnummer" to it.organisasjonsnummer,
            "erAnsatt" to it.erAnsatt,
            "arbeidsforhold" to it.arbeidsforhold?.somMap(),
            "sluttetFørSøknadsperiodeErSatt" to (it.sluttetFørSøknadsperiode != null),
            "sluttetFørSøknadsperiode" to it.sluttetFørSøknadsperiode
        )
    }

    private fun Frilans.somMap() = mapOf<String, Any?>(
        "startdato" to DATE_FORMATTER.format(startdato),
        "sluttdato" to if (sluttdato != null) DATE_FORMATTER.format(sluttdato) else null,
        "jobberFortsattSomFrilans" to jobberFortsattSomFrilans,
        "arbeidsforhold" to arbeidsforhold?.somMap(),
        "harHattInntektSomFrilanser" to harHattInntektSomFrilanser
    )

    private fun SelvstendigNæringsdrivende.somMap() = mapOf<String, Any?>(
        "virksomhet" to virksomhet.somMap(),
        "arbeidsforhold" to arbeidsforhold?.somMap()
    )

    private fun Virksomhet.somMap(): Map<String, Any?> = mapOf(
        "næringstypeBeskrivelse" to næringstype.beskrivelse,
        "næringsinntekt" to næringsinntekt,
        "yrkesaktivSisteTreFerdigliknedeÅrene" to yrkesaktivSisteTreFerdigliknedeÅrene?.somMap(),
        "varigEndring" to varigEndring?.somMap(),
        "harFlereAktiveVirksomheter" to harFlereAktiveVirksomheter,
        "navnPåVirksomheten" to navnPåVirksomheten,
        "fraOgMed" to Constants.DATE_FORMATTER.format(fraOgMed),
        "tilOgMed" to if (tilOgMed != null) Constants.DATE_FORMATTER.format(tilOgMed) else null,
        "fiskerErPåBladB" to fiskerErPåBladB,
        "registrertINorge" to registrertINorge,
        "organisasjonsnummer" to organisasjonsnummer,
        "registrertIUtlandet" to registrertIUtlandet?.somMap(),
        "regnskapsfører" to regnskapsfører?.somMap()
    )

    private fun Regnskapsfører.somMap() = mapOf<String, Any?>(
        "navn" to navn,
        "telefon" to telefon
    )

    private fun Land.somMap() = mapOf<String, Any?>(
        "landnavn" to landnavn,
        "landkode" to landkode
    )

    private fun YrkesaktivSisteTreFerdigliknedeArene.somMap(): Map<String, Any?> = mapOf(
        "oppstartsdato" to Constants.DATE_FORMATTER.format(oppstartsdato)
    )

    private fun VarigEndring.somMap(): Map<String, Any?> = mapOf(
        "dato" to Constants.DATE_FORMATTER.format(dato),
        "inntektEtterEndring" to inntektEtterEndring,
        "forklaring" to forklaring
    )

    private fun List<Bosted>.somMapBosted(): List<Map<String, Any?>> {
        return map {
            mapOf<String, Any?>(
                "landnavn" to it.landnavn,
                "fraOgMed" to Constants.DATE_FORMATTER.format(it.fraOgMed),
                "tilOgMed" to Constants.DATE_FORMATTER.format(it.tilOgMed)
            )
        }
    }

    private fun List<Ferieuttak>.somMapFerieuttak(): List<Map<String, Any?>> {
        return map {
            mapOf<String, Any?>(
                "fraOgMed" to Constants.DATE_FORMATTER.format(it.fraOgMed),
                "tilOgMed" to Constants.DATE_FORMATTER.format(it.tilOgMed)
            )
        }
    }

    private fun OLPMottattSøknad.sjekkOmHarIkkeVedlegg(): Boolean = vedleggId.isEmpty()
}
