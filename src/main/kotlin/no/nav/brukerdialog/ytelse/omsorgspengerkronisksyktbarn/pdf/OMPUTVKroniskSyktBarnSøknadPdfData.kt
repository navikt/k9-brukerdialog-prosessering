package no.nav.brukerdialog.ytelse.omsorgspengerkronisksyktbarn.pdf

import no.nav.brukerdialog.common.Constants.DATE_TIME_FORMATTER
import no.nav.brukerdialog.common.Constants.OSLO_ZONE_ID
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.meldinger.omsorgspengerkronisksyktbarn.domene.OMPUTVKroniskSyktBarnSøknadMottatt
import no.nav.brukerdialog.pdf.PdfData
import no.nav.brukerdialog.utils.DateUtils.somNorskDag
import no.nav.brukerdialog.utils.StringUtils.språkTilTekst
import no.nav.brukerdialog.utils.StringUtils.storForbokstav
import no.nav.k9.søknad.felles.type.Språk

class OMPUTVKroniskSyktBarnSøknadPdfData(private val søknad: OMPUTVKroniskSyktBarnSøknadMottatt) : PdfData() {
    override fun ytelse(): Ytelse = Ytelse.OMSORGSPENGER_UTVIDET_RETT

    override fun språk(): Språk {
        return søknad.k9FormatSøknad.språk
    }

    override fun pdfData(): Map<String, Any?> {
        return mapOf(
            "tittel" to ytelse().utledTittel(språk()),
            "soknad_id" to søknad.søknadId,
            "soknad_mottatt_dag" to søknad.mottatt.withZoneSameInstant(OSLO_ZONE_ID).somNorskDag(),
            "soknad_mottatt" to DATE_TIME_FORMATTER.format(søknad.mottatt),
            "søker" to søknad.søker.somMap(),
            "barn" to mapOf(
                "navn" to søknad.barn.navn.storForbokstav(),
                "id" to søknad.barn.norskIdentifikator,
                "fødselsdato" to søknad.barn.fødselsdato
            ),
            "relasjonTilBarnet" to søknad.relasjonTilBarnet?.utskriftsvennlig,
            "sammeAddresse" to søknad.sammeAdresse?.name,
            "høyereRisikoForFravær" to søknad.høyereRisikoForFravær,
            "høyereRisikoForFraværBeskrivelse" to søknad.høyereRisikoForFraværBeskrivelse,
            "kroniskEllerFunksjonshemming" to søknad.kroniskEllerFunksjonshemming,
            "samtykke" to mapOf(
                "harForståttRettigheterOgPlikter" to søknad.harForståttRettigheterOgPlikter,
                "harBekreftetOpplysninger" to søknad.harBekreftetOpplysninger
            ),
            "hjelp" to mapOf(
                "språk" to søknad.språk?.språkTilTekst()
            ),
            "harIkkeLastetOppLegeerklæring" to søknad.harIkkeLastetOppLegeerklæring()
        )
    }

    private fun OMPUTVKroniskSyktBarnSøknadMottatt.harIkkeLastetOppLegeerklæring(): Boolean =
        !legeerklæringVedleggId.isNotEmpty()
        
}
