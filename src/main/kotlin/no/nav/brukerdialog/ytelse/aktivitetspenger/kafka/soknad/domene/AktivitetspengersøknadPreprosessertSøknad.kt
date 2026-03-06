package no.nav.brukerdialog.ytelse.aktivitetspenger.kafka.soknad.domene

import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.dittnavvarsel.K9Beskjed
import no.nav.brukerdialog.domenetjenester.mottak.JournalføringsService
import no.nav.brukerdialog.domenetjenester.mottak.Preprosessert
import no.nav.brukerdialog.integrasjon.dokarkiv.dto.YtelseType
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.soknad.Barn
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.soknad.KontonummerInfo
import no.nav.brukerdialog.ytelse.fellesdomene.Navn
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.k9.søknad.Søknad
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as K9Søknad

data class AktivitetspengersøknadPreprosessertSøknad(
    val oppgaveReferanse: String,
    val mottatt: ZonedDateTime,
    val språk: String?,
    val søker: Søker,
    val startdato: LocalDate? = null,
    val barn: List<Barn>,
    val barnErRiktig: Boolean,
    val kontonummerInfo: KontonummerInfo,
    val dokumentId: List<List<String>>,
    val k9Format: K9Søknad,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
) : Preprosessert {
    internal constructor(
        aktivitetspengerSøknadMottatt: AktivitetspengersøknadMottatt,
        dokumentId: List<List<String>>,
    ) : this(
        språk = aktivitetspengerSøknadMottatt.språk,
        oppgaveReferanse = aktivitetspengerSøknadMottatt.søknadId,
        mottatt = aktivitetspengerSøknadMottatt.mottatt,
        søker = aktivitetspengerSøknadMottatt.søker,
        startdato = aktivitetspengerSøknadMottatt.startdato,
        barn = aktivitetspengerSøknadMottatt.barn,
        barnErRiktig = aktivitetspengerSøknadMottatt.barnErRiktig,
        kontonummerInfo = aktivitetspengerSøknadMottatt.kontonummerInfo,
        dokumentId = dokumentId,
        k9Format = aktivitetspengerSøknadMottatt.k9Format,
        harForståttRettigheterOgPlikter = aktivitetspengerSøknadMottatt.harForståttRettigheterOgPlikter,
        harBekreftetOpplysninger = aktivitetspengerSøknadMottatt.harBekreftetOpplysninger
    )

    override fun ytelse(): Ytelse = Ytelse.AKTIVITETSPENGER

    override fun mottattDato(): ZonedDateTime = mottatt

    override fun søkerNavn(): Navn = Navn(søker.fornavn, søker.mellomnavn, søker.etternavn)

    override fun søkerFødselsnummer(): String = søker.fødselsnummer

    override fun k9FormatSøknad(): Søknad = k9Format

    override fun dokumenter(): List<List<String>> = dokumentId

    override fun tilJournaførigsRequest(): JournalføringsService.JournalføringsRequest {

        return JournalføringsService.JournalføringsRequest(
            ytelseType = YtelseType.AKTIVITETSPENGER_SØKNAD,
            norskIdent = søkerFødselsnummer(),
            sokerNavn = søkerNavn(),
            mottatt = mottatt,
            dokumentId = dokumenter()
        )
    }

    override fun tilK9DittnavVarsel(metadata: MetaInfo): K9Beskjed = K9Beskjed(
        metadata = metadata,
        grupperingsId = oppgaveReferanse,
        tekst = "Søknad om aktivitetspenger er mottatt",
        link = null,
        dagerSynlig = 7,
        søkerFødselsnummer = søkerFødselsnummer(),
        eventId = UUID.randomUUID().toString(),
        ytelse = "AKTIVITETSPENGER"
    )
}
