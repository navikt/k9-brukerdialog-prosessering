package no.nav.brukerdialog.ytelse.ungdomsytelse.pdf

import no.nav.brukerdialog.common.Constants.DATE_FORMATTER
import no.nav.brukerdialog.common.Constants.DATE_TIME_FORMATTER
import no.nav.brukerdialog.common.Constants.OSLO_ZONE_ID
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.pdf.PdfData
import no.nav.brukerdialog.utils.DateUtils.somNorskDag
import no.nav.brukerdialog.utils.NumberUtils.formaterSomValuta
import no.nav.brukerdialog.utils.StringUtils.språkTilTekst
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.BekreftelseSvar
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.KomplettEndretPeriodeUngdomsytelseOppgaveDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.KomplettKontrollerRegisterInntektOppgaveTypeDataDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.KomplettUngdomsytelseOppgaveDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.UngdomsytelseOppgaveUttalelseDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.oppgavebekreftelse.domene.UngdomsytelseOppgavebekreftelseMottatt
import no.nav.k9.søknad.felles.type.Språk
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.RegisterinntektDTO
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.registerinntekt.YtelseType
import java.math.BigDecimal

class UngdomsytelseOppgavebekreftelsePdfData(private val oppgavebekreftelseMottatt: UngdomsytelseOppgavebekreftelseMottatt) :
    PdfData() {
    override fun ytelse(): Ytelse = Ytelse.UNGDOMSYTELSE_OPPGAVEBEKREFTELSE

    override fun språk(): Språk = Språk.NORSK_BOKMÅL

    override fun pdfData(): Map<String, Any?> {
        val k9Format = oppgavebekreftelseMottatt.k9Format
        return mapOf(
            "tittel" to ytelse().utledTittel(språk()),
            "oppgave" to oppgavebekreftelseMottatt.oppgave.somMap(),
            "søknadMottattDag" to oppgavebekreftelseMottatt.mottatt.withZoneSameInstant(OSLO_ZONE_ID).somNorskDag(),
            "søknadMottatt" to DATE_TIME_FORMATTER.format(oppgavebekreftelseMottatt.mottatt),
            "søker" to oppgavebekreftelseMottatt.søker.somMap(),
            "hjelp" to mapOf(
                "språk" to k9Format.språk.kode?.språkTilTekst()
            )
        )
    }

    private fun KomplettUngdomsytelseOppgaveDTO.somMap() = mapOf(
        "oppgaveReferanse" to oppgaveReferanse,
        "uttalelse" to uttalelse.somMap(),
        "endretPeriodeOppgave" to when (this) {
            is KomplettEndretPeriodeUngdomsytelseOppgaveDTO -> mapOf(
                "nyStartdato" to DATE_FORMATTER.format(nyStartdato),
                "nySluttdato" to (nySluttdato?.let { DATE_FORMATTER.format(it) }),
            )

            else -> null
        },

        "kontrollerRegisterInntektOppgave" to when (this) {
            is KomplettKontrollerRegisterInntektOppgaveTypeDataDTO -> mapOf(
                "fraOgMed" to DATE_FORMATTER.format(fraOgMed),
                "tilOgMed" to DATE_FORMATTER.format(tilOgMed),
                "registerinntekt" to registerinntekt.somMap(),
            )

            else -> null
        }
    )

    private fun RegisterinntektDTO.somMap() = mapOf(
        "arbeidOgFrilansInntekter" to this.arbeidOgFrilansInntekter.map {
            mapOf(
                "inntekt" to BigDecimal.valueOf(it.inntekt.toLong()).formaterSomValuta(),
                "arbeidsgiver" to it.arbeidsgiver
            )
        },
        "ytelseInntekter" to this.ytelseInntekter.map {
            mapOf(
                "inntekt" to BigDecimal.valueOf(it.inntekt.toLong()).formaterSomValuta(),
                "ytelsetype" to it.ytelsetype.tekst()
            )
        }
    )

    private fun YtelseType.tekst() = when (this) {
        YtelseType.PLEIEPENGER_SYKT_BARN -> "Pleiepenger sykt barn"
        YtelseType.PLEIEPENGER_LIVETS_SLUTTFASE -> "Skoleplass uten barnepass"
        YtelseType.OMSORGSPENGER -> "Omsorgspenger"
        YtelseType.OPPLAERINGSPENGER -> "Opplæringspenger"
        YtelseType.SYKEPENGER -> "Sykepenger"
    }

    private fun UngdomsytelseOppgaveUttalelseDTO.somMap() = mapOf(
        "bekreftelseSvar" to bekreftelseSvar.somJaNeiSvar(),
        "meldingFraDeltaker" to meldingFraDeltaker
    )

    private fun BekreftelseSvar.somJaNeiSvar() = when (this) {
        BekreftelseSvar.GODTAR -> "Ja"
        BekreftelseSvar.AVSLÅR -> "Nei"
    }

}
