package no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf

import com.fasterxml.jackson.core.type.TypeReference
import no.nav.k9brukerdialogprosessering.common.Constants.DATE_FORMATTER
import no.nav.k9brukerdialogprosessering.common.Constants.OSLO_ZONE_ID
import no.nav.k9brukerdialogprosessering.common.Ytelse
import no.nav.k9brukerdialogprosessering.config.JacksonConfiguration
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.domene.Barn
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.domene.Bekreftelser
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.domene.Land
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.domene.OMPUtbetalingSNFSoknadMottatt
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.domene.Regnskapsfører
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.domene.SelvstendigNæringsdrivende
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.domene.VarigEndring
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.domene.YrkesaktivSisteTreFerdigliknedeÅrene
import no.nav.k9brukerdialogprosessering.pdf.PdfData
import no.nav.k9brukerdialogprosessering.utils.StringUtils.språkTilTekst
import no.nav.k9brukerdialogprosessering.utils.somNorskDag
import java.time.Duration

class OMPUtbetalingSNFSoknadPDFData(private val melding: OMPUtbetalingSNFSoknadMottatt) : PdfData() {

    private companion object {
        val jacksonConfig = JacksonConfiguration()
        val mapper = jacksonConfig.objectMapper(jacksonConfig.kotlinModule(), jacksonConfig.javaTimeModule())
    }

    override fun ytelse(): Ytelse = Ytelse.OMSORGSPENGER_UTBETALING_SNF

    override fun pdfData(): Map<String, Any?> {
        val mottatt = melding.mottatt.toLocalDate()
        return mapOf(
            "tittel" to ytelse().tittel,
            "søknad" to melding.somMap(),
            "språk" to melding.språk.språkTilTekst(),
            "mottaksUkedag" to melding.mottatt.withZoneSameInstant(OSLO_ZONE_ID).somNorskDag(),
            "søker" to melding.søker.somMap(),
            "medlemskap" to mapOf(
                "siste12" to melding.bosteder.any {
                    it.fraOgMed.isBefore(mottatt) || it.tilOgMed.isEqual(mottatt)
                },
                "neste12" to melding.bosteder.any {
                    it.fraOgMed.isEqual(mottatt) || it.fraOgMed.isAfter(mottatt)
                }
            ),
            "barn" to if (melding.barn.isNotEmpty()) melding.barn.somMap() else null,
            "harUtbetalingsperioder" to melding.utbetalingsperioder.isNotEmpty(),
            "harOpphold" to melding.opphold.isNotEmpty(),
            "ikkeHarSendtInnVedlegg" to melding.vedleggId.isEmpty(),
            "harBosteder" to melding.bosteder.isNotEmpty(),
            "harVedlegg" to melding.vedleggId.isNotEmpty(),
            "bekreftelser" to melding.bekreftelser.bekreftelserSomMap(),
            "selvstendigNæringsdrivende" to melding.selvstendigNæringsdrivende?.somMap(),
            "titler" to mapOf(
                "vedlegg" to melding.titler?.somMapTitler()
            )
        )
    }

    private fun OMPUtbetalingSNFSoknadMottatt.somMap() = mapper.convertValue(
        this,
        object : TypeReference<MutableMap<String, Any?>>() {}
    )

    private fun List<String>.somMapTitler(): List<Map<String, Any?>> {
        return map {
            mapOf(
                "tittel" to it
            )
        }
    }

    private fun Bekreftelser.bekreftelserSomMap(): Map<String, Boolean> {
        return mapOf(
            "harBekreftetOpplysninger" to harBekreftetOpplysninger.boolean,
            "harForståttRettigheterOgPlikter" to harForståttRettigheterOgPlikter.boolean
        )
    }

    private fun Duration.tilString(): String = when (this.toMinutesPart()) {
        0 -> "${this.toHours()} timer"
        else -> "${this.toHoursPart()} timer og ${this.toMinutesPart()} minutter"
    }

    private fun SelvstendigNæringsdrivende.somMap(): Map<String, Any?> = mapOf(
        "næringsinntekt" to næringsinntekt,
        "næringstype" to næringstype.beskrivelse,
        "fiskerErPåBladB" to fiskerErPåBladB?.boolean,
        "yrkesaktivSisteTreFerdigliknedeÅrene" to yrkesaktivSisteTreFerdigliknedeÅrene?.somMap(),
        "varigEndring" to varigEndring?.somMap(),
        "harFlereAktiveVirksomheter" to harFlereAktiveVirksomheter,
        "navnPåVirksomheten" to navnPåVirksomheten,
        "fraOgMed" to DATE_FORMATTER.format(fraOgMed),
        "tilOgMed" to if(tilOgMed != null) DATE_FORMATTER.format(tilOgMed) else null,
        "registrertINorge" to registrertINorge.boolean,
        "organisasjonsnummer" to organisasjonsnummer,
        "registrertIUtlandet" to registrertIUtlandet?.somMap(),
        "regnskapsfører" to regnskapsfører?.somMap()
    )

    private fun YrkesaktivSisteTreFerdigliknedeÅrene.somMap() : Map<String, Any?> = mapOf(
        "oppstartsdato" to DATE_FORMATTER.format(oppstartsdato)
    )

    private fun VarigEndring.somMap() : Map<String, Any?> = mapOf(
        "dato" to DATE_FORMATTER.format(dato),
        "inntektEtterEndring" to inntektEtterEndring,
        "forklaring" to forklaring
    )

    private fun Land.somMap() = mapOf<String, Any?>(
        "landnavn" to landnavn,
        "landkode" to landkode
    )

    private fun Regnskapsfører.somMap() = mapOf<String, Any?>(
        "navn" to navn,
        "telefon" to telefon
    )

    private fun List<Barn>.somMap() = map {
        mapOf(
            "navn" to it.navn,
            "fødselsdato" to it.fødselsdato,
            "identitetsnummer" to it.identitetsnummer,
            "type" to it.type.pdfTekst,
            "harUtvidetRett" to (it.utvidetRett == true)
        )
    }
}
