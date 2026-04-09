package no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.inntektsrapportering.domene

import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.domenetjenester.mottak.MottattMelding
import no.nav.brukerdialog.domenetjenester.mottak.PreprosesseringsData
import no.nav.brukerdialog.pdf.PdfData
import no.nav.brukerdialog.ytelse.aktivitetspenger.pdf.AktivitetspengerInntektsrapporteringPdfData
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.inntektsrapportering.OppgittInntektForPeriode
import no.nav.k9.søknad.Søknad
import java.time.ZonedDateTime

data class AktivitetspengerInntektsrapporteringMottatt(
    val oppgaveReferanse: String,
    val søker: Søker,
    val oppgittInntektForPeriode: OppgittInntektForPeriode,
    val mottatt: ZonedDateTime,
    val k9Format: Søknad,
) : MottattMelding {
    override fun ytelse(): Ytelse = Ytelse.AKTIVITETSPENGER_INNTEKTSRAPPORTERING

    override fun søkerFødselsnummer(): String = søker.fødselsnummer

    override fun k9FormatSøknad(): Søknad = k9Format

    override fun vedleggId(): List<String> = listOf()

    override fun fødselsattestVedleggId(): List<String> = listOf()

    override fun mapTilPreprosessert(dokumentId: List<List<String>>) =
        AktivitetspengerInntektsrapporteringPreprosessert(
            aktivitetspengerInntektsrapporteringMottatt = this,
            dokumentId = dokumentId
        )

    override fun pdfData(): PdfData = AktivitetspengerInntektsrapporteringPdfData(this)

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
