package no.nav.brukerdialog.meldinger.omsorgspengerkronisksyktbarn.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.søknad.Søknad
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.domenetjenester.mottak.MottattMelding
import no.nav.brukerdialog.domenetjenester.mottak.PreprosesseringsData
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.brukerdialog.ytelse.omsorgspengerkronisksyktbarn.pdf.OMPUTVKroniskSyktBarnSøknadPdfData
import no.nav.brukerdialog.pdf.PdfData
import no.nav.brukerdialog.ytelse.omsorgspengerkronisksyktbarn.kafka.domene.OMPUTVKroniskSyktBarnSøknadPreprosesssert
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
    val sammeAdresse: BarnSammeAdresse? = null,
    val høyereRisikoForFravær: Boolean? = null,
    val høyereRisikoForFraværBeskrivelse: String? = null,
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
        pdfJournalføringsTittel = ytelse().tittel,
        jsonJournalføringsTittel = "${ytelse().tittel}(JSON)",
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
