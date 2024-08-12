package no.nav.brukerdialog.meldinger.omsorgspengermidlertidigalene.domene

import no.nav.k9.søknad.Søknad
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.innsending.MottattMelding
import no.nav.brukerdialog.innsending.PreprosesseringsData
import no.nav.brukerdialog.meldinger.felles.domene.Søker
import no.nav.brukerdialog.ytelse.omsorgspengermidlertidigalene.pdf.OMPMidlertidigAleneSoknadPDFData
import no.nav.brukerdialog.utils.DateUtils.NO_LOCALE
import java.time.ZonedDateTime

data class OMPMidlertidigAleneSoknadMottatt(
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val språk: String? = "nb",
    val søker: Søker,
    val annenForelder: AnnenForelder,
    val barn: List<Barn>,
    val k9Format: Søknad,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean
): MottattMelding {
    override fun ytelse() = Ytelse.OMSORGSPENGER_MIDLERTIDIG_ALENE

    override fun søkerFødselsnummer(): String = søker.fødselsnummer

    override fun k9FormatSøknad() = k9Format

    override fun vedleggId(): List<String> = listOf()

    override fun fødselsattestVedleggId(): List<String> = listOf()

    override fun mapTilPreprosessert(dokumentId: List<List<String>>) = OMPMidlertidigAleneSoknadPreprosessert(
        melding = this,
        dokumentId = dokumentId,
    )

    override fun pdfData() = OMPMidlertidigAleneSoknadPDFData(this)

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

data class Barn(
    val navn: String,
    val aktørId: String?,
    var norskIdentifikator: String?,
) {
    override fun toString(): String {
        return "Barn(navn='$navn', aktørId=*****, norskIdentifikator=*****)"
    }
}

internal fun List<Barn>.somMapTilPdf(): List<Map<String, Any?>> {
    return map {
        mapOf<String, Any?>(
            "navn" to it.navn.capitalizeName(),
            "norskIdentifikator" to it.norskIdentifikator
        )
    }
}

fun String.capitalizeName(): String = split(" ").joinToString(" ") { s ->
    s.lowercase()
        .replaceFirstChar { it.titlecase(NO_LOCALE) }
}
