package no.nav.k9brukerdialogprosessering.meldinger.omsorgspengeraleneomsorg.domene

import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogprosessering.common.Ytelse
import no.nav.k9brukerdialogprosessering.dittnavvarsel.K9Beskjed
import no.nav.k9brukerdialogprosessering.innsending.Preprosessert
import no.nav.k9brukerdialogprosessering.journalforing.JournalføringsRequest
import no.nav.k9brukerdialogprosessering.kafka.types.Metadata
import no.nav.k9brukerdialogprosessering.meldinger.felles.domene.Søker
import java.time.ZonedDateTime
import java.util.*

data class OMPAleneomsorgSoknadPreprosessert(
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val språk: String?,
    val dokumentId: List<List<String>>,
    val søker: Søker,
    val barn: Barn,
    val k9Søknad: Søknad,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean
): Preprosessert {
    internal constructor(
        melding: OMPAleneomsorgSoknadMottatt,
        dokumentId: List<List<String>>,
    ) : this(
        språk = melding.språk,
        søknadId = melding.søknadId,
        mottatt = melding.mottatt,
        dokumentId = dokumentId,
        søker = melding.søker,
        barn = melding.barn,
        k9Søknad = melding.k9Søknad,
        harForståttRettigheterOgPlikter = melding.harForståttRettigheterOgPlikter,
        harBekreftetOpplysninger = melding.harBekreftetOpplysninger
    )

    override fun ytelse() = Ytelse.OMSORGSDAGER_ALENEOMSORG

    override fun mottattDato(): ZonedDateTime = mottatt

    override fun søkerNavn() = søker.fullnavn()

    override fun søkerFødselsnummer(): String = søker.fødselsnummer

    override fun k9FormatSøknad() = k9Søknad

    override fun dokumenter(): List<List<String>> = dokumentId

    override fun tilJournaførigsRequest() = JournalføringsRequest(
        ytelse = ytelse(),
        correlationId = søknadId,
        norskIdent = søkerFødselsnummer(),
        sokerNavn = søkerNavn(),
        mottatt = mottattDato(),
        dokumentId = dokumenter()
    )

    override fun tilK9DittnavVarsel(metadata: Metadata): K9Beskjed = K9Beskjed(
        metadata = metadata,
        grupperingsId = søknadId,
        tekst = "Vi har mottatt din melding om registrering av aleneomsorg.",
        link = null,
        dagerSynlig = 7,
        søkerFødselsnummer = søkerFødselsnummer(),
        eventId = UUID.randomUUID().toString(),
        ytelse = "OMSORGSDAGER_ALENEOMSORG"
    )
}
