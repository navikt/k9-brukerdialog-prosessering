package no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogprosessering.common.Ytelse
import no.nav.k9brukerdialogprosessering.innsending.MottattMelding
import no.nav.k9brukerdialogprosessering.innsending.PreprosesseringsData
import no.nav.k9brukerdialogprosessering.meldinger.felles.domene.Søker
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn.OMPUTVKroniskSyktBarnSøknadPdfData
import no.nav.k9brukerdialogprosessering.pdf.PdfData
import java.time.LocalDate
import java.time.ZonedDateTime

data class OMPUTVKroniskSyktBarnSøknadMottatt(
    val nyVersjon: Boolean = false,
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val språk: String? = "nb",
    val kroniskEllerFunksjonshemming: Boolean = false,
    val barn: Barn,
    val søker: Søker,
    val relasjonTilBarnet: SøkerBarnRelasjon? = null,
    val sammeAdresse: Boolean = false,
    val legeerklæringVedleggId: List<String> = listOf(),
    val samværsavtaleVedleggId: List<String> = listOf(),
    val harBekreftetOpplysninger: Boolean,
    val harForståttRettigheterOgPlikter: Boolean,
    val k9FormatSøknad: Søknad,
) : MottattMelding {
    override fun ytelse(): Ytelse = Ytelse.OMSORGSPENGER_UTVIDET_RETT

    override fun søkerFødselsnummer(): String = søker.fødselsnummer

    override fun k9FormatSøknad(): Søknad = k9FormatSøknad

    override fun vedleggId(): List<String> = legeerklæringVedleggId + samværsavtaleVedleggId

    override fun fødselsattestVedleggId(): List<String> = listOf()

    override fun mapTilPreprosessert(dokumentId: List<List<String>>) =
        OMPUTVKroniskSyktBarnSøknadPreprosesssert(this, dokumentId)

    override fun pdfData(): PdfData = OMPUTVKroniskSyktBarnSøknadPdfData(this)

    override fun mapTilPreprosesseringsData() = PreprosesseringsData(
        søkerFødselsnummer = søker.fødselsnummer,
        k9FormatSøknad = k9FormatSøknad,
        vedleggId = vedleggId(),
        fødselsattestVedleggId = fødselsattestVedleggId(),
        pdfJournalføringsTittel = "Søknad om ekstra omsorgsdager",
        jsonJournalføringsTittel = "Søknad om ekstra omsorgsdager som JSON",
        pdfData = pdfData()
    )

    override fun toString(): String {
        return "MeldingV1(søknadId='$søknadId', mottatt=$mottatt)"
    }
}

data class Barn(
    val navn: String,
    val norskIdentifikator: String,
    @JsonFormat(pattern = "yyyy-MM-dd") val fødselsdato: LocalDate? = null,
    val aktørId: String?,
) {
    override fun toString(): String {
        return "Barn()"
    }
}

enum class SøkerBarnRelasjon(val utskriftsvennlig: String) {
    MOR("Mor"),
    FAR("Far"),
    ADOPTIVFORELDER("Adoptivforelder"),
    FOSTERFORELDER("Fosterforelder")
}
