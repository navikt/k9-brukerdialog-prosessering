package no.nav.brukerdialog.ytelse.ungdomsytelse.pdf

import no.nav.brukerdialog.common.Constants.DATE_FORMATTER
import no.nav.brukerdialog.common.Constants.DATE_TIME_FORMATTER
import no.nav.brukerdialog.common.Constants.OSLO_ZONE_ID
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.pdf.PdfData
import no.nav.brukerdialog.utils.DateUtils.somNorskDag
import no.nav.brukerdialog.utils.StringUtils.språkTilTekst
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.inntektsrapportering.domene.UngdomsytelseInntektsrapporteringMottatt
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.felles.type.Språk
import no.nav.k9.søknad.ytelse.ung.v1.inntekt.OppgittInntekt
import no.nav.k9.søknad.ytelse.ung.v1.Ungdomsytelse
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

class UngdomsytelseInntektsrapporteringPdfData(private val søknad: UngdomsytelseInntektsrapporteringMottatt) : PdfData() {
    override fun ytelse(): Ytelse = Ytelse.UNGDOMSYTELSE_INNTEKTSRAPPORTERING

    override fun språk(): Språk = Språk.NORSK_BOKMÅL

    override fun pdfData(): Map<String, Any?> {
        val k9Format = søknad.k9Format
        return mapOf(
            "tittel" to ytelse().utledTittel(språk()),
            "søknadId" to søknad.søknadId,
            "søknadMottattDag" to søknad.mottatt.withZoneSameInstant(OSLO_ZONE_ID).somNorskDag(),
            "søknadMottatt" to DATE_TIME_FORMATTER.format(søknad.mottatt),
            "inntektForPeriode" to k9Format.getYtelse<Ungdomsytelse>().inntekter?.somMap(),
            "søker" to søknad.søker.somMap(),
            "hjelp" to mapOf(
                "språk" to k9Format.språk.kode?.språkTilTekst()
            )
        )
    }

    fun BigDecimal.formaterSomValuta(): String {
        val valutaFormat = NumberFormat.getCurrencyInstance(Locale.of("no", "NO"))
        return valutaFormat.format(this)
    }

    private fun OppgittInntekt.somMap(): List<Map<String, Any?>> = oppgittePeriodeinntekter.map {
            mapOf<String, Any?>(
                "periode" to it.periode.somMap(),
                "arbeidstakerOgFrilansInntekt" to it.arbeidstakerOgFrilansInntekt?.formaterSomValuta(),
                "næringsinntekt" to it.næringsinntekt?.formaterSomValuta(),
                "ytelse" to it.ytelse?.formaterSomValuta()
            )
    }

    private fun Periode.somMap() = mapOf(
        "fraOgMed" to DATE_FORMATTER.format(fraOgMed),
        "tilOgMed" to DATE_FORMATTER.format(tilOgMed)
    )
}
