package no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.dittnavvarsel.K9Beskjed
import no.nav.brukerdialog.domenetjenester.mottak.JournalføringsService
import no.nav.brukerdialog.domenetjenester.mottak.Preprosessert
import no.nav.brukerdialog.integrasjon.dokarkiv.dto.YtelseType
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.felles.*
import no.nav.k9.søknad.Søknad
import java.time.LocalDate
import java.time.ZonedDateTime

data class OLPPreprosessertSøknad(
    val apiDataVersjon: String? = null,
    val språk: String,
    val søknadId: String,
    val dokumentId: List<List<String>>,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    val mottatt: ZonedDateTime,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val søker: Søker,
    val barn: Barn,
    val medlemskap: Medlemskap,
    val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden?, // TODO: fjern nullable når vi har lansert og mellomlagring inneholder dette feltet.
    val opptjeningIUtlandet: List<OpptjeningIUtlandet>,
    val utenlandskNæring: List<UtenlandskNæring>,
    val ferieuttakIPerioden: FerieuttakIPerioden?,
    val frilans: Frilans? = null,
    val stønadGodtgjørelse: StønadGodtgjørelse? = null,
    val selvstendigNæringsdrivende: SelvstendigNæringsdrivende? = null,
    val arbeidsgivere: List<Arbeidsgiver>,
    val harForstattRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val harVærtEllerErVernepliktig: Boolean? = null,
    val k9FormatSøknad: Søknad,
) : Preprosessert {
    constructor(
        melding: OLPMottattSøknad,
        dokumentId: List<List<String>>,
    ) : this(
        språk = melding.språk,
        søknadId = melding.søknadId,
        dokumentId = dokumentId,
        mottatt = melding.mottatt,
        fraOgMed = melding.fraOgMed,
        tilOgMed = melding.tilOgMed,
        søker = melding.søker,
        barn = melding.barn,
        medlemskap = melding.medlemskap,
        utenlandsoppholdIPerioden = melding.utenlandsoppholdIPerioden,
        frilans = melding.frilans,
        stønadGodtgjørelse = melding.stønadGodtgjørelse,
        selvstendigNæringsdrivende = melding.selvstendigNæringsdrivende,
        opptjeningIUtlandet = melding.opptjeningIUtlandet,
        utenlandskNæring = melding.utenlandskNæring,
        arbeidsgivere = melding.arbeidsgivere,
        harForstattRettigheterOgPlikter = melding.harForståttRettigheterOgPlikter,
        harBekreftetOpplysninger = melding.harBekreftetOpplysninger,
        ferieuttakIPerioden = melding.ferieuttakIPerioden,
        harVærtEllerErVernepliktig = melding.harVærtEllerErVernepliktig,
        k9FormatSøknad = melding.k9FormatSøknad
    )

    override fun ytelse(): Ytelse = Ytelse.OPPLÆRINGSPENGER

    override fun mottattDato(): ZonedDateTime = mottatt

    override fun søkerNavn() = søker.fullnavn()

    override fun søkerFødselsnummer(): String = søker.fødselsnummer

    override fun k9FormatSøknad(): Søknad = k9FormatSøknad

    override fun dokumenter(): List<List<String>> = dokumentId
    override fun tilJournaførigsRequest(): JournalføringsService.JournalføringsRequest {
        return JournalføringsService.JournalføringsRequest(
            ytelseType = YtelseType.OPPLÆRINGSPENGERSØKNAD,
            norskIdent = søkerFødselsnummer(),
            sokerNavn = søkerNavn(),
            mottatt = mottattDato(),
            dokumentId = dokumenter()
        )
    }

    override fun tilK9DittnavVarsel(metadata: MetaInfo): K9Beskjed? = null
}
