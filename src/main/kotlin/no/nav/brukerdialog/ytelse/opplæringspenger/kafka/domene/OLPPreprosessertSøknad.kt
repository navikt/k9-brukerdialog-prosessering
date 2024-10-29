package no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.helse.felles.Omsorgstilbud
import no.nav.k9.søknad.Søknad
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.dittnavvarsel.K9Beskjed
import no.nav.brukerdialog.domenetjenester.mottak.JournalføringsService
import no.nav.brukerdialog.domenetjenester.mottak.Preprosessert
import no.nav.brukerdialog.integrasjon.dokarkiv.dto.YtelseType
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Arbeidsgiver
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Barn
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.BarnRelasjon
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Beredskap
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.FerieuttakIPerioden
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Frilans
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Medlemskap
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Nattevåk
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.OpptjeningIUtlandet
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.SelvstendigNæringsdrivende
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.StønadGodtgjørelse
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.UtenlandskNæring
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.UtenlandsoppholdIPerioden
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
    val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden,
    val opptjeningIUtlandet: List<OpptjeningIUtlandet>,
    val utenlandskNæring: List<UtenlandskNæring>,
    val ferieuttakIPerioden: FerieuttakIPerioden?,
    val beredskap: Beredskap?,
    val nattevåk: Nattevåk?,
    val omsorgstilbud: Omsorgstilbud? = null,
    val frilans: Frilans? = null,
    val stønadGodtgjørelse: StønadGodtgjørelse? = null,
    val selvstendigNæringsdrivende: SelvstendigNæringsdrivende? = null,
    val arbeidsgivere: List<Arbeidsgiver>,
    val barnRelasjon: BarnRelasjon? = null,
    val barnRelasjonBeskrivelse: String? = null,
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
        beredskap = melding.beredskap,
        nattevåk = melding.nattevåk,
        omsorgstilbud = melding.omsorgstilbud,
        frilans = melding.frilans,
        stønadGodtgjørelse = melding.stønadGodtgjørelse,
        selvstendigNæringsdrivende = melding.selvstendigNæringsdrivende,
        opptjeningIUtlandet = melding.opptjeningIUtlandet,
        utenlandskNæring = melding.utenlandskNæring,
        arbeidsgivere = melding.arbeidsgivere,
        harForstattRettigheterOgPlikter = melding.harForståttRettigheterOgPlikter,
        harBekreftetOpplysninger = melding.harBekreftetOpplysninger,
        utenlandsoppholdIPerioden = melding.utenlandsoppholdIPerioden,
        ferieuttakIPerioden = melding.ferieuttakIPerioden,
        barnRelasjon = melding.barnRelasjon,
        barnRelasjonBeskrivelse = melding.barnRelasjonBeskrivelse,
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
