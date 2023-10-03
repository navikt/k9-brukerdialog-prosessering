package no.nav.k9brukerdialogprosessering.meldinger.endringsmelding

import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.LovbestemtFerie
import no.nav.k9.søknad.ytelse.psb.v1.NormalArbeidstid
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarn
import no.nav.k9.søknad.ytelse.psb.v1.UkjentArbeidsforhold
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstaker
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstid
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import no.nav.k9.søknad.ytelse.psb.v1.tilsyn.TilsynPeriodeInfo
import no.nav.k9brukerdialogprosessering.common.Constants.DATE_FORMATTER
import no.nav.k9brukerdialogprosessering.common.Constants.DATE_TIME_FORMATTER
import no.nav.k9brukerdialogprosessering.common.Constants.OSLO_ZONE_ID
import no.nav.k9brukerdialogprosessering.common.Ytelse
import no.nav.k9brukerdialogprosessering.meldinger.endringsmelding.domene.PSBEndringsmeldingMottatt
import no.nav.k9brukerdialogprosessering.pdf.PdfData
import no.nav.k9brukerdialogprosessering.utils.DurationUtils.somTekst
import no.nav.k9brukerdialogprosessering.utils.somNorskDag
import java.time.DayOfWeek
import java.time.Duration
import java.time.ZoneOffset.UTC
import java.time.temporal.WeekFields

class PSBEndringsmeldingPdfData(private val endringsmelding: PSBEndringsmeldingMottatt) : PdfData() {
    override fun ytelse(): Ytelse = Ytelse.PLEIEPENGER_SYKT_BARN_ENDRINGSMELDING
    override fun pdfData(): Map<String, Any?> {

        val k9Format = endringsmelding.k9Format
        val ytelse = k9Format.getYtelse<PleiepengerSyktBarn>()
        val dataBruktTilUtledning = ytelse.søknadInfo.orElse(null)
        return mapOf(
            "tittel" to ytelse().tittel,
            "søknadId" to k9Format.søknadId.id,
            "soknadDialogCommitSha" to dataBruktTilUtledning?.soknadDialogCommitSha,
            "mottattDag" to k9Format.mottattDato.withZoneSameInstant(OSLO_ZONE_ID).somNorskDag(),
            "mottattDato" to DATE_TIME_FORMATTER.format(k9Format.mottattDato),
            "soker" to endringsmelding.søker.somMap(),
            "barn" to ytelse.barn.somMap(endringsmelding.pleietrengendeNavn),
            "ukjenteArbeidsforhold" to when {
                dataBruktTilUtledning?.ukjenteArbeidsforhold?.isNotEmpty() == true -> dataBruktTilUtledning.ukjenteArbeidsforhold.somMap()
                else -> null
            },
            "arbeidstid" to when {
                ytelse.arbeidstid != null -> ytelse.arbeidstid.somMap(dataBruktTilUtledning.ukjenteArbeidsforhold)
                else -> null
            },
            "lovbestemtFerie" to when {
                ytelse.lovbestemtFerie != null && ytelse.lovbestemtFerie.perioder.isNotEmpty() -> ytelse.lovbestemtFerie.somMap()
                else -> null
            },
            "samtykke" to mapOf(
                "har_forstatt_rettigheter_og_plikter" to endringsmelding.harForståttRettigheterOgPlikter,
                "har_bekreftet_opplysninger" to endringsmelding.harBekreftetOpplysninger
            )
        )
    }

    @JvmName("somMapPeriodeTilsynPeriodeInfo")
    private fun MutableMap<Periode, TilsynPeriodeInfo>.somMap(): List<Map<String, Any?>> = map { entry ->
        mapOf(
            "periode" to entry.key.somMap(),
            "tilsynPeriodeInfo" to entry.value.somMap()
        )
    }

    private fun TilsynPeriodeInfo.somMap(): Map<String, Any?> = mapOf(
        "etablertTilsynTimerPerDag" to etablertTilsynTimerPerDag.somTekst()
    )

    private fun no.nav.k9.søknad.felles.personopplysninger.Barn.somMap(pleietrengendeNavn: String): Map<String, Any?> =
        mapOf(
            "navn" to pleietrengendeNavn,
            "fødselsnummer" to personIdent.verdi
        )

    @JvmName("ukjentArbeidsforholdSomMap")
    private fun List<UkjentArbeidsforhold>.somMap(): List<Map<String, Any?>> = map { it.somMap() }
    private fun UkjentArbeidsforhold.somMap(): Map<String, Any?> = mapOf(
        "organisasjonsnummer" to organisasjonsnummer.verdi,
        "organisasjonsnavn" to organisasjonsnavn,
        "erAnsatt" to isErAnsatt,
        "arbeiderIPerioden" to arbeiderIPerioden?.name,
        "normalarbeidstid" to normalarbeidstid?.somMap()
    )

