package no.nav.k9brukerdialogprosessering.meldinger.ettersendelse.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.ettersendelse.EttersendelseType
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    val mottatt: ZonedDateTime,
    val språk: String? = "nb",
    val vedleggId: List<String>,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val beskrivelse: String?,
    val søknadstype: Søknadstype,
    val ettersendelsesType: EttersendelseType,
    val pleietrengende: Pleietrengende? = null,
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
        pdfJournalføringsTittel = søknadstype.tittel,
        jsonJournalføringsTittel = "${søknadstype.tittel} (JSON)",
        pdfData = pdfData()
    )
}
