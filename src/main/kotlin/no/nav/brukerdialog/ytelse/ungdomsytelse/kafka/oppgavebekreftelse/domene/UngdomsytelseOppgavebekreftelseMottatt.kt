package no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.oppgavebekreftelse.domene

import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.domenetjenester.mottak.MottattMelding
import no.nav.brukerdialog.domenetjenester.mottak.PreprosesseringsData
import no.nav.brukerdialog.pdf.PdfData
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.brukerdialog.ytelse.ungdomsytelse.pdf.UngdomsytelseOppgavebekreftelsePdfData
import no.nav.brukerdialog.ytelse.ungdomsytelse.pdf.UngdomsytelsesøknadPdfData
import no.nav.k9.søknad.Søknad
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID

data class UngdomsytelseOppgavebekreftelseMottatt(
    val deltakelseId: UUID,
    val oppgaveId: UUID,
    val mottatt: ZonedDateTime,
    val språk: String? = "nb",
    val søker: Søker,
    val k9Format: Søknad,
) : MottattMelding {
    override fun ytelse(): Ytelse = Ytelse.UNGDOMSYTELSE_OPPGAVEBEKREFTELSE

    override fun søkerFødselsnummer(): String = søker.fødselsnummer

    override fun k9FormatSøknad(): Søknad = k9Format

    override fun vedleggId(): List<String> = listOf()

    override fun fødselsattestVedleggId(): List<String> = listOf()

    override fun mapTilPreprosessert(dokumentId: List<List<String>>) = UngdomsytelseOppgavebekreftelsePreprosessert(
        ungdomsytelseSøknadMottatt = this,
        dokumentId = dokumentId
    )

    override fun pdfData(): PdfData = UngdomsytelseOppgavebekreftelsePdfData(this)

    override fun mapTilPreprosesseringsData(): PreprosesseringsData = PreprosesseringsData(
        søkerFødselsnummer = søkerFødselsnummer(),
        k9FormatSøknad = k9FormatSøknad(),
        vedleggId = vedleggId(),
        fødselsattestVedleggId = fødselsattestVedleggId(),
        pdfData = pdfData(),
        pdfJournalføringsTittel = ytelse().tittel,
        jsonJournalføringsTittel = "${ytelse().tittel}(JSON)",
    )
}
