package no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase

import no.nav.k9brukerdialogprosessering.common.Constants.DATE_FORMATTER
import no.nav.k9brukerdialogprosessering.common.Constants.DATE_TIME_FORMATTER
import no.nav.k9brukerdialogprosessering.common.Constants.OSLO_ZONE_ID
import no.nav.k9brukerdialogprosessering.common.Ytelse
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.ArbeidIPeriode
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Arbeidsforhold
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Arbeidsgiver
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Enkeltdag
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Ferieuttak
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Frilans
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Land
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Medlemskap
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Opphold
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.OpptjeningIUtlandet
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.PilsSøknadMottatt
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Pleietrengende
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Regnskapsfører
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.SelvstendigNæringsdrivende
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.UtenlandskNæring
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Utenlandsopphold
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.VarigEndring
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.Virksomhet
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.YrkesaktivSisteTreFerdigliknedeArene
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.domene.capitalizeName
import no.nav.k9brukerdialogprosessering.pdf.PdfData
import no.nav.k9brukerdialogprosessering.utils.DurationUtils.somTekst
import no.nav.k9brukerdialogprosessering.utils.StringUtils.språkTilTekst
import no.nav.k9brukerdialogprosessering.utils.somNorskDag
import no.nav.k9brukerdialogprosessering.utils.somNorskMåned
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*

class PilsSøknadPdfData(private val søknad: PilsSøknadMottatt): PdfData() {
    override fun ytelse(): Ytelse = Ytelse.PLEIEPENGER_LIVETS_SLUTTFASE

    override fun pdfData(): Map<String, Any?> = mapOf(
        "tittel" to ytelse().tittel,
        "søknadId" to søknad.søknadId,
        "søknadMottattDag" to søknad.mottatt.withZoneSameInstant(OSLO_ZONE_ID).somNorskDag(),
        "søknadMottatt" to DATE_TIME_FORMATTER.format(søknad.mottatt),
        "periode" to mapOf(
            "fraOgMed" to DATE_FORMATTER.format(søknad.fraOgMed),
            "tilOgMed" to DATE_FORMATTER.format(søknad.tilOgMed)
        ),
        "søker" to søknad.søker.somMap(),
        "pleietrengende" to søknad.pleietrengende.somMap(),
        "medlemskap" to søknad.medlemskap.somMap(),
        "utenlandsoppholdIPerioden" to mapOf(
            "skalOppholdeSegIUtlandetIPerioden" to søknad.utenlandsoppholdIPerioden.skalOppholdeSegIUtlandetIPerioden,
            "opphold" to søknad.utenlandsoppholdIPerioden.opphold.somMapUtenlandsopphold()
        ),
        "ferieuttakIPerioden" to mapOf(
            "skalTaUtFerieIPerioden" to søknad.ferieuttakIPerioden?.skalTaUtFerieIPerioden,
            "ferieuttak" to søknad.ferieuttakIPerioden?.ferieuttak?.somMapFerieuttak()
        ),
        "harLastetOppId" to søknad.opplastetIdVedleggId.isNotEmpty(),
        "harLastetOppLegeerklæring" to søknad.vedleggId.isNotEmpty(),
        "arbeidsgivere" to søknad.arbeidsgivere.somMapAnsatt(),
        "frilans" to søknad.frilans?.somMap(),
        "selvstendigNæringsdrivende" to søknad.selvstendigNæringsdrivende?.somMap(),
        "opptjeningIUtlandet" to søknad.opptjeningIUtlandet.somMap(),
        "utenlandskNæring" to søknad.utenlandskNæring.somMapUtenlandskNæring(),
        "harVærtEllerErVernepliktig" to søknad.harVærtEllerErVernepliktig,
        "samtykke" to mapOf(
            "harForståttRettigheterOgPlikter" to søknad.harForståttRettigheterOgPlikter,
            "harBekreftetOpplysninger" to søknad.harBekreftetOpplysninger
        ),
        "flereSokere" to søknad.flereSokere?.name,
        "hjelp" to mapOf(
            "språk" to søknad.språk?.språkTilTekst(),
            "ingen_arbeidsgivere" to søknad.arbeidsgivere.isEmpty(),
            "harFlereAktiveVirksomheterErSatt" to søknad.harFlereAktiveVirksomehterSatt(),
            "ingen_arbeidsforhold" to !søknad.harMinstEtArbeidsforhold(),
            "harVærtEllerErVernepliktigErSatt" to (søknad.harVærtEllerErVernepliktig != null)
        )
    )

    private fun Pleietrengende.somMap() = mapOf<String, Any?>(
        "manglerNorskIdentitetsnummer" to (norskIdentitetsnummer == null),
        "norskIdentitetsnummer" to norskIdentitetsnummer,
        "fødselsdato" to if(fødselsdato != null) DATE_FORMATTER.format(fødselsdato) else null,
        "årsakManglerIdentitetsnummer" to årsakManglerIdentitetsnummer?.pdfTekst,
        "navn" to navn
    )

    private fun Medlemskap.somMap() = mapOf<String, Any?>(
        "data" to this,
        "harBoddIUtlandetSiste12Mnd" to this.harBoddIUtlandetSiste12Mnd,
        "utenlandsoppholdSiste12Mnd" to this.utenlandsoppholdSiste12Mnd.somMapOpphold(),
        "skalBoIUtlandetNeste12Mnd" to this.skalBoIUtlandetNeste12Mnd,
        "utenlandsoppholdNeste12Mnd" to this.utenlandsoppholdNeste12Mnd.somMapOpphold()
    )

