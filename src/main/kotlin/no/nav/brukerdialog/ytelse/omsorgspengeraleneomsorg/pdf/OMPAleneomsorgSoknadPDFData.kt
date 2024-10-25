package no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.pdf

import no.nav.brukerdialog.common.Constants.DATE_TIME_FORMATTER
import no.nav.brukerdialog.common.Constants.OSLO_ZONE_ID
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.meldinger.omsorgspengeraleneomsorg.domene.OMPAleneomsorgSoknadMottatt
import no.nav.brukerdialog.meldinger.omsorgspengeraleneomsorg.domene.somMapTilPdf
import no.nav.brukerdialog.pdf.PdfData
import no.nav.brukerdialog.utils.DateUtils.somNorskDag
import no.nav.brukerdialog.utils.StringUtils.språkTilTekst
import no.nav.k9.søknad.felles.type.Språk

class OMPAleneomsorgSoknadPDFData(private val melding: OMPAleneomsorgSoknadMottatt): PdfData() {
    override fun ytelse() = Ytelse.OMSORGSDAGER_ALENEOMSORG

    override fun språk(): Språk = Språk.NORSK_BOKMÅL

    override fun pdfData(): Map<String, Any?> {
        return mapOf(
            "tittel" to ytelse().utledTittel(språk()),
            "søknadId" to melding.søknadId,
            "søknadMottattDag" to melding.mottatt.withZoneSameInstant(OSLO_ZONE_ID).somNorskDag(),
            "søknadMottatt" to DATE_TIME_FORMATTER.format(melding.mottatt),
            "søker" to melding.søker.somMap(),
            "barn" to melding.barn.somMapTilPdf(),
            "samtykke" to mapOf(
                "harForståttRettigheterOgPlikter" to melding.harForståttRettigheterOgPlikter,
                "harBekreftetOpplysninger" to melding.harBekreftetOpplysninger
            ),
            "hjelp" to mapOf(
                "språk" to melding.språk?.språkTilTekst()
            )
        )
    }
}
