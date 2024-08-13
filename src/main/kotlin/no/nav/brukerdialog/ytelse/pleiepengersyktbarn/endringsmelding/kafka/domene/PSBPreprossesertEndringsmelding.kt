package no.nav.brukerdialog.meldinger.endringsmelding.domene

import no.nav.k9.søknad.Søknad
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.dittnavvarsel.K9Beskjed
import no.nav.brukerdialog.domenetjenester.mottak.Preprosessert
import no.nav.brukerdialog.integrasjon.k9joark.JournalføringsRequest
import no.nav.brukerdialog.ytelse.fellesdomene.Navn
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import java.time.ZonedDateTime

data class PSBPreprossesertEndringsmelding(
    val søker: Søker,
    val pleietrengendeNavn: String,
    val k9FormatSøknad: Søknad,
    val dokumentId: List<List<String>>
): Preprosessert {
    internal constructor(
        endringsmelding: PSBEndringsmeldingMottatt,
        dokumentId: List<List<String>>,
        k9Format: Søknad
    ) : this(
        søker = endringsmelding.søker,
        pleietrengendeNavn = endringsmelding.pleietrengendeNavn,
        k9FormatSøknad = k9Format,
        dokumentId = dokumentId
    )

    override fun ytelse(): Ytelse = Ytelse.PLEIEPENGER_SYKT_BARN_ENDRINGSMELDING

    override fun mottattDato(): ZonedDateTime = k9FormatSøknad.mottattDato

    override fun søkerNavn(): Navn = søker.fullnavn()

    override fun søkerFødselsnummer(): String = søker.fødselsnummer

    override fun k9FormatSøknad(): Søknad = k9FormatSøknad

    override fun dokumenter(): List<List<String>> = dokumentId

    override fun tilJournaførigsRequest(): JournalføringsRequest = JournalføringsRequest(
        ytelse = ytelse(),
        norskIdent = søkerFødselsnummer(),
        sokerNavn = søkerNavn(),
        mottatt = mottattDato(),
        dokumentId = dokumenter()
    )

    override fun tilK9DittnavVarsel(metadata: MetaInfo): K9Beskjed? = null
}