    private fun List<Opphold>.somMapOpphold(): List<Map<String, Any?>> {
        return map {
            mapOf(
                "landnavn" to it.landnavn,
                "fraOgMed" to DATE_FORMATTER.format(it.fraOgMed),
                "tilOgMed" to DATE_FORMATTER.format(it.tilOgMed)
            )
        }
    }

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

    private fun PilsSøknadMottatt.harMinstEtArbeidsforhold() : Boolean{
        frilans?.let {
            if(it.arbeidsforhold != null) return true
        }

        selvstendigNæringsdrivende?.let {
            if(it.arbeidsforhold != null) return true
        }

        if(arbeidsgivere.any(){it.arbeidsforhold != null}) return true

        return false
    }

    private fun Arbeidsforhold.somMap(): Map<String, Any?> = mapOf(
        "jobberNormaltTimer" to jobberNormaltTimer,
        "arbeidIPeriode" to arbeidIPeriode.somMap()
    )

    private fun ArbeidIPeriode.somMap(): Map<String, Any?> = mapOf(
        "jobberIPerioden" to jobberIPerioden.tilBoolean(),
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
        val perUke = this.groupBy {
            val uketall = it.dato.get(WeekFields.of(Locale.getDefault()).weekOfYear())
            if (uketall == 0) 53 else uketall
        }
        return perUke.map {
            mapOf(
                "uke" to it.key,
                "dager" to it.value.somMapEnkeltdag()
            )
        }
    }

    fun List<Enkeltdag>.somMapPerMnd(): List<Map<String, Any>> {
        val perMåned: Map<Month, List<Enkeltdag>> = this.groupBy { it.dato.month }

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


    private fun PilsSøknadMottatt.harFlereAktiveVirksomehterSatt() =
        (this.selvstendigNæringsdrivende?.virksomhet?.harFlereAktiveVirksomheter != null)

    private fun Virksomhet.somMap(): Map<String, Any?> = mapOf(
        "næringstype" to næringstype.beskrivelse,
        "næringsinntekt" to næringsinntekt,
        "yrkesaktivSisteTreFerdigliknedeÅrene" to yrkesaktivSisteTreFerdigliknedeÅrene?.somMap(),
        "varigEndring" to varigEndring?.somMap(),
        "harFlereAktiveVirksomheter" to harFlereAktiveVirksomheter,
        "navnPåVirksomheten" to navnPåVirksomheten,
        "fraOgMed" to DATE_FORMATTER.format(fraOgMed),
        "tilOgMed" to if (tilOgMed != null) DATE_FORMATTER.format(tilOgMed) else null,
        "fiskerErPåBladB" to fiskerErPåBladB,
        "registrertINorge" to registrertINorge,
        "organisasjonsnummer" to organisasjonsnummer,
        "registrertIUtlandet" to registrertIUtlandet?.somMap(),
        "regnskapsfører" to regnskapsfører?.somMap()
    )

    private fun List<OpptjeningIUtlandet>.somMap(): List<Map<String, Any?>>? {
        if(isEmpty()) return null
        return map {
            mapOf<String, Any?>(
                "navn" to it.navn,
                "land" to it.land.somMap(),
                "opptjeningType" to it.opptjeningType.pdfTekst,
                "fraOgMed" to DATE_FORMATTER.format(it.fraOgMed),
                "tilOgMed" to DATE_FORMATTER.format(it.tilOgMed)
            )
        }
    }

    private fun List<UtenlandskNæring>.somMapUtenlandskNæring(): List<Map<String, Any?>>? {
        if(isEmpty()) return null
        return map {
            mapOf(
                "næringstype" to it.næringstype.beskrivelse,
                "navnPåVirksomheten" to it.navnPåVirksomheten,
                "land" to it.land.somMap(),
                "organisasjonsnummer" to it.organisasjonsnummer,
                "fraOgMed" to DATE_FORMATTER.format(it.fraOgMed),
                "tilOgMed" to if(it.tilOgMed != null) DATE_FORMATTER.format(it.tilOgMed) else null
            )
        }
    }

    private fun Regnskapsfører.somMap() = mapOf<String, Any?>(
        "navn" to navn,
        "telefon" to telefon
    )

    private fun YrkesaktivSisteTreFerdigliknedeArene.somMap() = mapOf<String, Any?>(
        "oppstartsdato" to DATE_FORMATTER.format(oppstartsdato)
    )

    private fun Land.somMap() = mapOf<String, Any?>(
        "landnavn" to landnavn,
        "landkode" to landkode
    )

    private fun VarigEndring.somMap() = mapOf<String, Any?>(
        "dato" to DATE_FORMATTER.format(dato),
        "inntektEtterEndring" to inntektEtterEndring,
        "forklaring" to forklaring
    )

    private fun List<Ferieuttak>.somMapFerieuttak(): List<Map<String, Any?>> {
        return map {
            mapOf<String, Any?>(
                "fraOgMed" to DATE_FORMATTER.format(it.fraOgMed),
                "tilOgMed" to DATE_FORMATTER.format(it.tilOgMed)
            )
        }
    }
}
