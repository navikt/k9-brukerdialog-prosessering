package no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn

import no.nav.helse.felles.Enkeltdag
import no.nav.helse.felles.Omsorgstilbud
import no.nav.helse.felles.PlanUkedager
import no.nav.k9brukerdialogprosessering.common.Constants
import no.nav.k9brukerdialogprosessering.common.Ytelse
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.PSBMottattSøknad
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.ArbeidIPeriode
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.ArbeidsRedusert
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.ArbeidsUke
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Arbeidsforhold
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Arbeidsgiver
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Barn
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Beredskap
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Bosted
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Ferieuttak
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Frilans
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Land
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Nattevåk
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.NormalArbeidstid
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.OpptjeningIUtlandet
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Periode
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Regnskapsfører
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.SelvstendigNæringsdrivende
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.StønadGodtgjørelse
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.UtenlandskNæring
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Utenlandsopphold
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.VarigEndring
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Virksomhet
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.YrkesaktivSisteTreFerdigliknedeÅrene
import no.nav.k9brukerdialogprosessering.pdf.PdfData
import no.nav.k9brukerdialogprosessering.utils.DateUtils
import no.nav.k9brukerdialogprosessering.utils.DurationUtils.somTekst
import no.nav.k9brukerdialogprosessering.utils.DurationUtils.tilString
import no.nav.k9brukerdialogprosessering.utils.StringUtils.språkTilTekst
import no.nav.k9brukerdialogprosessering.utils.StringUtils.storForbokstav
import no.nav.k9brukerdialogprosessering.utils.somNorskDag
import no.nav.k9brukerdialogprosessering.utils.somNorskMåned
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields

