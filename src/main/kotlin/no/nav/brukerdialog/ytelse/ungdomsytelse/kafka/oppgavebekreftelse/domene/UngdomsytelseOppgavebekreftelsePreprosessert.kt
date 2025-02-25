package no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.oppgavebekreftelse.domene

import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.dittnavvarsel.K9Beskjed
import no.nav.brukerdialog.domenetjenester.mottak.JournalføringsService
import no.nav.brukerdialog.domenetjenester.mottak.Preprosessert
import no.nav.brukerdialog.integrasjon.dokarkiv.dto.YtelseType
import no.nav.brukerdialog.ytelse.fellesdomene.Navn
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.UngdomsytelseOppgaveDTO
import no.nav.k9.søknad.Søknad
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as K9Søknad

data class UngdomsytelseOppgavebekreftelsePreprosessert(
    val deltakelseId: String,
    val mottatt: ZonedDateTime,
    val oppgave: UngdomsytelseOppgaveDTO,
    val språk: String?,
    val søker: Søker,
    val dokumentId: List<List<String>>,
    val k9Format: K9Søknad,
) : Preprosessert {
    internal constructor(
        ungdomsytelseSøknadMottatt: UngdomsytelseOppgavebekreftelseMottatt,
        dokumentId: List<List<String>>,
    ) : this(
        språk = ungdomsytelseSøknadMottatt.språk,
        deltakelseId = ungdomsytelseSøknadMottatt.deltakelseId,
        oppgave = ungdomsytelseSøknadMottatt.oppgave,
        mottatt = ungdomsytelseSøknadMottatt.mottatt,
        søker = ungdomsytelseSøknadMottatt.søker,
        dokumentId = dokumentId,
        k9Format = ungdomsytelseSøknadMottatt.k9Format,
    )

    override fun ytelse(): Ytelse = Ytelse.UNGDOMSYTELSE_OPPGAVEBEKREFTELSE

    override fun mottattDato(): ZonedDateTime = mottatt

    override fun søkerNavn(): Navn = Navn(søker.fornavn, søker.mellomnavn, søker.etternavn)

    override fun søkerFødselsnummer(): String = søker.fødselsnummer

    override fun k9FormatSøknad(): Søknad = k9Format

    override fun dokumenter(): List<List<String>> = dokumentId

    override fun tilJournaførigsRequest(): JournalføringsService.JournalføringsRequest {

        return JournalføringsService.JournalføringsRequest(
            ytelseType = YtelseType.UNGDOMSYTELSE_OPPGAVEBEKREFTELSE,
            norskIdent = søkerFødselsnummer(),
            sokerNavn = søkerNavn(),
            mottatt = mottatt,
            dokumentId = dokumenter()
        )
    }

    override fun tilK9DittnavVarsel(metadata: MetaInfo): K9Beskjed = K9Beskjed(
        metadata = metadata,
        grupperingsId = oppgave.oppgaveId,
        tekst = "Bekrftelse om endring av ungdomsprogramopplysninger er mottatt",
        link = null,
        dagerSynlig = 7,
        søkerFødselsnummer = søkerFødselsnummer(),
        eventId = UUID.randomUUID().toString(),
        ytelse = "UNGDOMSYTELSE"
    )
}