    private fun NormalArbeidstid.somMap(): Map<String, Any> = mapOf(
        "timerPerUke" to timerPerUke.formaterString()
    )

    fun Arbeidstid.somMap(ukjenteArbeidsforhold: List<UkjentArbeidsforhold>?): Map<String, Any?> = mapOf(
        "arbeidstakerList" to when {
            !arbeidstakerList.isNullOrEmpty() -> arbeidstakerList.somMap(ukjenteArbeidsforhold)
            else -> null
        },
        "frilanserArbeidstidInfo" to when {
            frilanserArbeidstidInfo.isPresent -> frilanserArbeidstidInfo.get().somMap()
            else -> null
        },
        "selvstendigNæringsdrivendeArbeidstidInfo" to when {
            selvstendigNæringsdrivendeArbeidstidInfo.isPresent -> selvstendigNæringsdrivendeArbeidstidInfo.get()
                .somMap()

            else -> null
        }
    )

    fun List<Arbeidstaker>.somMap(ukjenteArbeidsforhold: List<UkjentArbeidsforhold>?): List<Map<String, Any?>> =
        map { arbeidstaker ->
            val ukjentArbeidsforhold =
                ukjenteArbeidsforhold?.find { it.organisasjonsnummer.verdi == arbeidstaker.organisasjonsnummer.verdi }
            mapOf(
                "organisasjonsnummer" to arbeidstaker.organisasjonsnummer.verdi,
                "organisasjonsnavn" to arbeidstaker.organisasjonsnavn,
                "arbeidstidInfo" to arbeidstaker.arbeidstidInfo.somMap(),
                "ukjentArbeidsforhold" to ukjentArbeidsforhold?.somMap()
            )
        }

    fun ArbeidstidInfo.somMap(): Map<String, Any?> = mapOf(
        "perioder" to perioder.toSortedMap { p1, p2 -> p1.compareTo(p2) }.somMap()
    )

    fun Map<Periode, ArbeidstidPeriodeInfo>.somMap() = map { entry ->
        mapOf(
            "periode" to entry.key.somMap(),
            "arbeidstidPeriodeInfo" to entry.value.somMap()
        )
    }

    fun Periode.uke(): Int = fraOgMed.get(WeekFields.of(DayOfWeek.MONDAY, 7).weekOfYear())

    fun Periode.somMap(): Map<String, Any?> = mutableMapOf(
        "uke" to uke(),
        "fraOgMedDag" to fraOgMed.atStartOfDay(UTC).somNorskDag(),
        "fraOgMed" to DATE_FORMATTER.format(fraOgMed),
        "tilOgMedDag" to tilOgMed.atStartOfDay(UTC).somNorskDag(),
        "tilOgMed" to DATE_FORMATTER.format(tilOgMed)
    )

    fun ArbeidstidPeriodeInfo.somMap(): Map<String, Any?> = mutableMapOf(
        "jobberNormaltTimerPerUke" to jobberNormaltTimerPerDag.tilTimerPerUke().formaterString(),
        "faktiskArbeidTimerPerUke" to faktiskArbeidTimerPerDag.tilTimerPerUke().formaterString()
    )

    private fun LovbestemtFerie.somMap(): Map<String, Any> {
        val perioderLagtTil = perioder.toSortedMap { p1, p2 -> p1.compareTo(p2) }.filter { it.value.isSkalHaFerie }
        val perioderFjernet = perioder.toSortedMap { p1, p2 -> p1.compareTo(p2) }.filter { !it.value.isSkalHaFerie }
        return mapOf(
            "harPerioderLagtTil" to perioderLagtTil.isNotEmpty(),
            "perioderLagtTil" to perioderLagtTil.somMap(),
            "harPerioderFjernet" to perioderFjernet.isNotEmpty(),
            "perioderFjernet" to perioderFjernet.somMap()
        )
    }

    @JvmName("somMapPeriodeLovbestemtFeriePeriodeInfo")
    private fun Map<Periode, LovbestemtFerie.LovbestemtFeriePeriodeInfo>.somMap() = map {
        mapOf("periode" to it.key.somMap(), "lovbestemtFeriePeriodeInfo" to it.value.somMap())
    }

    private fun LovbestemtFerie.LovbestemtFeriePeriodeInfo.somMap() = mutableMapOf("skalHaFerie" to isSkalHaFerie)

    private fun Duration.formaterString(): String {
        val totalMinutes = toMinutes()
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        val totalT = hours + (minutes.toDouble() / 60.0)
        return "${String.format("%.1f", totalT)}t (${hours}t. ${minutes}m.)"
    }

    private fun Duration.tilTimerPerUke(): Duration = this.multipliedBy(5)

}
