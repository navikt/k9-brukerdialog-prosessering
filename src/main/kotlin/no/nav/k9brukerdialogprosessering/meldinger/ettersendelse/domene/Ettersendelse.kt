package no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.domene

import no.nav.k9brukerdialogprosessering.common.Ytelse
import no.nav.k9brukerdialogprosessering.innsending.MottattMelding
import no.nav.k9brukerdialogprosessering.innsending.PreprosesseringsData
import no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.EttersendelsePdfData
import no.nav.k9brukerdialogprosessering.meldinger.felles.domene.Søker
import no.nav.k9brukerdialogprosessering.pdf.PdfData
import java.time.ZonedDateTime

data class Ettersendelse(
    val søker: Søker,
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val språk: String? = "nb",
    val vedleggId: List<String>,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val beskrivelse: String?,
    val søknadstype: Søknadstype,
    val titler: List<String>,
    val k9Format: no.nav.k9.ettersendelse.Ettersendelse,
) : MottattMelding {
    override fun ytelse(): Ytelse = Ytelse.ETTERSENDELSE

    override fun søkerFødselsnummer(): String = søker.fødselsnummer

    override fun k9FormatSøknad() = k9Format

    override fun vedleggId(): List<String> = vedleggId

    override fun fødselsattestVedleggId(): List<String> = listOf()

    override fun mapTilPreprosessert(dokumentId: List<List<String>>) = PreprosessertEttersendelse(
        this,
        dokumentId
    )

    override fun pdfData(): PdfData = EttersendelsePdfData(this)

    override fun mapTilPreprosesseringsData() = PreprosesseringsData(
        søkerFødselsnummer = søker.fødselsnummer,
        k9FormatSøknad = k9Format,
        vedleggId = vedleggId,
        fødselsattestVedleggId = fødselsattestVedleggId(),
        pdfJournalføringsTittel = søknadstype.somDokumentbeskrivelse(),
        jsonJournalføringsTittel = "Ettersendelse $søknadstype som JSON",
        pdfData = pdfData()
    )

    private fun Søknadstype.somDokumentbeskrivelse(): String {
        return when (this) {
            Søknadstype.PLEIEPENGER_SYKT_BARN -> "Ettersendelse pleiepenger sykt barn"
            Søknadstype.OMP_UTV_KS -> "Ettersendelse ekstra omsorgsdager"
            Søknadstype.OMP_UT_SNF -> "Ettersendelse omsorgspenger utbetaling selvstendig/frilanser"
            Søknadstype.OMP_UT_ARBEIDSTAKER -> "Ettersendelse omsorgspenger utbetaling arbeidstaker"
            Søknadstype.OMP_UTV_MA -> "Ettersendelse omsorgspenger regnet som alene"
            Søknadstype.OMP_DELE_DAGER -> "Ettersendelse melding om deling av omsorgsdager"
            Søknadstype.PLEIEPENGER_LIVETS_SLUTTFASE -> "Ettersendelse pleiepenger i livets sluttfase"
        }
    }
}
