package no.nav.brukerdialog.ytelse.ungdomsytelse.pdf

import no.nav.brukerdialog.common.Constants.DATE_FORMATTER
import no.nav.brukerdialog.common.Constants.DATE_TIME_FORMATTER
import no.nav.brukerdialog.common.Constants.OSLO_ZONE_ID
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.pdf.PdfData
import no.nav.brukerdialog.utils.DateUtils.somNorskDag
import no.nav.brukerdialog.utils.StringUtils.språkTilTekst
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.BekreftelseSvar
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.KomplettEndretSluttdatoUngdomsytelseOppgaveDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.KomplettEndretStartdatoUngdomsytelseOppgaveDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.KomplettUngdomsytelseOppgaveDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.UngdomsytelseIkkeGodkjentResponse
import no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.oppgavebekreftelse.domene.UngdomsytelseOppgavebekreftelseMottatt
import no.nav.k9.søknad.felles.type.Språk

class UngdomsytelseOppgavebekreftelsePdfData(private val oppgavebekreftelseMottatt: UngdomsytelseOppgavebekreftelseMottatt) :
    PdfData() {
    override fun ytelse(): Ytelse = Ytelse.UNGDOMSYTELSE_OPPGAVEBEKREFTELSE

    override fun språk(): Språk = Språk.NORSK_BOKMÅL

    override fun pdfData(): Map<String, Any?> {
        val k9Format = oppgavebekreftelseMottatt.k9Format
        return mapOf(
            "tittel" to ytelse().utledTittel(språk()),
            "deltakelseId" to oppgavebekreftelseMottatt.deltakelseId,
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
        "oppgaveId" to oppgaveId,
        "veilederRef" to veilederRef,
        "meldingFraVeileder" to meldingFraVeileder,
        "endretStartdatoOppgave" to when (this) {
            is KomplettEndretStartdatoUngdomsytelseOppgaveDTO -> mapOf(
                "nyStartdato" to DATE_FORMATTER.format(nyStartdato),
                "bekreftelseSvar" to bekreftelseSvar.somJaNeiSvar(),
                "ikkeGodkjentResponse" to ikkeGodkjentResponse?.somMap()
            )

            else -> null
        },
        "endretSluttdatoOppgave" to when (this) {
            is KomplettEndretSluttdatoUngdomsytelseOppgaveDTO -> mapOf(
                "nySluttdato" to DATE_FORMATTER.format(nySluttdato),
                "bekreftelseSvar" to bekreftelseSvar.somJaNeiSvar(),
                "ikkeGodkjentResponse" to ikkeGodkjentResponse?.somMap()
            )

            else -> null
        }
    )

    private fun UngdomsytelseIkkeGodkjentResponse.somMap() = mapOf(
        "korrigertDato" to DATE_FORMATTER.format(korrigertDato),
        "kontaktVeilederSvar" to kontaktVeilederSvar,
        "meldingFraDeltaker" to meldingFraDeltaker
    )

    private fun BekreftelseSvar.somJaNeiSvar() = when (this) {
        BekreftelseSvar.GODTAR -> "Ja"
        BekreftelseSvar.AVSLÅR -> "Nei"
    }

}
