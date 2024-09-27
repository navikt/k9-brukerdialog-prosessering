package no.nav.brukerdialog.ytelse.ungdomsytelse.pdf

import no.nav.brukerdialog.common.Constants.DATE_FORMATTER
import no.nav.brukerdialog.common.Constants.DATE_TIME_FORMATTER
import no.nav.brukerdialog.common.Constants.OSLO_ZONE_ID
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.pdf.PdfData
import no.nav.brukerdialog.utils.DateUtils.somNorskDag
import no.nav.brukerdialog.utils.StringUtils.språkTilTekst
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.domene.UngdomsytelsesøknadMottatt

class UngdomsytelsesøknadPdfData(private val søknad: UngdomsytelsesøknadMottatt) : PdfData() {
    override fun ytelse(): Ytelse = Ytelse.UNGDOMSYTELSE

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
        "samtykke" to mapOf(
            "harForståttRettigheterOgPlikter" to søknad.harForståttRettigheterOgPlikter,
            "harBekreftetOpplysninger" to søknad.harBekreftetOpplysninger
        ),
        "hjelp" to mapOf(
            "språk" to søknad.språk?.språkTilTekst()
        )
    )
}
