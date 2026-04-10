package no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.oppgavebekreftelse.domene

import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.domenetjenester.mottak.MottattMelding
import no.nav.brukerdialog.domenetjenester.mottak.PreprosesseringsData
import no.nav.brukerdialog.pdf.PdfData
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.oppgavebekreftelse.KomplettAktivitetspengerOppgaveDTO
import no.nav.brukerdialog.ytelse.aktivitetspenger.pdf.AktivitetspengerOppgavebekreftelsePdfData
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.k9.oppgave.OppgaveBekreftelse
import java.time.ZonedDateTime

data class AktivitetspengerOppgavebekreftelseMottatt(
    val mottatt: ZonedDateTime,
    val oppgave: KomplettAktivitetspengerOppgaveDTO,
    val språk: String? = "nb",
    val søker: Søker,
    val k9Format: OppgaveBekreftelse,
) : MottattMelding {
    override fun ytelse(): Ytelse = Ytelse.AKTIVITETSPENGER_OPPGAVEBEKREFTELSE

    override fun søkerFødselsnummer(): String = søker.fødselsnummer

    override fun k9FormatSøknad(): OppgaveBekreftelse = k9Format

    override fun vedleggId(): List<String> = listOf()

    override fun fødselsattestVedleggId(): List<String> = listOf()

    override fun mapTilPreprosessert(dokumentId: List<List<String>>) =
        AktivitetspengerOppgavebekreftelsePreprosessert(
            aktivitetspengerOppgavebekreftelseMottatt = this,
            dokumentId = dokumentId,
        )

    override fun pdfData(): PdfData = AktivitetspengerOppgavebekreftelsePdfData(this)

    override fun mapTilPreprosesseringsData(): PreprosesseringsData {
        val tittel = ytelse().tittel + oppgave.dokumentTittelSuffix()
        return PreprosesseringsData(
            søkerFødselsnummer = søkerFødselsnummer(),
            k9FormatSøknad = k9FormatSøknad(),
            vedleggId = vedleggId(),
            fødselsattestVedleggId = fødselsattestVedleggId(),
            pdfData = pdfData(),
            pdfJournalføringsTittel = tittel,
            jsonJournalføringsTittel = "$tittel(JSON)",
        )
    }
}
