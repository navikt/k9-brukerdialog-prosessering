package no.nav.brukerdialog.meldinger.omsorgpengerutbetalingsnf.domene

import no.nav.k9.søknad.Søknad
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.dittnavvarsel.K9Beskjed
import no.nav.brukerdialog.domenetjenester.mottak.Preprosessert
import no.nav.brukerdialog.integrasjon.k9joark.JournalføringsRequest
import no.nav.brukerdialog.meldinger.felles.domene.Navn
import no.nav.brukerdialog.meldinger.felles.domene.Søker
import java.time.ZonedDateTime
import java.util.*

data class OMPUtbetalingSNFSoknadPreprosessert(
    val soknadId: String,
    val mottatt: ZonedDateTime,
    val søker: Søker,
    val språk: String?,
    val harDekketTiFørsteDagerSelv: Boolean? = null,
    val harSyktBarn: Boolean? = null,
    val harAleneomsorg: Boolean? = null,
    val bosteder: List<Bosted>,
    val opphold: List<Opphold>,
    val spørsmål: List<SpørsmålOgSvar>,
    val dokumentId: List<List<String>>,
    val utbetalingsperioder: List<Utbetalingsperiode>,
    val barn: List<Barn>,
    val frilans: Frilans? = null,
    val selvstendigNæringsdrivende: SelvstendigNæringsdrivende? = null,
    val bekreftelser: Bekreftelser,
    val k9FormatSøknad: Søknad
): Preprosessert {
    internal constructor(
        melding: OMPUtbetalingSNFSoknadMottatt,
        dokumentId: List<List<String>>,
    ) : this(
        soknadId = melding.søknadId,
        mottatt = melding.mottatt,
        søker = melding.søker,
        språk = melding.språk,
        bosteder = melding.bosteder,
        opphold = melding.opphold,
        spørsmål = melding.spørsmål,
        dokumentId = dokumentId,
        harDekketTiFørsteDagerSelv = melding.harDekketTiFørsteDagerSelv,
        harSyktBarn = melding.harSyktBarn,
        harAleneomsorg = melding.harAleneomsorg,
        utbetalingsperioder = melding.utbetalingsperioder,
        barn = melding.barn,
        frilans = melding.frilans,
        selvstendigNæringsdrivende = melding.selvstendigNæringsdrivende,
        bekreftelser = melding.bekreftelser,
        k9FormatSøknad = melding.k9FormatSøknad
    )

    override fun ytelse(): Ytelse = Ytelse.OMSORGSPENGER_UTBETALING_SNF

    override fun mottattDato(): ZonedDateTime = mottatt

    override fun søkerNavn(): Navn = Navn(søker.fornavn, søker.mellomnavn, søker.etternavn)

    override fun søkerFødselsnummer(): String = søker.fødselsnummer

    override fun k9FormatSøknad() = k9FormatSøknad

    override fun dokumenter(): List<List<String>> = dokumentId

    override fun tilJournaførigsRequest(): JournalføringsRequest = JournalføringsRequest(
        ytelse = Ytelse.OMSORGSPENGER_UTBETALING_SNF,
        norskIdent = søkerFødselsnummer(),
        sokerNavn = søkerNavn(),
        mottatt = mottattDato(),
        dokumentId = dokumenter()
    )

    override fun tilK9DittnavVarsel(metadata: MetaInfo) = K9Beskjed(
        metadata = metadata,
        grupperingsId = soknadId,
        tekst = "Søknad om utbetaling av omsorgspenger er mottatt.",
        link = null,
        dagerSynlig = 7,
        søkerFødselsnummer = søkerFødselsnummer(),
        eventId = UUID.randomUUID().toString(),
        ytelse = "OMSORGSPENGER_UT_SNF" // TODO: Bytt til OMSORGSPENGER_UTBETALING_SNF når det er støttet i k9-dittnav-varsel
    )
}
