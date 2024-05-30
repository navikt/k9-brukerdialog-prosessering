package no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingat

import com.fasterxml.jackson.core.type.TypeReference
import no.nav.k9brukerdialogprosessering.common.Constants.OSLO_ZONE_ID
import no.nav.k9brukerdialogprosessering.common.Ytelse
import no.nav.k9brukerdialogprosessering.config.JacksonConfiguration
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingat.domene.*
import no.nav.k9brukerdialogprosessering.meldinger.omsorgpengerutbetalingsnf.domene.Barn
import no.nav.k9brukerdialogprosessering.pdf.PdfData
import no.nav.k9brukerdialogprosessering.utils.DateUtils.somNorskDag
import no.nav.k9brukerdialogprosessering.utils.StringUtils.språkTilTekst
import java.time.Duration

class OMPUtbetalingATSoknadPDFData(private val melding: OMPUtbetalingATSoknadMottatt) : PdfData() {

    private companion object {
        val jacksonConfig = JacksonConfiguration()
        val mapper = jacksonConfig.objectMapper(jacksonConfig.kotlinModule(), jacksonConfig.javaTimeModule())
    }

    override fun ytelse(): Ytelse = Ytelse.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER

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
            "harArbeidsgivere" to melding.arbeidsgivere.isNotEmpty(),
            "arbeidsgivere" to melding.arbeidsgivere.somMap(),
            "fosterbarn" to if (!melding.fosterbarn.isNullOrEmpty()) melding.fosterbarn.somMap() else null,
            "dineBarn" to if (!melding.dineBarn?.barn.isNullOrEmpty()) melding.dineBarn?.somMap() else null,
            "harOpphold" to melding.opphold.isNotEmpty(),
            "harBosteder" to melding.bosteder.isNotEmpty(),
            "harVedlegg" to melding.vedleggId.isNotEmpty(),
            "ikkeHarSendtInnVedlegg" to melding.vedleggId.isEmpty(),
            "bekreftelser" to melding.bekreftelser.bekreftelserSomMap(),
            "titler" to mapOf(
                "vedlegg" to melding.titler.somMapTitler()
            )
        )
    }

    private fun OMPUtbetalingATSoknadMottatt.somMap() = mapper.convertValue(
        this,
        object :
            TypeReference<MutableMap<String, Any?>>() {}
    )

    private fun DineBarn.somMap() = mapOf(
        "harDeltBosted" to harDeltBosted,
        "barn" to barn.map {
            mapOf(
                "navn" to it.navn,
                "fødselsdato" to it.fødselsdato,
                "identitetsnummer" to it.identitetsnummer,
                "type" to it.type.pdfTekst
            )
        }
    )

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

    private fun List<String>.somMapTitler(): List<Map<String, Any?>> {
        return map {
            mapOf(
                "tittel" to it
            )
        }
    }

    private fun List<ArbeidsgiverDetaljer>.somMap(): List<Map<String, Any?>> {
        return map {
            mapOf(
                "navn" to it.navn,
                "organisasjonsnummer" to it.organisasjonsnummer,
                "utbetalingsårsak" to it.utbetalingsårsak.pdfTekst,
                "harSattKonfliktForklaring" to (it.konfliktForklaring != null),
                "konfliktForklaring" to it.konfliktForklaring,
                "harSattÅrsakNyoppstartet" to (it.årsakNyoppstartet != null),
                "årsakNyoppstartet" to it.årsakNyoppstartet?.pdfTekst
            )
        }
    }

    @JvmName("somMapFosterbarn")
    private fun List<Fosterbarn>.somMap() = map {
        mapOf(
            "navn" to it.navn,
            "identitetsnummer" to it.identitetsnummer
        )
    }
}
