package no.nav.brukerdialog.meldinger.omsorgspengermidlertidigalene.domene

import no.nav.k9.søknad.Søknad
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.dittnavvarsel.K9Beskjed
import no.nav.brukerdialog.innsending.Preprosessert
import no.nav.brukerdialog.journalforing.JournalføringsRequest
import no.nav.brukerdialog.meldinger.felles.domene.Søker
import java.time.ZonedDateTime
import java.util.*

data class OMPMidlertidigAleneSoknadPreprosessert(
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val språk: String?,
    val dokumentId: List<List<String>>,
    val søker: Søker,
    val annenForelder: AnnenForelder,
    val barn: List<Barn>,
    val k9Format: Søknad,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean
): Preprosessert {
    internal constructor(
        melding: OMPMidlertidigAleneSoknadMottatt,
        dokumentId: List<List<String>>
    ) : this(
        språk = melding.språk,
        søknadId = melding.søknadId,
        mottatt = melding.mottatt,
        dokumentId = dokumentId,
        søker = melding.søker,
        annenForelder = melding.annenForelder,
        barn = melding.barn,
        k9Format = melding.k9Format,
        harForståttRettigheterOgPlikter = melding.harForståttRettigheterOgPlikter,
        harBekreftetOpplysninger = melding.harBekreftetOpplysninger
    )

    override fun ytelse() = Ytelse.OMSORGSPENGER_MIDLERTIDIG_ALENE

    override fun mottattDato(): ZonedDateTime = mottatt

    override fun søkerNavn() = søker.fullnavn()

    override fun søkerFødselsnummer(): String = søker.fødselsnummer

    override fun k9FormatSøknad() = k9Format

    override fun dokumenter(): List<List<String>> = dokumentId

    override fun tilJournaførigsRequest() = JournalføringsRequest(
        ytelse = ytelse(),
        norskIdent = søkerFødselsnummer(),
        sokerNavn = søkerNavn(),
        mottatt = mottattDato(),
        dokumentId = dokumenter()
    )

    override fun tilK9DittnavVarsel(metadata: MetaInfo): K9Beskjed = K9Beskjed(
        metadata = metadata,
        grupperingsId = søknadId,
        tekst = "Vi har mottatt søknad fra deg om ekstra omsorgsdager når den andre forelderen ikke kan ha tilsyn med barn.",
        link = null,
        dagerSynlig = 7,
        søkerFødselsnummer = søkerFødselsnummer(),
        eventId = UUID.randomUUID().toString(),
        ytelse = "OMSORGSPENGER_MIDLERTIDIG_ALENE"
    )
}
