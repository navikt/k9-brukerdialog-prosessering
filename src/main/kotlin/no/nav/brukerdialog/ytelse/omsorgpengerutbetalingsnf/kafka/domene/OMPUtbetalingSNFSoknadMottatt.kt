package no.nav.brukerdialog.meldinger.omsorgpengerutbetalingsnf.domene

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonValue
import no.nav.k9.søknad.Søknad
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.domenetjenester.mottak.MottattMelding
import no.nav.brukerdialog.domenetjenester.mottak.PreprosesseringsData
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingsnf.kafka.domene.OMPUtbetalingSNFSoknadPreprosessert
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingsnf.pdf.OMPUtbetalingSNFSoknadPDFData
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime

data class OMPUtbetalingSNFSoknadMottatt(
    val søknadId: String,
    val mottatt: ZonedDateTime,
    val søker: Søker,
    val språk: String,
    val harDekketTiFørsteDagerSelv: Boolean? = null,
    val harSyktBarn: Boolean? = null,
    val harAleneomsorg: Boolean? = null,
    val bosteder: List<Bosted>,
    val opphold: List<Opphold>,
    val spørsmål: List<SpørsmålOgSvar>,
    val utbetalingsperioder: List<Utbetalingsperiode>,
    val barn: List<Barn> = listOf(),
    val vedleggId: List<String> = listOf(),
    val titler: List<String>? = null,
    val frilans: Frilans? = null,
    val selvstendigNæringsdrivende: SelvstendigNæringsdrivende? = null,
    val erArbeidstakerOgså: Boolean,
    val bekreftelser: Bekreftelser,
    val k9FormatSøknad: Søknad
): MottattMelding {
    override fun ytelse(): Ytelse = Ytelse.OMSORGSPENGER_UTBETALING_SNF

    override fun søkerFødselsnummer(): String = søker.fødselsnummer

    override fun k9FormatSøknad() = k9FormatSøknad

    override fun vedleggId(): List<String> = vedleggId

    override fun fødselsattestVedleggId(): List<String> = listOf()

    override fun mapTilPreprosessert(dokumentId: List<List<String>>) = OMPUtbetalingSNFSoknadPreprosessert(
        melding = this,
        dokumentId = dokumentId
    )

    override fun pdfData() = OMPUtbetalingSNFSoknadPDFData(this)

    override fun mapTilPreprosesseringsData() = PreprosesseringsData(
        søkerFødselsnummer = søkerFødselsnummer(),
        k9FormatSøknad = k9FormatSøknad(),
        vedleggId = vedleggId(),
        fødselsattestVedleggId = fødselsattestVedleggId(),
        pdfData = pdfData(),
        pdfJournalføringsTittel = ytelse().tittel,
        jsonJournalføringsTittel = "${ytelse().tittel}(JSON)",
    )
}

data class Barn(
    val navn: String,
    val fødselsdato: LocalDate,
    val identitetsnummer: String,
    val type: TypeBarn,
    val aktørId: String? = null
)

enum class TypeBarn(val pdfTekst: String?){
    FOSTERBARN("(Fosterbarn)"),
    ANNET("(Annet)"),
    FRA_OPPSLAG(null)
}

data class Bekreftelser(
    val harBekreftetOpplysninger: JaNei,
    val harForståttRettigheterOgPlikter: JaNei
)

data class Frilans(
    @JsonFormat(pattern = "yyyy-MM-dd") val startdato: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val sluttdato: LocalDate? = null,
    val jobberFortsattSomFrilans: Boolean
)

data class SelvstendigNæringsdrivende(
    val næringstype: Næringstyper,
    val fiskerErPåBladB: JaNei? = JaNei.Nei,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val tilOgMed: LocalDate? = null,
    val næringsinntekt: Int? = null,
    val navnPåVirksomheten: String,
    val organisasjonsnummer: String? = null,
    val registrertINorge: JaNei,
    val registrertIUtlandet: Land? = null,
    val erNyoppstartet: Boolean,
    val yrkesaktivSisteTreFerdigliknedeÅrene: YrkesaktivSisteTreFerdigliknedeÅrene? = null,
    val varigEndring: VarigEndring? = null,
    val regnskapsfører: Regnskapsfører? = null,
    val harFlereAktiveVirksomheter: Boolean
) {
    override fun toString(): String {
        return "Virksomhet()"
    }
}

/**
 * ISO 3166 alpha-3 landkode.
 *
 * @see https://en.wikipedia.org/wiki/ISO_3166-1_alpha-3
 */
data class Land(val landkode: String, val landnavn: String)

/**
 * Unngå `Boolean` default-verdi null -> false
 */
enum class JaNei (@get:JsonValue val boolean: Boolean) {
    Ja(true),
    Nei(false);

    companion object {
        @JsonCreator
        @JvmStatic
        fun fraBoolean(boolean: Boolean?) = when(boolean) {
            true -> Ja
            false -> Nei
            else -> throw IllegalStateException("Kan ikke være null")
        }
    }
}

data class YrkesaktivSisteTreFerdigliknedeÅrene(
    val oppstartsdato: LocalDate
)

enum class Næringstyper(val beskrivelse: String) {
    FISKE("Fiske"),
    JORDBRUK_SKOGBRUK("Jordbruk/skogbruk"),
    DAGMAMMA("Dagmamma eller familiebarnehage i eget hjem"),
    ANNEN("Annen");
}

data class VarigEndring(
    @JsonFormat(pattern = "yyyy-MM-dd")
    val dato: LocalDate,
    val inntektEtterEndring: Int,
    val forklaring: String
)

data class Regnskapsfører(
    val navn: String,
    val telefon: String
)

data class Utbetalingsperiode(
    @JsonFormat(pattern = "yyyy-MM-dd") val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val tilOgMed: LocalDate,
    val antallTimerBorte: Duration? = null,
    val antallTimerPlanlagt: Duration? = null,
    val årsak: FraværÅrsak? = null,
    val aktivitetFravær: List<AktivitetFravær> = listOf()
)

enum class FraværÅrsak {
    STENGT_SKOLE_ELLER_BARNEHAGE,
    SMITTEVERNHENSYN,
    ORDINÆRT_FRAVÆR
}

enum class AktivitetFravær {
    FRILANSER,
    SELVSTENDIG_VIRKSOMHET
}

data class Bosted(
    @JsonFormat(pattern = "yyyy-MM-dd") val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val tilOgMed: LocalDate,
    val landkode: String,
    val landnavn: String,
    val erEØSLand: JaNei
)

typealias Opphold = Bosted

data class SpørsmålOgSvar(
    val spørsmål: Spørsmål,
    val svar: JaNei
)

typealias Spørsmål = String
