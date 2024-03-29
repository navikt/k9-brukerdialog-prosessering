package no.nav.k9brukerdialogprosessering.meldinger.omsorgspengeraleneomsorg

import no.nav.k9brukerdialogprosessering.common.Constants.DATE_TIME_FORMATTER
import no.nav.k9brukerdialogprosessering.common.Constants.OSLO_ZONE_ID
import no.nav.k9brukerdialogprosessering.common.Ytelse
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengeraleneomsorg.domene.OMPAleneomsorgSoknadMottatt
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengeraleneomsorg.domene.somMapTilPdf
import no.nav.k9brukerdialogprosessering.pdf.PdfData
import no.nav.k9brukerdialogprosessering.utils.DateUtils.somNorskDag
import no.nav.k9brukerdialogprosessering.utils.StringUtils.språkTilTekst

class OMPAleneomsorgSoknadPDFData(private val melding: OMPAleneomsorgSoknadMottatt): PdfData() {
    override fun ytelse() = Ytelse.OMSORGSDAGER_ALENEOMSORG

    override fun pdfData(): Map<String, Any?> =  mapOf(
        "tittel" to ytelse().tittel,
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
