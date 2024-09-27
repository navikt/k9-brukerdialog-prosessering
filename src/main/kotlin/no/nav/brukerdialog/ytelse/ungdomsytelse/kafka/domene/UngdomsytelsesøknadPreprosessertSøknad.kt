package no.nav.brukerdialog.ytelse.ungdomsytelse.kafka.domene

import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.dittnavvarsel.K9Beskjed
import no.nav.brukerdialog.domenetjenester.mottak.Preprosessert
import no.nav.brukerdialog.integrasjon.k9joark.JournalføringsRequest
import no.nav.brukerdialog.ytelse.fellesdomene.Navn
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.k9.søknad.Søknad
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as K9Søknad

data class UngdomsytelsesøknadPreprosessertSøknad(
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val språk: String?,
    val søker: Søker,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val dokumentId: List<List<String>>,
    val k9Format: K9Søknad,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
) : Preprosessert {
    internal constructor(
        ungdomsytelseSøknadMottatt: UngdomsytelsesøknadMottatt,
        dokumentId: List<List<String>>,
    ) : this(
        språk = ungdomsytelseSøknadMottatt.språk,
        søknadId = ungdomsytelseSøknadMottatt.søknadId,
        mottatt = ungdomsytelseSøknadMottatt.mottatt,
        søker = ungdomsytelseSøknadMottatt.søker,
        fraOgMed = ungdomsytelseSøknadMottatt.fraOgMed,
        tilOgMed = ungdomsytelseSøknadMottatt.tilOgMed,
        dokumentId = dokumentId,
        k9Format = ungdomsytelseSøknadMottatt.k9Format,
        harForståttRettigheterOgPlikter = ungdomsytelseSøknadMottatt.harForståttRettigheterOgPlikter,
        harBekreftetOpplysninger = ungdomsytelseSøknadMottatt.harBekreftetOpplysninger
    )

    override fun ytelse(): Ytelse = Ytelse.UNGDOMSYTELSE

    override fun mottattDato(): ZonedDateTime = mottatt

    override fun søkerNavn(): Navn = Navn(søker.fornavn, søker.mellomnavn, søker.etternavn)

    override fun søkerFødselsnummer(): String = søker.fødselsnummer

    override fun k9FormatSøknad(): Søknad = k9Format

    override fun dokumenter(): List<List<String>> = dokumentId

    override fun tilJournaførigsRequest(): JournalføringsRequest = JournalføringsRequest(
        ytelse = ytelse(),
        norskIdent = søkerFødselsnummer(),
        sokerNavn = søkerNavn(),
        mottatt = mottatt,
        dokumentId = dokumenter()
    )

    override fun tilK9DittnavVarsel(metadata: MetaInfo): K9Beskjed = K9Beskjed(
        metadata = metadata,
        grupperingsId = søknadId,
        tekst = "Søknad om ungdomsytelse er mottatt",
        link = null,
        dagerSynlig = 7,
        søkerFødselsnummer = søkerFødselsnummer(),
        eventId = UUID.randomUUID().toString(),
        ytelse = "UNGDOMSYTELSE"
    )
}
