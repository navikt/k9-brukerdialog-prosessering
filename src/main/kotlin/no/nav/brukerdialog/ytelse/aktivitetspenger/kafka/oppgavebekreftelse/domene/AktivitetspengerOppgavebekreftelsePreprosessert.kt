package no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.oppgavebekreftelse.domene

import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.dittnavvarsel.K9Beskjed
import no.nav.brukerdialog.domenetjenester.mottak.JournalføringsService
import no.nav.brukerdialog.domenetjenester.mottak.Preprosessert
import no.nav.brukerdialog.integrasjon.dokarkiv.dto.YtelseType
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.oppgavebekreftelse.KomplettAktivitetspengerOppgaveDTO
import no.nav.brukerdialog.ytelse.fellesdomene.Navn
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.k9.oppgave.OppgaveBekreftelse
import java.time.ZonedDateTime
import java.util.UUID

data class AktivitetspengerOppgavebekreftelsePreprosessert(
    val mottatt: ZonedDateTime,
    val oppgave: KomplettAktivitetspengerOppgaveDTO,
    val språk: String?,
    val søker: Søker,
    val dokumentId: List<List<String>>,
    val k9Format: OppgaveBekreftelse,
) : Preprosessert {
    internal constructor(
        aktivitetspengerOppgavebekreftelseMottatt: AktivitetspengerOppgavebekreftelseMottatt,
        dokumentId: List<List<String>>,
    ) : this(
        språk = aktivitetspengerOppgavebekreftelseMottatt.språk,
        oppgave = aktivitetspengerOppgavebekreftelseMottatt.oppgave,
        mottatt = aktivitetspengerOppgavebekreftelseMottatt.mottatt,
        søker = aktivitetspengerOppgavebekreftelseMottatt.søker,
        dokumentId = dokumentId,
        k9Format = aktivitetspengerOppgavebekreftelseMottatt.k9Format,
    )

    override fun ytelse(): Ytelse = Ytelse.AKTIVITETSPENGER_OPPGAVEBEKREFTELSE

    override fun mottattDato(): ZonedDateTime = mottatt

    override fun søkerNavn(): Navn = Navn(søker.fornavn, søker.mellomnavn, søker.etternavn)

    override fun søkerFødselsnummer(): String = søker.fødselsnummer

    override fun k9FormatSøknad(): OppgaveBekreftelse = k9Format

    override fun dokumenter(): List<List<String>> = dokumentId

    override fun tilJournaførigsRequest(): JournalføringsService.JournalføringsRequest {
        return JournalføringsService.JournalføringsRequest(
            ytelseType = YtelseType.AKTIVITETSPENGER_VARSEL_UTTALELSE,
            norskIdent = søkerFødselsnummer(),
            sokerNavn = søkerNavn(),
            mottatt = mottatt,
            dokumentId = dokumenter(),
        )
    }

    override fun tilK9DittnavVarsel(metadata: MetaInfo): K9Beskjed = K9Beskjed(
        metadata = metadata,
        grupperingsId = oppgave.oppgaveReferanse,
        tekst = "Bekreftelse om aktivitetspengeropplysninger er mottatt",
        link = null,
        dagerSynlig = 7,
        søkerFødselsnummer = søkerFødselsnummer(),
        eventId = UUID.randomUUID().toString(),
        ytelse = "AKTIVITETSPENGER",
    )
}
