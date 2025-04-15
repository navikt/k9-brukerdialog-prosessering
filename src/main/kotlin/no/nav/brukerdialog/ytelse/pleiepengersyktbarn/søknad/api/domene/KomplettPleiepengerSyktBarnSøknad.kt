package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene

import no.nav.k9.søknad.Søknad
import no.nav.brukerdialog.domenetjenester.innsending.KomplettInnsending
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.fosterhjemgodtgjørelse.Fosterhjemgodtgjørelse
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.omsorgsstønad.Omsorgsstønad
import java.time.LocalDate
import java.time.ZonedDateTime

data class KomplettPleiepengerSyktBarnSøknad(
    val apiDataVersjon: String? = null,
    val språk: Språk? = null,
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val søker: Søker,
    val barn: BarnDetaljer,
    val arbeidsgivere: List<Arbeidsgiver>,
    var vedleggId: List<String> = listOf(),
    val fødselsattestVedleggId: List<String>,
    val medlemskap: Medlemskap,
    val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden,
    val opptjeningIUtlandet: List<OpptjeningIUtlandet>,
    val utenlandskNæring: List<UtenlandskNæring>,
    val ferieuttakIPerioden: FerieuttakIPerioden?,
    val harForståttRettigheterOgPlikter: Boolean,
    val harBekreftetOpplysninger: Boolean,
    val omsorgstilbud: Omsorgstilbud? = null,
    val nattevåk: Nattevåk?,
    val beredskap: Beredskap?,
    val frilans: Frilans,

    @Deprecated("StønadGodtgjørelse er deprecated og vil bli fjernet i fremtidige versjoner av APIet")
    val stønadGodtgjørelse: StønadGodtgjørelse? = null,

    val fosterhjemgodtgjørelse: Fosterhjemgodtgjørelse? = null,
    val omsorgsstønad: Omsorgsstønad? = null,

    val selvstendigNæringsdrivende: SelvstendigNæringsdrivende,
    val barnRelasjon: BarnRelasjon? = null,
    val barnRelasjonBeskrivelse: String? = null,
    val harVærtEllerErVernepliktig: Boolean? = null,
    val k9FormatSøknad: Søknad? = null,
) : KomplettInnsending {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KomplettPleiepengerSyktBarnSøknad

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
