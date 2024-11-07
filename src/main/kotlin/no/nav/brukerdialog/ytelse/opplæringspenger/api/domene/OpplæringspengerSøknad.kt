package no.nav.brukerdialog.ytelse.opplæringspenger.api.domene

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import no.nav.brukerdialog.domenetjenester.innsending.Innsending
import no.nav.brukerdialog.ytelse.Ytelse
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.integrasjon.k9mellomlagring.dokumentId
import no.nav.brukerdialog.oppslag.barn.BarnOppslag
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.UtenlandskNæring.Companion.valider
import no.nav.brukerdialog.utils.StringUtils
import no.nav.brukerdialog.utils.krever
import no.nav.brukerdialog.validation.ValidationErrorResponseException
import no.nav.brukerdialog.validation.ValidationProblemDetailsString
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.Arbeidsgiver.Companion.somK9Arbeidstaker
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.Kurs.Companion.tilK9Format
import no.nav.fpsak.tidsserie.LocalDateInterval
import no.nav.k9.søknad.SøknadValidator
import no.nav.k9.søknad.felles.Kildesystem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.opptjening.OpptjeningAktivitet
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.DataBruktTilUtledning
import no.nav.k9.søknad.ytelse.olp.v1.Opplæringspenger
import no.nav.k9.søknad.ytelse.olp.v1.OpplæringspengerSøknadValidator
import no.nav.k9.søknad.ytelse.psb.v1.Omsorg
import no.nav.k9.søknad.ytelse.psb.v1.Uttak
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstid
import java.net.URL
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as K9Søknad
import no.nav.k9.søknad.felles.type.Periode as K9Periode
import no.nav.k9.søknad.felles.type.Språk as K9Språk

enum class Språk { nb, nn }

private val k9FormatVersjon = Versjon.of("1.0.0")

