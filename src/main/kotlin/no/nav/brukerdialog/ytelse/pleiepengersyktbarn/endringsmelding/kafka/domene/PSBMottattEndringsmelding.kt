package no.nav.brukerdialog.meldinger.endringsmelding.domene

import no.nav.k9.søknad.Søknad
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.pdf.PSBEndringsmeldingPdfData
import no.nav.brukerdialog.domenetjenester.mottak.MottattMelding
import no.nav.brukerdialog.domenetjenester.mottak.PreprosesseringsData
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.brukerdialog.pdf.PdfData

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
        pdfJournalføringsTittel = ytelse().tittel,
        jsonJournalføringsTittel = "${ytelse().tittel}(JSON)",
        pdfData = pdfData()
    )
}
