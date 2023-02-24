package no.nav.k9brukerdialogprosessering.endringsmelding

import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene.felles.Søker
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarn
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstaker
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstid
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import no.nav.k9.søknad.ytelse.psb.v1.tilsyn.TilsynPeriodeInfo
import no.nav.k9brukerdialogprosessering.common.Constants.DATE_FORMATTER
import no.nav.k9brukerdialogprosessering.common.Constants.DATE_TIME_FORMATTER
import no.nav.k9brukerdialogprosessering.common.Constants.ZONE_ID
import no.nav.k9brukerdialogprosessering.common.Ytelse
import no.nav.k9brukerdialogprosessering.endringsmelding.domene.PSBEndringsmeldingMottatt
import no.nav.k9brukerdialogprosessering.pdf.PdfData
import no.nav.k9brukerdialogprosessering.utils.DurationUtils.somTekst
import no.nav.k9brukerdialogprosessering.utils.DurationUtils.timer
import no.nav.k9brukerdialogprosessering.utils.somNorskDag
import java.time.DayOfWeek
import java.time.Duration
import java.time.temporal.WeekFields

class PSBEndringsmeldingPdfData(private val endringsmelding: PSBEndringsmeldingMottatt) : PdfData() {
    override fun ytelse(): Ytelse = Ytelse.PLEIEPENGER_SYKT_BARN_ENDRINGSMELDING
    override fun pdfData(): Map<String, Any?> {

        val k9Format = endringsmelding.k9Format
        val ytelse = k9Format.getYtelse<PleiepengerSyktBarn>()
        return mapOf(
            "søknadId" to k9Format.søknadId.id,
            "soknadDialogCommitSha" to ytelse.søknadInfo.orElse(null)?.soknadDialogCommitSha,
            "mottattDag" to k9Format.mottattDato.withZoneSameInstant(ZONE_ID).somNorskDag(),
            "mottattDato" to DATE_TIME_FORMATTER.format(k9Format.mottattDato),
            "soker" to mapOf(
                "navn" to endringsmelding.søker.formatertNavn().capitalizeName(),
                "fødselsnummer" to endringsmelding.søker.fødselsnummer
            ),
            "barn" to ytelse.barn.somMap(),
            "arbeidstid" to when {
                ytelse.arbeidstid != null -> ytelse.arbeidstid.somMap()
                else -> null
            },
            "samtykke" to mapOf(
                "har_forstatt_rettigheter_og_plikter" to endringsmelding.harForståttRettigheterOgPlikter,
                "har_bekreftet_opplysninger" to endringsmelding.harBekreftetOpplysninger
            )
        )
    }

    fun Søker.formatertNavn() = if (mellomnavn != null) "$fornavn $mellomnavn $etternavn" else "$fornavn $etternavn"
    fun String.capitalizeName(): String = split(" ").joinToString(" ") { name: String ->
        name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    @JvmName("somMapPeriodeTilsynPeriodeInfo")
    private fun MutableMap<no.nav.k9.søknad.felles.type.Periode, TilsynPeriodeInfo>.somMap(): List<Map<String, Any?>> = map { entry ->
        mapOf(
            "periode" to entry.key.somMap(),
            "tilsynPeriodeInfo" to entry.value.somMap()
        )
    }

    private fun TilsynPeriodeInfo.somMap(): Map<String, Any?> = mapOf(
        "etablertTilsynTimerPerDag" to etablertTilsynTimerPerDag.somTekst()
    )

    private fun no.nav.k9.søknad.felles.personopplysninger.Barn.somMap(): Map<String, Any?> = mapOf(
        "fødselsnummer" to personIdent.verdi
    )

    fun Arbeidstid.somMap(): Map<String, Any?> = mapOf(
        "arbeidstakerList" to when {
            !arbeidstakerList.isNullOrEmpty() -> arbeidstakerList.somMap()
            else -> null
        },
        "frilanserArbeidstidInfo" to when {
            frilanserArbeidstidInfo.isPresent -> frilanserArbeidstidInfo.get().somMap()
            else -> null
        },
        "selvstendigNæringsdrivendeArbeidstidInfo" to when {
            selvstendigNæringsdrivendeArbeidstidInfo.isPresent -> selvstendigNæringsdrivendeArbeidstidInfo.get().somMap()
            else -> null
        }
    )

    fun List<Arbeidstaker>.somMap(): List<Map<String, Any?>> = map { arbeidstaker ->
        mapOf(
            "organisasjonsnummer" to arbeidstaker.organisasjonsnummer.verdi,
            "arbeidstidInfo" to arbeidstaker.arbeidstidInfo.somMap()
        )
    }

    fun ArbeidstidInfo.somMap(): Map<String, Any?> = mapOf(
        "perioder" to perioder.toSortedMap { p1, p2 -> p1.compareTo(p2) }.somMap()
    )

    fun MutableMap<no.nav.k9.søknad.felles.type.Periode, ArbeidstidPeriodeInfo>.somMap(): List<Map<String, Any?>> = map { entry ->
        mapOf(
            "periode" to entry.key.somMap(),
            "arbeidstidPeriodeInfo" to entry.value.somMap()
        )
    }

    fun no.nav.k9.søknad.felles.type.Periode.uke(): Int = fraOgMed.get(WeekFields.of(DayOfWeek.MONDAY, 7).weekOfYear())

    fun no.nav.k9.søknad.felles.type.Periode.somMap(): Map<String, Any?> = mutableMapOf(
        "uke" to uke(),
        "fraOgMed" to DATE_FORMATTER.format(fraOgMed),
        "tilOgMed" to DATE_FORMATTER.format(tilOgMed)
    )

    fun ArbeidstidPeriodeInfo.somMap(): Map<String, Any?> = mutableMapOf(
        "jobberNormaltTimerPerUke" to jobberNormaltTimerPerDag.tilTimerPerUke().formaterString(),
        "faktiskArbeidTimerPerUke" to faktiskArbeidTimerPerDag.tilTimerPerUke().formaterString()
    )

    private fun Duration.formaterString(): String {
        return "${timer()}t. ${toMinutesPart()}m."
    }

    private fun Duration.tilTimerPerUke(): Duration = this.multipliedBy(5)

}
