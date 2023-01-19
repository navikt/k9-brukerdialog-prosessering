package no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.helse.felles.Barn
import no.nav.helse.felles.BarnRelasjon
import no.nav.helse.felles.Beredskap
import no.nav.helse.felles.FerieuttakIPerioden
import no.nav.helse.felles.Frilans
import no.nav.helse.felles.Medlemskap
import no.nav.helse.felles.Nattevåk
import no.nav.helse.felles.Omsorgstilbud
import no.nav.helse.felles.OpptjeningIUtlandet
import no.nav.helse.felles.SelvstendigNæringsdrivende
import no.nav.helse.felles.Søker
import no.nav.helse.felles.UtenlandskNæring
import no.nav.helse.felles.UtenlandsoppholdIPerioden
import no.nav.k9.søknad.Søknad
import no.nav.k9brukerdialogprosessering.common.Ytelse
import no.nav.k9brukerdialogprosessering.innsending.MottattMelding
import no.nav.k9brukerdialogprosessering.innsending.PreprosesseringsData
import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.PSBSøknadPdfData
import no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene.felles.Arbeidsgiver
import java.time.LocalDate
import java.time.ZonedDateTime

data class PSBMottattSøknad(
    val søknadId: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
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
    val harMedsøker : Boolean,
    val samtidigHjemme: Boolean? = null,
    val harForståttRettigheterOgPlikter : Boolean,
    val harBekreftetOpplysninger : Boolean,
    val omsorgstilbud: Omsorgstilbud? = null,
    val beredskap: Beredskap?,
    val nattevåk: Nattevåk?,
    val frilans: Frilans,
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
