package no.nav.brukerdialog.ytelse.ettersendelse.pdf

import no.nav.brukerdialog.common.Constants.DATE_TIME_FORMATTER
import no.nav.brukerdialog.common.Constants.OSLO_ZONE_ID
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.meldinger.ettersendelse.domene.Ettersendelse
import no.nav.brukerdialog.meldinger.ettersendelse.domene.Pleietrengende
import no.nav.brukerdialog.pdf.PdfData
import no.nav.brukerdialog.utils.DateUtils.somNorskDag
import no.nav.brukerdialog.utils.StringUtils.språkTilTekst

class EttersendelsePdfData(private val ettersendelse: Ettersendelse) : PdfData() {
    override fun ytelse(): Ytelse = Ytelse.ETTERSENDELSE

    override fun pdfData(): Map<String, Any?> = mapOf(
        "tittel" to ettersendelse.søknadstype.tittel,
        "soknad_id" to ettersendelse.søknadId,
        "soknad_mottatt_dag" to ettersendelse.mottatt.withZoneSameInstant(OSLO_ZONE_ID).somNorskDag(),
        "soknad_mottatt" to DATE_TIME_FORMATTER.format(ettersendelse.mottatt),
        "søker" to ettersendelse.søker.somMap(),
        "pleietrengende" to ettersendelse.pleietrengende?.somMap(),
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

    private fun Pleietrengende.somMap(): Map<String, Any?> {
        return mapOf(
            "norskIdentitetsnummer" to norskIdentitetsnummer,
            "navn" to navn,
            "fødselsdato" to fødselsdato
        )
    }
}
