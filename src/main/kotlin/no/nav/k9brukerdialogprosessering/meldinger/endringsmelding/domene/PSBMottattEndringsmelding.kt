package no.nav.k9brukerdialogprosessering.meldinger.endringsmelding.domene

import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogprosessering.common.Ytelse
import no.nav.k9brukerdialogprosessering.meldinger.endringsmelding.PSBEndringsmeldingPdfData
import no.nav.k9brukerdialogprosessering.innsending.MottattMelding
import no.nav.k9brukerdialogprosessering.innsending.PreprosesseringsData
import no.nav.k9brukerdialogprosessering.meldinger.felles.domene.Søker
import no.nav.k9brukerdialogprosessering.pdf.PdfData

data class PSBEndringsmeldingMottatt(
    val søker: Søker,
    val pleietrengendeNavn: String,
    val harBekreftetOpplysninger: Boolean,
    val harForståttRettigheterOgPlikter: Boolean,
    val k9Format: Søknad
): MottattMelding {
    override fun ytelse(): Ytelse = Ytelse.PLEIEPENGER_SYKT_BARN_ENDRINGSMELDING

    override fun søkerFødselsnummer(): String = søker.fødselsnummer

    override fun k9FormatSøknad(): Søknad = k9Format

    override fun vedleggId(): List<String> = listOf()

    override fun fødselsattestVedleggId(): List<String> = listOf()

    override fun mapTilPreprosessert(dokumentId: List<List<String>>) = PSBPreprossesertEndringsmelding(
        endringsmelding = this,
        dokumentId = dokumentId,
        k9Format = k9Format
    )

    override fun pdfData(): PdfData = PSBEndringsmeldingPdfData(this)

    override fun mapTilPreprosesseringsData(): PreprosesseringsData = PreprosesseringsData(
        søkerFødselsnummer = søker.fødselsnummer,
        k9FormatSøknad = k9Format,
        vedleggId = vedleggId(),
        fødselsattestVedleggId = fødselsattestVedleggId(),
        pdfJournalføringsTittel = "Endringsmelding om pleiepenger",
        jsonJournalføringsTittel = "Endringsmelding om pleiepenger som JSON",
        pdfData = pdfData()
    )
}
