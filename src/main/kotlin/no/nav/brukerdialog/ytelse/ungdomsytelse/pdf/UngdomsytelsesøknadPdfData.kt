package no.nav.brukerdialog.ytelse.ungdomsytelse.pdf

import no.nav.brukerdialog.common.Constants.DATE_FORMATTER
import no.nav.brukerdialog.common.Constants.DATE_TIME_FORMATTER
import no.nav.brukerdialog.common.Constants.OSLO_ZONE_ID
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.pdf.PdfData
import no.nav.brukerdialog.utils.DateUtils.somNorskDag
import no.nav.brukerdialog.utils.StringUtils.språkTilTekst
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.domene.UngdomsytelsesøknadMottatt
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.felles.type.Språk
import no.nav.k9.søknad.ytelse.ung.v1.OppgittInntekt
import no.nav.k9.søknad.ytelse.ung.v1.UngSøknadstype
import no.nav.k9.søknad.ytelse.ung.v1.Ungdomsytelse
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

class UngdomsytelsesøknadPdfData(private val søknad: UngdomsytelsesøknadMottatt) : PdfData() {
    override fun ytelse(): Ytelse = when (søknad.søknadstype) {
        UngSøknadstype.DELTAKELSE_SØKNAD -> {
            Ytelse.UNGDOMSYTELSE_DELTAKELSE_SØKNAD
        }
        else -> {
            Ytelse.UNGDOMSYTELSE_RAPPORTERING_SØKNAD
        }
    }

    override fun språk(): Språk = Språk.NORSK_BOKMÅL

    override fun pdfData(): Map<String, Any?> {
        return when(søknad.søknadstype) {
            UngSøknadstype.DELTAKELSE_SØKNAD -> deltakelseSøknadPdfData()

            UngSøknadstype.RAPPORTERING_SØKNAD -> rapporteringSøknadPdfData()
        }
    }

    private fun deltakelseSøknadPdfData(): Map<String, Any?> = mapOf(
        "tittel" to ytelse().utledTittel(språk()),
        "søknadstype" to søknad.søknadstype.name,
        "søknadId" to søknad.søknadId,
        "søknadMottattDag" to søknad.mottatt.withZoneSameInstant(OSLO_ZONE_ID).somNorskDag(),
        "søknadMottatt" to DATE_TIME_FORMATTER.format(søknad.mottatt),
        "startdato" to DATE_FORMATTER.format(søknad.startdato),
        "søker" to søknad.søker.somMap(),
        "samtykke" to mapOf(
            "harForståttRettigheterOgPlikter" to søknad.harForståttRettigheterOgPlikter,
            "harBekreftetOpplysninger" to søknad.harBekreftetOpplysninger
        ),
        "hjelp" to mapOf(
            "språk" to søknad.språk?.språkTilTekst()
        )
    )

    private fun rapporteringSøknadPdfData(): Map<String, Any?> = mapOf(
        "tittel" to ytelse().utledTittel(språk()),
        "søknadstype" to søknad.søknadstype.name,
        "søknadId" to søknad.søknadId,
        "søknadMottattDag" to søknad.mottatt.withZoneSameInstant(OSLO_ZONE_ID).somNorskDag(),
        "søknadMottatt" to DATE_TIME_FORMATTER.format(søknad.mottatt),
        "inntektForPeriode" to søknad.k9Format.getYtelse<Ungdomsytelse>().inntekter?.somMap(),
        "søker" to søknad.søker.somMap(),
        "hjelp" to mapOf(
            "språk" to søknad.språk?.språkTilTekst()
        )
    )

    fun BigDecimal.formaterSomValuta(): String {
        val valutaFormat = NumberFormat.getCurrencyInstance(Locale.of("no", "NO"))
        return valutaFormat.format(this)
    }

    private fun OppgittInntekt.somMap(): List<Map<String, Any?>> = oppgittePeriodeinntekter.map {
            mapOf<String, Any?>(
                "periode" to it.periode.somMap(),
                "arbeidstakerOgFrilansInntekt" to it.arbeidstakerOgFrilansInntekt.formaterSomValuta(),
                "næringsinntekt" to it.næringsinntekt,
                "ytelse" to it.ytelse
            )
    }

    private fun Periode.somMap() = mapOf(
        "fraOgMed" to DATE_FORMATTER.format(fraOgMed),
        "tilOgMed" to DATE_FORMATTER.format(tilOgMed)
    )
}
