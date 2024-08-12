package no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene

import no.nav.k9.søknad.Søknad
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.dittnavvarsel.K9Beskjed
import no.nav.brukerdialog.innsending.Preprosessert
import no.nav.brukerdialog.journalforing.JournalføringsRequest
import no.nav.brukerdialog.meldinger.felles.domene.Navn
import no.nav.brukerdialog.meldinger.felles.domene.Søker
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as K9Søknad

data class PilsPreprosessertSøknad(
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val språk: String?,
    val søker: Søker,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val dokumentId: List<List<String>>,
    val pleietrengende: Pleietrengende,
    val arbeidsgivere: List<Arbeidsgiver>,
    val medlemskap: Medlemskap,
    val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden,
    val opptjeningIUtlandet: List<OpptjeningIUtlandet>,
    val utenlandskNæring: List<UtenlandskNæring>,
    val frilans: Frilans?,
    val selvstendigNæringsdrivende: SelvstendigNæringsdrivende?,
    val harVærtEllerErVernepliktig: Boolean? = null,
    val k9Format: K9Søknad,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val flereSokere: FlereSokereSvar? = null
): Preprosessert {
    internal constructor(
        pilsSøknadMottatt: PilsSøknadMottatt,
        dokumentId: List<List<String>>,
    ) : this(
        språk = pilsSøknadMottatt.språk,
        søknadId = pilsSøknadMottatt.søknadId,
        mottatt = pilsSøknadMottatt.mottatt,
        søker = pilsSøknadMottatt.søker,
        fraOgMed = pilsSøknadMottatt.fraOgMed,
        tilOgMed = pilsSøknadMottatt.tilOgMed,
        dokumentId = dokumentId,
        pleietrengende = pilsSøknadMottatt.pleietrengende,
        arbeidsgivere = pilsSøknadMottatt.arbeidsgivere,
        medlemskap = pilsSøknadMottatt.medlemskap,
        utenlandsoppholdIPerioden = pilsSøknadMottatt.utenlandsoppholdIPerioden,
        opptjeningIUtlandet = pilsSøknadMottatt.opptjeningIUtlandet,
        utenlandskNæring = pilsSøknadMottatt.utenlandskNæring,
        frilans = pilsSøknadMottatt.frilans,
        selvstendigNæringsdrivende = pilsSøknadMottatt.selvstendigNæringsdrivende,
        harVærtEllerErVernepliktig = pilsSøknadMottatt.harVærtEllerErVernepliktig,
        k9Format = pilsSøknadMottatt.k9Format,
        harForståttRettigheterOgPlikter = pilsSøknadMottatt.harForståttRettigheterOgPlikter,
        harBekreftetOpplysninger = pilsSøknadMottatt.harBekreftetOpplysninger,
        flereSokere = pilsSøknadMottatt.flereSokere
    )

    override fun ytelse(): Ytelse = Ytelse.PLEIEPENGER_LIVETS_SLUTTFASE

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
        tekst = "Søknad om pleiepenger i livets sluttfase er mottatt",
        link = null,
        dagerSynlig = 7,
        søkerFødselsnummer = søkerFødselsnummer(),
        eventId = UUID.randomUUID().toString(),
        ytelse = "PLEIEPENGER_LIVETS_SLUTTFASE"
    )
}
