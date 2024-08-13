package no.nav.brukerdialog.meldinger.ettersendelse.domene

import no.nav.k9.ettersendelse.EttersendelseType
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.domenetjenester.mottak.MottattMelding
import no.nav.brukerdialog.domenetjenester.mottak.PreprosesseringsData
import no.nav.brukerdialog.ytelse.ettersendelse.pdf.EttersendelsePdfData
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.brukerdialog.pdf.PdfData
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
