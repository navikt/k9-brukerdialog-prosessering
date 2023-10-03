package no.nav.k9brukerdialogprosessering.meldinger.ettersendelse

import no.nav.k9brukerdialogprosessering.common.Constants.DATE_TIME_FORMATTER
import no.nav.k9brukerdialogprosessering.common.Constants.OSLO_ZONE_ID
import no.nav.k9brukerdialogprosessering.common.Ytelse
import no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.domene.Ettersendelse
import no.nav.k9brukerdialogprosessering.pdf.PdfData
import no.nav.k9brukerdialogprosessering.utils.StringUtils.språkTilTekst
import no.nav.k9brukerdialogprosessering.utils.somNorskDag

class EttersendelsePdfData(private val ettersendelse: Ettersendelse) : PdfData() {
    override fun ytelse(): Ytelse = Ytelse.ETTERSENDELSE

    override fun pdfData(): Map<String, Any?> = mapOf(
        "tittel" to ettersendelse.søknadstype.tittel,
        "soknad_id" to ettersendelse.søknadId,
        "soknad_mottatt_dag" to ettersendelse.mottatt.withZoneSameInstant(OSLO_ZONE_ID).somNorskDag(),
        "soknad_mottatt" to DATE_TIME_FORMATTER.format(ettersendelse.mottatt),
        "søker" to ettersendelse.søker.somMap(),
        "beskrivelse" to ettersendelse.beskrivelse,
        "samtykke" to mapOf(
            "harForståttRettigheterOgPlikter" to ettersendelse.harForståttRettigheterOgPlikter,
            "harBekreftetOpplysninger" to ettersendelse.harBekreftetOpplysninger
        ),
        "titler" to mapOf(
            "vedlegg" to ettersendelse.titler.somMapTitler()
        ),
        "hjelp" to mapOf(
            "språk" to ettersendelse.språk?.språkTilTekst()
        )
    )

    private fun List<String>.somMapTitler(): List<Map<String, Any?>> {
        return map {
            mapOf(
                "tittel" to it
            )
        }
    }
}
