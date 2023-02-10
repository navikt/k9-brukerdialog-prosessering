package no.nav.k9brukerdialogprosessering.endringsmelding.domene

import no.nav.helse.felles.Søker
import no.nav.helse.felles.tilTpsNavn
import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogprosessering.common.Ytelse
import no.nav.k9brukerdialogprosessering.innsending.Preprosessert
import no.nav.k9brukerdialogprosessering.journalforing.JournalføringsRequest
import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene.felles.Navn
import java.time.ZonedDateTime

data class PSBPreprossesertEndringsmelding(
    val søker: Søker,
    val k9FormatSøknad: Søknad,
    val dokumentId: List<List<String>>
): Preprosessert {
    internal constructor(
        endringsmelding: PSBEndringsmeldingMottatt,
        dokumentId: List<List<String>>,
        k9Format: Søknad
    ) : this(
        søker = endringsmelding.søker,
        k9FormatSøknad = k9Format,
        dokumentId = dokumentId
    )

    override fun ytelse(): Ytelse = Ytelse.PLEIEPENGER_SYKT_BARN_ENDRINGSMELDING

    override fun mottattDato(): ZonedDateTime = k9FormatSøknad.mottattDato

    override fun søkerNavn(): Navn = søker.tilTpsNavn()

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
}
