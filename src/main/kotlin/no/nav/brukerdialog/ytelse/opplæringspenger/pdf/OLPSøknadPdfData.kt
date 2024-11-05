package no.nav.brukerdialog.ytelse.opplæringspenger.pdf

import no.nav.brukerdialog.common.Constants
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.OLPMottattSøknad
import no.nav.brukerdialog.pdf.PdfData
import no.nav.brukerdialog.utils.DateUtils
import no.nav.brukerdialog.utils.DateUtils.somNorskDag
import no.nav.brukerdialog.utils.DateUtils.ukeNummer
import no.nav.brukerdialog.utils.DurationUtils.tilString
import no.nav.brukerdialog.utils.StringUtils.språkTilTekst
import no.nav.brukerdialog.utils.StringUtils.storForbokstav
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.felles.*
import no.nav.k9.søknad.felles.type.Språk
import java.time.LocalDate
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
            "kurs" to søknad.kurs?.somMap(),
            "medlemskap" to mapOf(
                "har_bodd_i_utlandet_siste_12_mnd" to søknad.medlemskap.harBoddIUtlandetSiste12Mnd,
                "utenlandsopphold_siste_12_mnd" to søknad.medlemskap.utenlandsoppholdSiste12Mnd.somMapBosted(),
                "skal_bo_i_utlandet_neste_12_mnd" to søknad.medlemskap.skalBoIUtlandetNeste12Mnd,
                "utenlandsopphold_neste_12_mnd" to søknad.medlemskap.utenlandsoppholdNeste12Mnd.somMapBosted()
            ),
            "samtykke" to mapOf(
                "har_forstatt_rettigheter_og_plikter" to søknad.harForståttRettigheterOgPlikter,
                "har_bekreftet_opplysninger" to søknad.harBekreftetOpplysninger
            ),
            "hjelp" to mapOf(
                "ingen_arbeidsgivere" to søknad.arbeidsgivere.isEmpty(),
                "sprak" to søknad.språk?.språkTilTekst()
            ),
            "opptjeningIUtlandet" to søknad.opptjeningIUtlandet.somMapOpptjeningIUtlandet(),
            "utenlandskNæring" to søknad.utenlandskNæring.somMapUtenlandskNæring(),
            "utenlandsoppholdIPerioden" to mapOf(
                "skalOppholdeSegIUtlandetIPerioden" to søknad.utenlandsoppholdIPerioden.skalOppholdeSegIUtlandetIPerioden,
                "opphold" to søknad.utenlandsoppholdIPerioden.opphold.somMapUtenlandsopphold()
            ),
            "ferieuttakIPerioden" to mapOf(
                "skalTaUtFerieIPerioden" to søknad.ferieuttakIPerioden?.skalTaUtFerieIPerioden,
                "ferieuttak" to søknad.ferieuttakIPerioden?.ferieuttak?.somMapFerieuttak()
            ),
            "barnRelasjon" to søknad.barnRelasjon?.utskriftsvennlig,
            "barnRelasjonBeskrivelse" to søknad.barnRelasjonBeskrivelse,
            "harVærtEllerErVernepliktig" to søknad.harVærtEllerErVernepliktig,
            "frilans" to søknad.frilans.somMap(søknad.fraOgMed),
            "stønadGodtgjørelse" to søknad.stønadGodtgjørelse?.somMap(),
            "selvstendigNæringsdrivende" to søknad.selvstendigNæringsdrivende.somMap(),
            "arbeidsgivere" to søknad.arbeidsgivere.somMapAnsatt(),
            "hjelper" to mapOf(
                "harFlereAktiveVirksomheterErSatt" to søknad.harFlereAktiveVirksomehterSatt(),
                "harVærtEllerErVernepliktigErSatt" to erBooleanSatt(søknad.harVærtEllerErVernepliktig),
                "ingen_arbeidsforhold" to !søknad.harMinstEtArbeidsforhold()
            )
        )
    }

    private fun Barn.somMap() = mapOf<String, Any?>(
        "manglerNorskIdentitetsnummer" to (fødselsnummer == null),
        "norskIdentitetsnummer" to fødselsnummer,
        "navn" to navn.storForbokstav(),
        "fødselsdato" to if (fødselsdato != null) Constants.DATE_FORMATTER.format(fødselsdato) else null,
        "årsakManglerIdentitetsnummer" to årsakManglerIdentitetsnummer?.pdfTekst
    )

    private fun Kurs.somMap() = mapOf<String, Any?>(
        "institusjonsnavn" to kursholder.navn,
        "institusjosId" to kursholder.id,
        "kursperioder" to perioder.somMapPerioderMedReiseTid()
    )

    private fun List<KursPerioderMedReiseTid>.somMapPerioderMedReiseTid(): List<Map<String, Any?>> {
        return map {
            mapOf<String, Any?>(
                "fraOgMed" to Constants.DATE_FORMATTER.format(it.fraOgMed.toLocalDate()),
                "tilOgMed" to Constants.DATE_FORMATTER.format(it.tilOgMed.toLocalDate()),
                "avreise" to Constants.DATE_FORMATTER.format(it.avreise),
                "hjemkomst" to Constants.DATE_FORMATTER.format(it.hjemkomst)
            )
        }
    }

    private fun OLPMottattSøknad.harMinstEtArbeidsforhold(): Boolean {
        if (frilans.arbeidsforhold != null) return true

        if (selvstendigNæringsdrivende.arbeidsforhold != null) return true

        if (arbeidsgivere.any() { it.arbeidsforhold != null }) return true

        return false
    }

    private fun OLPMottattSøknad.harFlereAktiveVirksomehterSatt() =
        (this.selvstendigNæringsdrivende.virksomhet?.harFlereAktiveVirksomheter != null)

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
        "data" to this.toString(),
        "normalarbeidstid" to this.normalarbeidstid.somMap(),
        "arbeidIPeriode" to this.arbeidIPeriode.somMap()
    )

    private fun ArbeidIPeriode.somMap(): Map<String, Any?> = mapOf(
        "type" to this.type.name,
        "redusertArbeid" to this.redusertArbeid?.somMap()
    )

    private fun ArbeidsRedusert.somMap() = mapOf(
        "type" to this.type.name,
        "timerPerUke" to this.timerPerUke?.tilString(),
        "prosentAvNormalt" to this.prosentAvNormalt?.somString(),
        "arbeidsuker" to this.arbeidsuker?.somMap()
    )

    @JvmName("somMapArbeidsUke")
    private fun List<ArbeidsUke>.somMap() = sortedBy { it: ArbeidsUke -> it.periode.fraOgMed }
        .map {
            val fraOgMed = it.periode.fraOgMed
            mapOf(
                "uke" to fraOgMed.ukeNummer(),
                "faktiskTimerPerUke" to it.timer?.tilString()
            )
        }

    private fun NormalArbeidstid.somMap(): Map<String, Any?> = mapOf(
        "timerPerUkeISnitt" to this.timerPerUkeISnitt.tilString()
    )

    private fun Frilans.somMap(søknadsperiodeStartdato: LocalDate): Map<String, Any?> = mapOf(
        "harInntektSomFrilanser" to harInntektSomFrilanser,
        "startetFørSisteTreHeleMåneder" to startetFørSisteTreHeleMåneder,
        "sisteTreMånederFørSøknadsperiodeStart" to Constants.DATE_FORMATTER.format(søknadsperiodeStartdato.minusMonths(3)),
        "startdato" to if (startdato != null) Constants.DATE_FORMATTER.format(startdato) else null,
        "sluttdato" to if (sluttdato != null) Constants.DATE_FORMATTER.format(sluttdato) else null,
        "jobberFortsattSomFrilans" to jobberFortsattSomFrilans,
        "type" to type?.name,
        "misterHonorar" to misterHonorar,
        "arbeidsforhold" to arbeidsforhold?.somMap()
    )

    private fun SelvstendigNæringsdrivende.somMap(): Map<String, Any?> = mapOf(
        "harInntektSomSelvstendig" to harInntektSomSelvstendig,
        "virksomhet" to virksomhet?.somMap(),
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

    private fun YrkesaktivSisteTreFerdigliknedeÅrene.somMap(): Map<String, Any?> = mapOf(
        "oppstartsdato" to Constants.DATE_FORMATTER.format(oppstartsdato)
    )

    private fun VarigEndring.somMap(): Map<String, Any?> = mapOf(
        "dato" to Constants.DATE_FORMATTER.format(dato),
        "inntektEtterEndring" to inntektEtterEndring,
        "forklaring" to forklaring
    )

    private fun List<Arbeidsgiver>.somMapAnsatt() = map {
        mapOf(
            "navn" to it.navn,
            "organisasjonsnummer" to it.organisasjonsnummer,
            "erAnsatt" to it.erAnsatt,
            "arbeidsforhold" to it.arbeidsforhold?.somMap(),
            "sluttetFørSøknadsperiodeErSatt" to (it.sluttetFørSøknadsperiode != null),
            "sluttetFørSøknadsperiode" to it.sluttetFørSøknadsperiode
        )
    }

    private fun List<Bosted>.somMapBosted(): List<Map<String, Any?>> {
        return map {
            mapOf<String, Any?>(
                "landnavn" to it.landnavn,
                "fraOgMed" to Constants.DATE_FORMATTER.format(it.fraOgMed),
                "tilOgMed" to Constants.DATE_FORMATTER.format(it.tilOgMed)
            )
        }
    }

    private fun List<Utenlandsopphold>.somMapUtenlandsopphold(): List<Map<String, Any?>> {
        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneId.of("Europe/Oslo"))
        return map {
            mapOf(
                "landnavn" to it.landnavn,
                "landkode" to it.landkode,
                "fraOgMed" to dateFormatter.format(it.fraOgMed),
                "tilOgMed" to dateFormatter.format(it.tilOgMed),
                "erUtenforEØS" to it.erUtenforEøs,
                "erSammenMedBarnet" to it.erSammenMedBarnet,
                "erBarnetInnlagt" to it.erBarnetInnlagt,
                "perioderBarnetErInnlagt" to it.perioderBarnetErInnlagt.somMapPerioder(),
                "årsak" to it.årsak?.beskrivelse
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

    private fun List<Periode>.somMapPerioder(): List<Map<String, Any?>> {
        return map {
            mapOf<String, Any?>(
                "fraOgMed" to Constants.DATE_FORMATTER.format(it.fraOgMed),
                "tilOgMed" to Constants.DATE_FORMATTER.format(it.tilOgMed)
            )
        }
    }

    private fun Double.somString(): String {
        val split = toString().split(".")
        return if (split[1] == "0") split[0]
        else split.joinToString(".")
    }

    private fun OLPMottattSøknad.sjekkOmHarIkkeVedlegg(): Boolean = vedleggId.isEmpty()
    private fun StønadGodtgjørelse.somMap() = mapOf(
        "mottarStønadGodtgjørelse" to mottarStønadGodtgjørelse,
        "startdato" to startdato?.let { Constants.DATE_FORMATTER.format(it) },
        "startetIPerioden" to (startdato != null),
        "sluttdato" to sluttdato?.let { Constants.DATE_FORMATTER.format(it) },
        "sluttetIPerioden" to (sluttdato != null),
    )
}