data class OpplæringspengerSøknad(
    val newVersion: Boolean?,
    val apiDataVersjon: String? = null,
    @field:org.hibernate.validator.constraints.UUID(message = "Forventet gyldig UUID, men var '\${validatedValue}'") val søknadId: String = UUID.randomUUID()
        .toString(),
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    val språk: Språk,
    val søkerNorskIdent: String? = null, // TODO: Fjern nullable når vi har lansert og mellomlagring inneholder dette feltet.
    @field:Valid val barn: BarnDetaljer,
    @field:Valid val arbeidsgivere: List<Arbeidsgiver>,
    val vedlegg: List<URL> = listOf(), // TODO: Fjern listof() når krav om legeerklæring er påkrevd igjen.
    val fødselsattestVedleggUrls: List<URL>? = listOf(),
    @JsonFormat(pattern = "yyyy-MM-dd")
    val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val tilOgMed: LocalDate,
    val medlemskap: Medlemskap,
    val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden,
    val ferieuttakIPerioden: FerieuttakIPerioden?,
    val opptjeningIUtlandet: List<OpptjeningIUtlandet>,
    val utenlandskNæring: List<UtenlandskNæring>,

    @field:AssertTrue(message = "Opplysningene må bekreftes for å sende inn søknad")
    val harBekreftetOpplysninger: Boolean,

    @field:AssertTrue(message = "Må ha forstått rettigheter og plikter for å sende inn søknad")
    val harForståttRettigheterOgPlikter: Boolean,

    @field:Valid val frilans: Frilans? = null,
    val stønadGodtgjørelse: StønadGodtgjørelse? = null,
    @field:Valid val selvstendigNæringsdrivende: SelvstendigNæringsdrivende? = null,
    val barnRelasjon: BarnRelasjon? = null,
    val barnRelasjonBeskrivelse: String? = null,
    val harVærtEllerErVernepliktig: Boolean? = null,
    val dataBruktTilUtledningAnnetData: String? = null,
    val kurs: Kurs? = null
) : Innsending {

    internal fun leggTilIdentifikatorPåBarnHvisMangler(barnFraOppslag: List<BarnOppslag>) {
        if (barn.manglerIdentitetsnummer()) {
            barn oppdaterFødselsnummer barnFraOppslag.hentIdentitetsnummerForBarn(barn.aktørId)
        }
    }

    override fun somKomplettSøknad(
        søker: Søker,
        k9Format: no.nav.k9.søknad.Innsending?,
        titler: List<String>,
    ): KomplettOpplæringspengerSøknad {
        requireNotNull(k9Format)
        return KomplettOpplæringspengerSøknad(
            apiDataVersjon = apiDataVersjon,
            språk = språk,
            søknadId = søknadId,
            mottatt = mottatt,
            fraOgMed = fraOgMed,
            tilOgMed = tilOgMed,
            søker = søker,
            barn = barn,
            vedleggId = vedlegg.map { it.toURI().dokumentId() },
            fødselsattestVedleggId = fødselsattestVedleggUrls?.map { it.toURI().dokumentId() } ?: listOf(),
            arbeidsgivere = arbeidsgivere,
            medlemskap = medlemskap,
            ferieuttakIPerioden = ferieuttakIPerioden,
            opptjeningIUtlandet = opptjeningIUtlandet,
            utenlandskNæring = utenlandskNæring,
            utenlandsoppholdIPerioden = utenlandsoppholdIPerioden,
            harBekreftetOpplysninger = harBekreftetOpplysninger,
            harForståttRettigheterOgPlikter = harForståttRettigheterOgPlikter,
            frilans = frilans,
            stønadGodtgjørelse = stønadGodtgjørelse,
            selvstendigNæringsdrivende = selvstendigNæringsdrivende,
            barnRelasjon = barnRelasjon,
            barnRelasjonBeskrivelse = barnRelasjonBeskrivelse,
            harVærtEllerErVernepliktig = harVærtEllerErVernepliktig,
            kurs = kurs,
            k9FormatSøknad = k9Format as K9Søknad
        )
    }

    override fun søkerNorskIdent(): String? = søkerNorskIdent

    override fun ytelse(): Ytelse = Ytelse.OPPLARINGSPENGER

    override fun søknadId(): String = søknadId

    override fun vedlegg(): List<URL> = mutableListOf<URL>().apply {
        addAll(vedlegg)
        fødselsattestVedleggUrls?.let { addAll(it) }
    }

    @AssertTrue(message = "Når 'barnRelasjon' er ANNET, kan ikke 'barnRelasjonBeskrivelse' være tom")
    fun isBarnRelasjonBeskrivelse(): Boolean {
        if (barnRelasjon == BarnRelasjon.ANNET) {
            return !barnRelasjonBeskrivelse.isNullOrBlank()
        }
        return true
    }

    override fun valider(): List<String> = mutableListOf<String>().apply {
        addAll(opptjeningIUtlandet.valider())
        addAll(utenlandskNæring.valider("utenlandskNæring"))

        //TODO: har vi en annen måte å validere dette på?
        //addAll(frilans.valider("frilans", fraOgMed))
        addAll(medlemskap.valider("medlemskap"))
        addAll(utenlandsoppholdIPerioden.valider("utenlandsoppholdIPerioden"))

        ferieuttakIPerioden?.let { addAll(it.valider(("ferieuttakIPerioden"))) }

        vedlegg.mapIndexed { index, url ->
            krever(url.path.matches(Regex("/vedlegg/.*")), "vedlegg[$index] inneholder ikke gyldig url")
        }

        if (isNotEmpty()) throw ValidationErrorResponseException(ValidationProblemDetailsString(this))

    }

    override fun søknadValidator(): SøknadValidator<no.nav.k9.søknad.Søknad> =  OpplæringspengerSøknadValidator()

    override fun somK9Format(søker: Søker, metadata: MetaInfo): no.nav.k9.søknad.Innsending {
        val søknadsperiode = K9Periode(fraOgMed, tilOgMed)
        val olp = Opplæringspenger()
            .medSøknadsperiode(søknadsperiode)
            .medBarn(barn.tilK9Barn())
            .medOpptjeningAktivitet(byggK9OpptjeningAktivitet())
            .medArbeidstid(byggK9Arbeidstid())
            .medUttak(byggK9Uttak(søknadsperiode))
            .medBosteder(medlemskap.tilK9Bosteder())
            .medDataBruktTilUtledning(byggK9DataBruktTilUtledning(metadata)) as Opplæringspenger

        barnRelasjon?.let { olp.medOmsorg(byggK9Omsorg()) }

        ferieuttakIPerioden?.let {
            if (it.ferieuttak.isNotEmpty() && it.skalTaUtFerieIPerioden) {
                olp.medLovbestemtFerie(ferieuttakIPerioden.tilK9LovbestemtFerie())
            }
        }

        if (utenlandsoppholdIPerioden.skalOppholdeSegIUtlandetIPerioden == true) {
            olp.medUtenlandsopphold(utenlandsoppholdIPerioden.tilK9Utenlandsopphold())
        }

        if (kurs != null) {
            olp.medKurs(kurs.tilK9Format())
        }

        return K9Søknad(SøknadId.of(søknadId), k9FormatVersjon, mottatt, søker.somK9Søker(), olp)
            .medKildesystem(Kildesystem.SØKNADSDIALOG)
            .medSpråk(K9Språk.of(språk?.name ?: "nb"))
    }

    fun byggK9Uttak(periode: K9Periode): Uttak {
        val perioder = mutableMapOf<K9Periode, Uttak.UttakPeriodeInfo>()

        perioder[periode] = Uttak.UttakPeriodeInfo(Duration.ofHours(7).plusMinutes(30))

        return Uttak().medPerioder(perioder)
    }

    private fun byggK9OpptjeningAktivitet() = OpptjeningAktivitet().apply {
        frilans.let { medFrilanser(it?.somK9Frilanser()) }
        this@OpplæringspengerSøknad.selvstendigNæringsdrivende.let { medSelvstendigNæringsdrivende(it?.somK9SelvstendigNæringsdrivende()) }
    }

    private fun byggK9Arbeidstid() = Arbeidstid().apply {
        if (arbeidsgivere.isNotEmpty()) {
            medArbeidstaker(arbeidsgivere.somK9Arbeidstaker(fraOgMed, tilOgMed))
        }

        medSelvstendigNæringsdrivendeArbeidstidInfo(selvstendigNæringsdrivende?.somK9ArbeidstidInfo(fraOgMed, tilOgMed))
        medFrilanserArbeidstid(frilans?.somK9Arbeidstid(fraOgMed, tilOgMed))
    }

    fun byggK9DataBruktTilUtledning(metadata: MetaInfo): DataBruktTilUtledning = DataBruktTilUtledning()
        .medHarBekreftetOpplysninger(harBekreftetOpplysninger)
        .medHarForståttRettigheterOgPlikter(harForståttRettigheterOgPlikter)
        .medSoknadDialogCommitSha(metadata.soknadDialogCommitSha)
        .medAnnetData(dataBruktTilUtledningAnnetData)

    fun byggK9Omsorg() = Omsorg()
        .medRelasjonTilBarnet(
            when (barnRelasjon) {
                BarnRelasjon.FAR -> Omsorg.BarnRelasjon.FAR
                BarnRelasjon.MOR -> Omsorg.BarnRelasjon.MOR
                BarnRelasjon.FOSTERFORELDER -> Omsorg.BarnRelasjon.FOSTERFORELDER
                BarnRelasjon.MEDMOR -> Omsorg.BarnRelasjon.MEDMOR
                BarnRelasjon.ANNET -> Omsorg.BarnRelasjon.ANNET
                else -> null
            }
        ).medBeskrivelseAvOmsorgsrollen(barnRelasjonBeskrivelse?.let { StringUtils.saniter(it) })

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OpplæringspengerSøknad

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

fun List<BarnOppslag>.hentIdentitetsnummerForBarn(aktørId: String?): String? {
    return this.firstOrNull() { it.aktørId == aktørId }?.identitetsnummer
}

data class Periode(
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
) {
    fun somLocalDateInterval() = LocalDateInterval(fraOgMed, tilOgMed)
}
