package no.nav.brukerdialog.meldinger.omsorgspengeraleneomsorg.domene

import no.nav.k9.søknad.Søknad
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.innsending.MottattMelding
import no.nav.brukerdialog.innsending.PreprosesseringsData
import no.nav.brukerdialog.meldinger.felles.domene.Søker
import no.nav.brukerdialog.meldinger.omsorgspengeraleneomsorg.OMPAleneomsorgSoknadPDFData
import java.time.ZonedDateTime

data class OMPAleneomsorgSoknadMottatt(
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val språk: String? = "nb",
    val søker: Søker,
    val barn: Barn,
    val k9Søknad: Søknad,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean
): MottattMelding {
    override fun ytelse() = Ytelse.OMSORGSDAGER_ALENEOMSORG

    override fun søkerFødselsnummer(): String = søker.fødselsnummer

    override fun k9FormatSøknad() = k9Søknad

    override fun vedleggId(): List<String> = listOf()

    override fun fødselsattestVedleggId(): List<String> = listOf()

    override fun mapTilPreprosessert(dokumentId: List<List<String>>) = OMPAleneomsorgSoknadPreprosessert(
        melding = this,
        dokumentId = dokumentId,
    )

    override fun pdfData() = OMPAleneomsorgSoknadPDFData(this)

    override fun mapTilPreprosesseringsData() = PreprosesseringsData(
        søkerFødselsnummer = søkerFødselsnummer(),
        k9FormatSøknad = k9FormatSøknad(),
        vedleggId = vedleggId(),
        fødselsattestVedleggId = fødselsattestVedleggId(),
        pdfData = pdfData(),
        pdfJournalføringsTittel = ytelse().tittel,
        jsonJournalføringsTittel = "${ytelse().tittel}(JSON)",
    )
}
