package no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene

import no.nav.helse.felles.Omsorgstilbud
import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogprosessering.common.Ytelse
import no.nav.k9brukerdialogprosessering.innsending.MottattMelding
import no.nav.k9brukerdialogprosessering.innsending.PreprosesseringsData
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.PSBSøknadPdfData
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Arbeidsgiver
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Barn
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.BarnRelasjon
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Beredskap
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.FerieuttakIPerioden
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Frilans
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Medlemskap
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Nattevåk
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.OpptjeningIUtlandet
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.SelvstendigNæringsdrivende
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.StønadGodtgjørelse
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.Søker
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.UtenlandskNæring
import no.nav.k9brukerdialogprosessering.meldinger.pleiepengersyktbarn.domene.felles.UtenlandsoppholdIPerioden
import java.time.LocalDate
import java.time.ZonedDateTime

data class PSBMottattSøknad(
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val apiDataVersjon: String? = null,
    val språk: String? = null,
    val fraOgMed : LocalDate,
    val tilOgMed : LocalDate,
    val søker : Søker,
    val barn : Barn,
    var vedleggId : List<String> = listOf(),
    val fødselsattestVedleggId: List<String>? = listOf(), // TODO: Fjern nullabel etter lansering.
    val medlemskap: Medlemskap,
    val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden,
    val ferieuttakIPerioden: FerieuttakIPerioden?,
    val opptjeningIUtlandet: List<OpptjeningIUtlandet>,
    val utenlandskNæring: List<UtenlandskNæring>,
    val harForståttRettigheterOgPlikter : Boolean,
    val harBekreftetOpplysninger : Boolean,
    val omsorgstilbud: Omsorgstilbud? = null,
    val beredskap: Beredskap?,
    val nattevåk: Nattevåk?,
    val frilans: Frilans,
    val stønadGodtgjørelse: StønadGodtgjørelse? = null,
    val selvstendigNæringsdrivende: SelvstendigNæringsdrivende,
    val arbeidsgivere: List<Arbeidsgiver>,
    val barnRelasjon: BarnRelasjon? = null,
    val barnRelasjonBeskrivelse: String? = null,
    val harVærtEllerErVernepliktig: Boolean? = null,
    val k9FormatSøknad: Søknad
): MottattMelding {
    override fun ytelse(): Ytelse = Ytelse.PLEIEPENGER_SYKT_BARN

    override fun søkerFødselsnummer(): String = søker.fødselsnummer

    override fun k9FormatSøknad(): Søknad = k9FormatSøknad

    override fun vedleggId(): List<String> = vedleggId

    override fun fødselsattestVedleggId(): List<String> = fødselsattestVedleggId ?: listOf()

    override fun mapTilPreprosessert(dokumentId: List<List<String>>) = PSBPreprosessertSøknad(
        melding = this,
        dokumentId = dokumentId
    )

    override fun pdfData() = PSBSøknadPdfData(this)

    override fun mapTilPreprosesseringsData() = PreprosesseringsData(
        søkerFødselsnummer = søker.fødselsnummer,
        k9FormatSøknad = k9FormatSøknad,
        vedleggId = vedleggId,
        fødselsattestVedleggId = fødselsattestVedleggId,
        pdfJournalføringsTittel = "Søknad om pleiepenger",
        jsonJournalføringsTittel = "Søknad om pleiepenger som JSON",
        pdfData = pdfData()
    )
}
