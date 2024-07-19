package no.nav.k9brukerdialogprosessering.meldinger.omsorgspengerkronisksyktbarn.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogprosessering.common.MetaInfo
import no.nav.k9brukerdialogprosessering.common.Ytelse
import no.nav.k9brukerdialogprosessering.dittnavvarsel.K9Beskjed
import no.nav.k9brukerdialogprosessering.innsending.Preprosessert
import no.nav.k9brukerdialogprosessering.journalforing.JournalføringsRequest
import no.nav.k9brukerdialogprosessering.meldinger.felles.domene.Navn
import no.nav.k9brukerdialogprosessering.meldinger.felles.domene.Søker
import java.time.ZonedDateTime
import java.util.*

data class OMPUTVKroniskSyktBarnSøknadPreprosesssert(
    val soknadId: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    val mottatt: ZonedDateTime,
    val språk: String?,
    val dokumentId: List<List<String>>,
    val kroniskEllerFunksjonshemming: Boolean,
    val barn: Barn,
    val søker: Søker,
    val relasjonTilBarnet: SøkerBarnRelasjon? = null,
    val sammeAdresse: BarnSammeAdresse? = null,
    val høyereRisikoForFravær: Boolean? = null, // TODO: Fjern nullable når lansert
    val høyereRisikoForFraværBeskrivelse: String? = null,
    val harBekreftetOpplysninger: Boolean,
    val harForståttRettigheterOgPlikter: Boolean,
    val k9FormatSøknad: Søknad
): Preprosessert {
    internal constructor(
        melding: OMPUTVKroniskSyktBarnSøknadMottatt,
        dokumentId: List<List<String>>
    ) : this(
        språk = melding.språk,
        soknadId = melding.søknadId,
        mottatt = melding.mottatt,
        dokumentId = dokumentId,
        kroniskEllerFunksjonshemming = melding.kroniskEllerFunksjonshemming,
        søker = melding.søker,
        sammeAdresse = melding.sammeAdresse,
        høyereRisikoForFravær = melding.høyereRisikoForFravær,
        høyereRisikoForFraværBeskrivelse = melding.høyereRisikoForFraværBeskrivelse,
        barn = melding.barn,
        relasjonTilBarnet = melding.relasjonTilBarnet,
        harForståttRettigheterOgPlikter = melding.harForståttRettigheterOgPlikter,
        harBekreftetOpplysninger = melding.harBekreftetOpplysninger,
        k9FormatSøknad = melding.k9FormatSøknad
    )

    override fun ytelse(): Ytelse = Ytelse.OMSORGSPENGER_UTVIDET_RETT

    override fun mottattDato(): ZonedDateTime = mottatt

    override fun søkerNavn(): Navn = Navn(søker.fornavn, søker.mellomnavn, søker.etternavn)

    override fun søkerFødselsnummer(): String = søker.fødselsnummer

    override fun k9FormatSøknad(): Søknad = k9FormatSøknad

    override fun dokumenter(): List<List<String>> = dokumentId

    override fun tilJournaførigsRequest(): JournalføringsRequest = JournalføringsRequest(
        mottatt = mottattDato(),
        norskIdent = søkerFødselsnummer(),
        sokerNavn = søkerNavn(),
        ytelse = ytelse(),
        dokumentId = dokumenter()
    )

    override fun tilK9DittnavVarsel(metadata: MetaInfo): K9Beskjed = K9Beskjed(
        metadata = metadata,
        grupperingsId = soknadId,
        tekst = "Vi har mottatt søknad fra deg om ekstra omsorgsdager ved kronisk sykt eller funksjonshemmet barn.",
        link = null,
        dagerSynlig = 7,
        søkerFødselsnummer = søkerFødselsnummer(),
        eventId = UUID.randomUUID().toString(),
        ytelse = "OMSORGSPENGER_UTV_KS" // TODO: Bytt til Ytelse.OMSORGSPENGER_UTVIDET_RETT når det er på plass i k9-dittnav-varsel
    )

    override fun toString(): String {
        return "PreprosessertMeldingV1(soknadId='$soknadId', mottatt=$mottatt)"
    }

}
