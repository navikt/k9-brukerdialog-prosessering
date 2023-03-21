package no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Barn
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.BarnRelasjon
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Beredskap
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.FerieuttakIPerioden
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Frilans
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Medlemskap
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Nattevåk
import no.nav.helse.felles.Omsorgstilbud
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.OpptjeningIUtlandet
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.SelvstendigNæringsdrivende
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Søker
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.UtenlandskNæring
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.UtenlandsoppholdIPerioden
import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogprosessering.common.Ytelse
import no.nav.k9brukerdialogprosessering.innsending.Preprosessert
import no.nav.k9brukerdialogprosessering.journalforing.JournalføringsRequest
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Arbeidsgiver
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Navn
import java.time.LocalDate
import java.time.ZonedDateTime

data class PSBPreprosessertSøknad(
    val apiDataVersjon: String? = null,
    val språk: String?,
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
        melding: PSBMottattSøknad,
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

    override fun ytelse(): Ytelse = Ytelse.PLEIEPENGER_SYKT_BARN

    override fun mottattDato(): ZonedDateTime = mottatt

    override fun søkerNavn(): Navn =
        Navn(fornavn = søker.fornavn, mellomnavn = søker.mellomnavn, etternavn = søker.etternavn)

    override fun søkerFødselsnummer(): String = søker.fødselsnummer

    override fun k9FormatSøknad(): Søknad = k9FormatSøknad

    override fun dokumenter(): List<List<String>> = dokumentId
    override fun tilJournaførigsRequest(): JournalføringsRequest {
        return JournalføringsRequest(
            ytelse = ytelse(),
            norskIdent = søkerFødselsnummer(),
            sokerNavn = søkerNavn(),
            mottatt = mottattDato(),
            dokumentId = dokumenter()
        )
    }
}
