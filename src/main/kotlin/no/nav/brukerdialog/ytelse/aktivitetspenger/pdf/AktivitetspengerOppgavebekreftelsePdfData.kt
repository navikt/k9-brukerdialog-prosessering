package no.nav.brukerdialog.ytelse.aktivitetspenger.pdf

import no.nav.brukerdialog.common.Constants.DATE_FORMATTER
import no.nav.brukerdialog.common.Constants.DATE_TIME_FORMATTER
import no.nav.brukerdialog.common.Constants.OSLO_ZONE_ID
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.pdf.PdfData
import no.nav.brukerdialog.utils.DateUtils.somNorskDag
import no.nav.brukerdialog.utils.DateUtils.somNorskMåned
import no.nav.brukerdialog.utils.NumberUtils.formaterSomValuta
import no.nav.brukerdialog.utils.StringUtils.språkTilTekst
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.oppgavebekreftelse.AktivitetspengerOppgaveUttalelseDTO
import no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.oppgavebekreftelse.domene.AktivitetspengerOppgavebekreftelseMottatt
import no.nav.k9.søknad.felles.type.Språk
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.RegisterinntektDTO
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.kontrollerregisterinntekt.YtelseType
import java.math.BigDecimal

class AktivitetspengerOppgavebekreftelsePdfData(
    private val oppgavebekreftelseMottatt: AktivitetspengerOppgavebekreftelseMottatt,
) : PdfData() {
    override fun ytelse(): Ytelse = Ytelse.AKTIVITETSPENGER_OPPGAVEBEKREFTELSE

    override fun språk(): Språk = Språk.NORSK_BOKMÅL

    override fun pdfData(): Map<String, Any?> {
        val k9Format = oppgavebekreftelseMottatt.k9Format
        return mapOf(
            "tittel" to ytelse().utledTittel(språk()) + oppgavebekreftelseMottatt.oppgave.dokumentTittelSuffix(),
            "oppgave" to mapOf(
                "oppgaveReferanse" to oppgavebekreftelseMottatt.oppgave.oppgaveReferanse,
                "uttalelse" to oppgavebekreftelseMottatt.oppgave.uttalelse.somMap(),
                "kontrollerRegisterInntektOppgave" to mapOf(
                    "fraOgMed" to DATE_FORMATTER.format(oppgavebekreftelseMottatt.oppgave.fraOgMed),
                    "månedÅr" to "${oppgavebekreftelseMottatt.oppgave.fraOgMed.month.somNorskMåned()} ${oppgavebekreftelseMottatt.oppgave.fraOgMed.year}",
                    "tilOgMed" to DATE_FORMATTER.format(oppgavebekreftelseMottatt.oppgave.tilOgMed),
                    "registerinntekt" to oppgavebekreftelseMottatt.oppgave.registerinntekt.somMap(),
                    "spørsmål" to "Har du tilbakemelding på lønnen?",
                ),
            ),
            "søknadMottattDag" to oppgavebekreftelseMottatt.mottatt.withZoneSameInstant(OSLO_ZONE_ID).somNorskDag(),
            "søknadMottatt" to DATE_TIME_FORMATTER.format(oppgavebekreftelseMottatt.mottatt),
            "søker" to oppgavebekreftelseMottatt.søker.somMap(),
            "hjelp" to mapOf(
                "språk" to k9Format.språk.kode?.språkTilTekst(),
            ),
        )
    }

    private fun RegisterinntektDTO.somMap() = mapOf(
        "arbeidOgFrilansInntekter" to this.arbeidOgFrilansInntekter.map {
            mapOf(
                "inntekt" to BigDecimal.valueOf(it.inntekt.toLong()).formaterSomValuta(),
                "arbeidsgiver" to it.arbeidsgiverIdentifikator,
                "navn" to it.arbeidsgiverNavn,
            )
        },
        "ytelseInntekter" to this.ytelseInntekter.map {
            mapOf(
                "inntekt" to BigDecimal.valueOf(it.inntekt.toLong()).formaterSomValuta(),
                "ytelsetype" to it.ytelsetype.tekst(),
            )
        },
    )

    private fun YtelseType.tekst() = when (this) {
        YtelseType.PLEIEPENGER -> "Pleiepenger"
        YtelseType.OMSORGSPENGER -> "Omsorgspenger"
        YtelseType.OPPLÆRINGSPENGER -> "Opplæringspenger"
        YtelseType.SYKEPENGER -> "Sykepenger"
        YtelseType.DAGPENGER -> "Dagpenger"
        YtelseType.FORELDREPENGER -> "Foreldrepenger"
        YtelseType.AAP -> "Arbeidsavklaringspenger"
        YtelseType.ANNET -> "Annet"
    }

    private fun AktivitetspengerOppgaveUttalelseDTO.somMap() = mapOf(
        "harUttalelse" to harUttalelse,
        "uttalelseFraDeltaker" to uttalelseFraDeltaker,
    )
}
