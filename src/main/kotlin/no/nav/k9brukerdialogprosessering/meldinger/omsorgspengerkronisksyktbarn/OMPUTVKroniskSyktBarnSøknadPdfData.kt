package no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn

import no.nav.k9brukerdialogprosessering.common.Constants.DATE_TIME_FORMATTER
import no.nav.k9brukerdialogprosessering.common.Constants.OSLO_ZONE_ID
import no.nav.k9brukerdialogprosessering.common.Ytelse
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn.domene.OMPUTVKroniskSyktBarnSøknadMottatt
import no.nav.k9brukerdialogprosessering.pdf.PdfData
import no.nav.k9brukerdialogprosessering.utils.DateUtils.somNorskDag
import no.nav.k9brukerdialogprosessering.utils.StringUtils.språkTilTekst
import no.nav.k9brukerdialogprosessering.utils.StringUtils.storForbokstav

class OMPUTVKroniskSyktBarnSøknadPdfData(private val søknad: OMPUTVKroniskSyktBarnSøknadMottatt) : PdfData() {
    override fun ytelse(): Ytelse = Ytelse.OMSORGSPENGER_UTVIDET_RETT

    override fun pdfData(): Map<String, Any?> = mapOf(
        "tittel" to ytelse().tittel,
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
        "sammeAddresse" to søknad.sammeAdresse,
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

    private fun OMPUTVKroniskSyktBarnSøknadMottatt.harIkkeLastetOppLegeerklæring(): Boolean =
        !legeerklæringVedleggId.isNotEmpty()

}
