package no.nav.k9brukerdialogprosessering.meldinger.ettersendelse

import no.nav.k9brukerdialogprosessering.common.Constants.DATE_TIME_FORMATTER
import no.nav.k9brukerdialogprosessering.common.Constants.ZONE_ID
import no.nav.k9brukerdialogprosessering.common.Ytelse
import no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.domene.Ettersendelse
import no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.domene.Søker
import no.nav.k9brukerdialogprosessering.pdf.PdfData
import no.nav.k9brukerdialogprosessering.utils.somNorskDag
import java.util.*

class EttersendelsePdfData(private val ettersendelse: Ettersendelse) : PdfData() {
    override fun ytelse(): Ytelse = Ytelse.ETTERSENDELSE

    override fun pdfData(): Map<String, Any?> = mapOf(
        "soknad_id" to ettersendelse.søknadId,
        "soknad_mottatt_dag" to ettersendelse.mottatt.withZoneSameInstant(ZONE_ID).somNorskDag(),
        "soknad_mottatt" to DATE_TIME_FORMATTER.format(ettersendelse.mottatt),
        "søker" to mapOf(
            "navn" to ettersendelse.søker.formatertNavn().capitalizeName(),
            "fødselsnummer" to ettersendelse.søker.fødselsnummer
        ),
        "beskrivelse" to ettersendelse.beskrivelse,
        "søknadstype" to ettersendelse.søknadstype.pdfNavn,
        "samtykke" to mapOf(
            "harForståttRettigheterOgPlikter" to ettersendelse.harForståttRettigheterOgPlikter,
            "harBekreftetOpplysninger" to ettersendelse.harBekreftetOpplysninger
        ),
        "titler" to mapOf(
            "vedlegg" to ettersendelse.titler.somMapTitler()
        ),
        "hjelp" to mapOf(
            "språk" to ettersendelse.språk?.sprakTilTekst()
        )
    )

    fun Søker.formatertNavn() = if (mellomnavn != null) "$fornavn $mellomnavn $etternavn" else "$fornavn $etternavn"
    fun String.capitalizeName(): String = split(" ").joinToString(" ") { it.lowercase(Locale.getDefault()).capitalize() }

    private fun List<String>.somMapTitler(): List<Map<String, Any?>> {
        return map {
            mapOf(
                "tittel" to it
            )
        }
    }

    private fun String.sprakTilTekst() = when (this.lowercase(Locale.getDefault())) {
        "nb" -> "bokmål"
        "nn" -> "nynorsk"
        else -> this
    }
}
