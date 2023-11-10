package no.nav.k9brukerdialogprosessering.meldinger.omsorgspengermidlertidigalene

import no.nav.k9brukerdialogprosessering.common.Constants.DATE_TIME_FORMATTER
import no.nav.k9brukerdialogprosessering.common.Constants.OSLO_ZONE_ID
import no.nav.k9brukerdialogprosessering.common.Ytelse
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengermidlertidigalene.domene.OMPMidlertidigAleneSoknadMottatt
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengermidlertidigalene.domene.somMapTilPdf
import no.nav.k9brukerdialogprosessering.pdf.PdfData
import no.nav.k9brukerdialogprosessering.utils.DateUtils.somNorskDag
import no.nav.k9brukerdialogprosessering.utils.StringUtils.språkTilTekst
import java.time.temporal.ChronoUnit

class OMPMidlertidigAleneSoknadPDFData(private val melding: OMPMidlertidigAleneSoknadMottatt): PdfData() {
    override fun ytelse() = Ytelse.OMSORGSPENGER_MIDLERTIDIG_ALENE

    override fun pdfData(): Map<String, Any?> = mapOf(
        "tittel" to ytelse().tittel,
        "søknadId" to melding.søknadId,
        "søknadMottattDag" to melding.mottatt.withZoneSameInstant(OSLO_ZONE_ID).somNorskDag(),
        "søknadMottatt" to DATE_TIME_FORMATTER.format(melding.mottatt),
        "søker" to melding.søker.somMap(),
        "barn" to melding.barn.somMapTilPdf(),
        "annenForelder" to melding.annenForelder.somMapTilPdf(),
        "samtykke" to mapOf(
            "harForståttRettigheterOgPlikter" to melding.harForståttRettigheterOgPlikter,
            "harBekreftetOpplysninger" to melding.harBekreftetOpplysninger
        ),
        "hjelp" to mapOf(
            "språk" to melding.språk?.språkTilTekst(),
            "periodeOver6MånederSatt" to melding.annenForelder.periodeOver6Måneder.erSatt(),
            "erPeriodenOver6Måneder" to melding.erPeriodeOver6Mnd()
        )
    )

    private fun Boolean?.erSatt() = this != null

    private fun OMPMidlertidigAleneSoknadMottatt.erPeriodeOver6Mnd(): Boolean? {
        return if(annenForelder.periodeFraOgMed == null || annenForelder.periodeTilOgMed == null) null else {
            val differanse = ChronoUnit.DAYS.between(annenForelder.periodeFraOgMed, annenForelder.periodeTilOgMed.plusDays(1)) // plusDays(1) fordi den er eksklusiv i utregningen
            differanse >= 182
        }
    }
}
