package no.nav.brukerdialog.ytelse.opplæringspenger.api.domene

import no.nav.brukerdialog.domenetjenester.innsending.KomplettInnsending
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid.ArbeidsgiverOLP
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid.FrilansOLP
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid.SelvstendigNæringsdrivendeOLP
import no.nav.k9.søknad.Søknad
import java.time.LocalDate
import java.time.ZonedDateTime

data class KomplettOpplæringspengerSøknad(
    val apiDataVersjon: String? = null,
    val språk: Språk? = null,
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val søker: Søker,
    val barn: BarnDetaljer,
    val arbeidsgivere: List<ArbeidsgiverOLP>,
    var vedleggId: List<String> = listOf(),
    val fødselsattestVedleggId: List<String>,
    val medlemskap: Medlemskap,
    val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden?, // TODO: fjern nullable når vi har lansert og mellomlagring inneholder dette feltet.
    val opptjeningIUtlandet: List<OpptjeningIUtlandet>,
    val utenlandskNæring: List<UtenlandskNæring>,
    val ferieuttakIPerioden: FerieuttakIPerioden?,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val frilans: FrilansOLP? = null,
    val stønadGodtgjørelse: StønadGodtgjørelse? = null,
    val selvstendigNæringsdrivende: SelvstendigNæringsdrivendeOLP? = null,
    val harVærtEllerErVernepliktig: Boolean? = null,
    val ettersendingAvVedlegg: EttersendingAvVedlegg? = null, // TODO: fjern nullable når vi har lansert ettersendingAvVedlegg og mellomlagring inneholder dette feltet.
    val kurs: Kurs,
    val k9FormatSøknad: Søknad? = null,
) : KomplettInnsending {
    override fun innsendingId(): String = søknadId

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KomplettOpplæringspengerSøknad

        if (søknadId != other.søknadId) return false
        if (mottatt != other.mottatt) return false
        if (fraOgMed != other.fraOgMed) return false
        if (tilOgMed != other.tilOgMed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = søknadId.hashCode()
        result = 31 * result + mottatt.hashCode()
        result = 31 * result + fraOgMed.hashCode()
        result = 31 * result + tilOgMed.hashCode()
        return result
    }
}
