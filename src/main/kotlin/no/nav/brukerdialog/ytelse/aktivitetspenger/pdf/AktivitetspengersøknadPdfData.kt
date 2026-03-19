package no.nav.brukerdialog.ytelse.aktivitetspenger.pdf

import no.nav.brukerdialog.common.Constants.DATE_FORMATTER
import no.nav.brukerdialog.common.Constants.DATE_TIME_FORMATTER
import no.nav.brukerdialog.common.Constants.OSLO_ZONE_ID
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.pdf.PdfData
import no.nav.brukerdialog.utils.DateUtils.somNorskDag
import no.nav.brukerdialog.utils.StringUtils.språkTilTekst
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.soknad.Barn
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.soknad.Bosted
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.soknad.ForutgåendeMedlemskap
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.soknad.KontonummerInfo
import no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.soknad.domene.AktivitetspengersøknadMottatt
import no.nav.k9.søknad.felles.type.Språk

class AktivitetspengersøknadPdfData(private val søknad: AktivitetspengersøknadMottatt) : PdfData() {
    override fun ytelse(): Ytelse = Ytelse.AKTIVITETSPENGER

    override fun språk(): Språk = Språk.NORSK_BOKMÅL

    override fun pdfData(): Map<String, Any?> = mapOf(
        "tittel" to ytelse().utledTittel(språk()),
        "søknadId" to søknad.søknadId,
        "søknadMottattDag" to søknad.mottatt.withZoneSameInstant(OSLO_ZONE_ID).somNorskDag(),
        "søknadMottatt" to DATE_TIME_FORMATTER.format(søknad.mottatt),
        "startdato" to søknad.startdato?.let { DATE_FORMATTER.format(it) },
        "søker" to søknad.søker.somMap(),
        "forutgåendeMedlemskap" to søknad.forutgåendeMedlemskap.somMap(),
        "barn" to mapOf(
            "barnErRiktig" to søknad.barnErRiktig,
            "folkeregistrerteBarn" to if (søknad.barn.isNotEmpty()) {
                søknad.barn.map { it.somMap() }
            } else null,
        ),
        "kontonummerInfo" to søknad.kontonummerInfo.somMap(),
        "samtykke" to mapOf(
            "harForståttRettigheterOgPlikter" to søknad.harForståttRettigheterOgPlikter,
            "harBekreftetOpplysninger" to søknad.harBekreftetOpplysninger
        ),
        "hjelp" to mapOf(
            "språk" to søknad.språk?.språkTilTekst(),
        )
    )

    private fun KontonummerInfo.somMap() = mapOf(
        "kontonummerErRiktig" to kontonummerErRiktig,
        "kontonummerFraRegister" to kontonummerFraRegister,
        "harKontonummer" to harKontonummer.name
    )

    private fun Barn.somMap(): Map<String, String?> = mapOf(
        "navn" to navn
    )

    private fun ForutgåendeMedlemskap.somMap() = mapOf(
        "har_bodd_i_utlandet_siste_5_år" to harBoddIUtlandetSiste5År,
        "utenlandsopphold_siste_5_år" to utenlandsoppholdSiste5År.map { it.somMap() },
    )

    private fun Bosted.somMap() = mapOf(
        "landnavn" to landnavn,
        "fraOgMed" to fraOgMed,
        "tilOgMed" to tilOgMed,
    )
}
