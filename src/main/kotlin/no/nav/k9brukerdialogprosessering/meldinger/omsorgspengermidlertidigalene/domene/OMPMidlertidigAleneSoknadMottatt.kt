package no.nav.k9brukerdialogprosessering.meldinger.omsorgspengermidlertidigalene.domene

import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogprosessering.common.Ytelse
import no.nav.k9brukerdialogprosessering.innsending.MottattMelding
import no.nav.k9brukerdialogprosessering.innsending.PreprosesseringsData
import no.nav.k9brukerdialogprosessering.meldinger.felles.domene.Søker
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengermidlertidigalene.OMPMidlertidigAleneSoknadPDFData
import java.time.ZonedDateTime
import java.util.*

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
        pdfJournalføringsTittel = "Søknad ekstra omsorgsdager – ikke tilsyn",
        jsonJournalføringsTittel = "Søknad ekstra omsorgsdager – ikke tilsyn som JSON"
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
        .replaceFirstChar { it.titlecase(Locale.getDefault()) }
}
