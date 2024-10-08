package no.nav.brukerdialog.meldinger.omsorgspengeraleneomsorg.domene

import no.nav.k9.søknad.Søknad
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.dittnavvarsel.K9Beskjed
import no.nav.brukerdialog.domenetjenester.mottak.JournalføringsService
import no.nav.brukerdialog.domenetjenester.mottak.Preprosessert
import no.nav.brukerdialog.integrasjon.dokarkiv.dto.YtelseType
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
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

    override fun tilJournaførigsRequest() = JournalføringsService.JournalføringsRequest(
        ytelseType = YtelseType.OMSORGSDAGER_ALENEOMSORG,
        correlationId = søknadId,
        norskIdent = søkerFødselsnummer(),
        sokerNavn = søkerNavn(),
        mottatt = mottattDato(),
        dokumentId = dokumenter()
    )

    override fun tilK9DittnavVarsel(metadata: MetaInfo): K9Beskjed = K9Beskjed(
        metadata = metadata,
        grupperingsId = søknadId,
        tekst = "Vi har mottatt søknad fra deg om ekstra omsorgsdager ved aleneomsorg.",
        link = null,
        dagerSynlig = 7,
        søkerFødselsnummer = søkerFødselsnummer(),
        eventId = UUID.randomUUID().toString(),
        ytelse = "OMSORGSDAGER_ALENEOMSORG"
    )
}
