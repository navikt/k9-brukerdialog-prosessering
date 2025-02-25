package no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.inntektsrapportering.domene

import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.dittnavvarsel.K9Beskjed
import no.nav.brukerdialog.domenetjenester.mottak.JournalføringsService
import no.nav.brukerdialog.domenetjenester.mottak.Preprosessert
import no.nav.brukerdialog.integrasjon.dokarkiv.dto.YtelseType
import no.nav.brukerdialog.ytelse.fellesdomene.Navn
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.inntektsrapportering.OppgittInntektForPeriode
import no.nav.k9.søknad.Søknad
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as K9Søknad

data class UngdomsytelseInntektsrapporteringPreprosessert(
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val søker: Søker,
    val oppgittInntektForPeriode: OppgittInntektForPeriode,
    val harBekreftetInntekt: Boolean,
    val dokumentId: List<List<String>>,
    val k9Format: K9Søknad,
) : Preprosessert {
    internal constructor(
        ungdomsytelseInntektsrapporteringMottatt: UngdomsytelseInntektsrapporteringMottatt,
        dokumentId: List<List<String>>,
    ) : this(
        søknadId = ungdomsytelseInntektsrapporteringMottatt.søknadId,
        mottatt = ungdomsytelseInntektsrapporteringMottatt.mottatt,
        søker = ungdomsytelseInntektsrapporteringMottatt.søker,
        oppgittInntektForPeriode = ungdomsytelseInntektsrapporteringMottatt.oppgittInntektForPeriode,
        dokumentId = dokumentId,
        k9Format = ungdomsytelseInntektsrapporteringMottatt.k9Format,
        harBekreftetInntekt = ungdomsytelseInntektsrapporteringMottatt.harBekreftetInntekt
    )

    override fun ytelse(): Ytelse = Ytelse.UNGDOMSYTELSE_INNTEKTSRAPPORTERING

    override fun mottattDato(): ZonedDateTime = mottatt

    override fun søkerNavn(): Navn = Navn(søker.fornavn, søker.mellomnavn, søker.etternavn)

    override fun søkerFødselsnummer(): String = søker.fødselsnummer

    override fun k9FormatSøknad(): Søknad = k9Format

    override fun dokumenter(): List<List<String>> = dokumentId

    override fun tilJournaførigsRequest(): JournalføringsService.JournalføringsRequest {

        return JournalføringsService.JournalføringsRequest(
            ytelseType = YtelseType.UNGDOMSYTELSE_INNTEKTRAPPORTERING,
            norskIdent = søkerFødselsnummer(),
            sokerNavn = søkerNavn(),
            mottatt = mottatt,
            dokumentId = dokumenter()
        )
    }

    override fun tilK9DittnavVarsel(metadata: MetaInfo): K9Beskjed = K9Beskjed(
        metadata = metadata,
        grupperingsId = søknadId,
        tekst = "Rapportert inntenkt for ungdomsytelsen er mottatt",
        link = null,
        dagerSynlig = 7,
        søkerFødselsnummer = søkerFødselsnummer(),
        eventId = UUID.randomUUID().toString(),
        ytelse = "UNGDOMSYTELSE"
    )
}