class PSBSøknadPdfData(private val søknad: PSBMottattSøknad) : PdfData() {
    override fun ytelse(): Ytelse = Ytelse.PLEIEPENGER_SYKT_BARN
    override fun pdfData(): Map<String, Any?> = mapOf(
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
        "omsorgstilbud" to søknad.omsorgstilbud?.somMap(),
        "nattevaak" to nattevåk(søknad.nattevåk),
        "beredskap" to beredskap(søknad.beredskap),
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

    private fun Barn.somMap() = mapOf<String, Any?>(
        "manglerNorskIdentitetsnummer" to (fødselsnummer == null),
        "norskIdentitetsnummer" to fødselsnummer,
        "navn" to navn.storForbokstav(),
        "fødselsdato" to if (fødselsdato != null) Constants.DATE_FORMATTER.format(fødselsdato) else null,
        "årsakManglerIdentitetsnummer" to årsakManglerIdentitetsnummer?.pdfTekst
    )

    private fun PSBMottattSøknad.harMinstEtArbeidsforhold(): Boolean {
        if (frilans.arbeidsforhold != null) return true

        if (selvstendigNæringsdrivende.arbeidsforhold != null) return true

        if (arbeidsgivere.any() { it.arbeidsforhold != null }) return true

        return false
    }

    private fun PSBMottattSøknad.harFlereAktiveVirksomehterSatt() =
        (this.selvstendigNæringsdrivende.virksomhet?.harFlereAktiveVirksomheter != null)

    private fun erBooleanSatt(verdi: Boolean?) = verdi != null

    private fun nattevåk(nattevaak: Nattevåk?) = when {
        nattevaak == null -> null
        else -> {
            mapOf(
                "har_nattevaak" to nattevaak.harNattevåk,
                "tilleggsinformasjon" to nattevaak.tilleggsinformasjon
            )
        }
    }

    private fun beredskap(beredskap: Beredskap?) = when {
        beredskap == null -> null
        else -> {
            mapOf(
                "i_beredskap" to beredskap.beredskap,
                "tilleggsinformasjon" to beredskap.tilleggsinformasjon
            )
        }
    }

    private fun Omsorgstilbud.somMap(): Map<String, Any?> {
        return mapOf(
            "svarFortid" to svarFortid?.pdfTekst,
            "svarFremtid" to svarFremtid?.pdfTekst,
            "erLiktHverUkeErSatt" to (erLiktHverUke != null),
            "erLiktHverUke" to erLiktHverUke,
            "enkeltdagerPerMnd" to enkeltdager?.somMapPerMnd(),
            "ukedager" to ukedager?.somMap()
        )
    }

    private fun List<Enkeltdag>.somMapEnkeltdag(): List<Map<String, Any?>> {
        return map {
            mapOf<String, Any?>(
                "dato" to Constants.DATE_FORMATTER.format(it.dato),
                "dag" to it.dato.dayOfWeek.somNorskDag(),
                "tid" to it.tid.somTekst(avkort = false)
            )
        }
    }

    fun List<Enkeltdag>.somMapPerMnd(): List<Map<String, Any>> {
        val omsorgsdagerPerMnd = this.groupBy { it.dato.month }

        return omsorgsdagerPerMnd.map {
            mapOf(
                "år" to it.value.first().dato.year,
                "måned" to it.key.somNorskMåned().storForbokstav(),
                "enkeltdagerPerUke" to it.value.somMapPerUke()
            )
        }
    }

    private fun List<Enkeltdag>.somMapPerUke(): List<Map<String, Any>> {
        val omsorgsdagerPerUke = this.groupBy {
            val uketall = it.dato.get(WeekFields.of(DayOfWeek.MONDAY, 7).weekOfYear())
            if (uketall == 0) 53 else uketall
        }
        return omsorgsdagerPerUke.map {
            mapOf(
                "uke" to it.key,
                "dager" to it.value.somMapEnkeltdag()
            )
        }
    }

    private fun PlanUkedager.somMap(avkort: Boolean = true) = mapOf<String, Any?>(
        "mandag" to if (mandag.harGyldigVerdi()) mandag!!.somTekst(avkort) else null,
        "tirsdag" to if (tirsdag.harGyldigVerdi()) tirsdag!!.somTekst(avkort) else null,
        "onsdag" to if (onsdag.harGyldigVerdi()) onsdag!!.somTekst(avkort) else null,
        "torsdag" to if (torsdag.harGyldigVerdi()) torsdag!!.somTekst(avkort) else null,
        "fredag" to if (fredag.harGyldigVerdi()) fredag!!.somTekst(avkort) else null,
    )

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

    private fun Duration?.harGyldigVerdi() = this != null && this != Duration.ZERO

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
            mapOf(
                "uke" to it.periode.fraOgMed.get(WeekFields.of(DayOfWeek.MONDAY, 7).weekOfYear()),
                "faktiskTimerPerUke" to it.timer?.tilString()
            )
        }

    private fun NormalArbeidstid.somMap(): Map<String, Any?> = mapOf(
        "timerPerUkeISnitt" to this.timerPerUkeISnitt.tilString()
    )

    private fun Frilans.somMap(søknadsperiodeStartdato: LocalDate): Map<String, Any?> = mapOf(
        "harInntektSomFrilanser" to harInntektSomFrilanser,
        "startetFørOpptjeningsperiode" to startetFørOpptjeningsperiode,
        "opptjeningsperiodeStartdato" to Constants.DATE_FORMATTER.format(søknadsperiodeStartdato.minusDays(28)),
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

    private fun PSBMottattSøknad.sjekkOmHarIkkeVedlegg(): Boolean = vedleggId.isEmpty()
    private fun StønadGodtgjørelse.somMap() = mapOf(
        "mottarStønadGodtgjørelse" to mottarStønadGodtgjørelse,
        "startdato" to startdato?.let { Constants.DATE_FORMATTER.format(it) },
        "startetIPerioden" to (startdato != null),
        "sluttdato" to sluttdato?.let { Constants.DATE_FORMATTER.format(it) },
        "sluttetIPerioden" to (sluttdato != null),
    )
}
