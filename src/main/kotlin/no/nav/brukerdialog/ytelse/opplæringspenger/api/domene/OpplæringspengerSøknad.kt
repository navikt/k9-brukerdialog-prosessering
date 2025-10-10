package no.nav.brukerdialog.ytelse.opplæringspenger.api.domene

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.domenetjenester.innsending.Innsending
import no.nav.brukerdialog.utils.URIUtils.dokumentId
import no.nav.brukerdialog.oppslag.barn.BarnOppslag
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.brukerdialog.utils.StringUtils
import no.nav.brukerdialog.utils.krever
import no.nav.brukerdialog.validation.ValidationErrorResponseException
import no.nav.brukerdialog.validation.ValidationProblemDetailsString
import no.nav.brukerdialog.ytelse.Ytelse
import no.nav.brukerdialog.ytelse.fellesdomene.ArbeidUtils.arbeidstidInfoMedNullTimer
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.UtenlandskNæring.Companion.valider
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid.ArbeidsgiverOLP
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid.ArbeidsgiverOLP.Companion.somK9Arbeidstaker
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid.FrilansOLP
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid.SelvstendigNæringsdrivendeOLP
import no.nav.fpsak.tidsserie.LocalDateInterval
import no.nav.k9.søknad.SøknadValidator
import no.nav.k9.søknad.felles.Kildesystem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.opptjening.OpptjeningAktivitet
import no.nav.k9.søknad.felles.type.Periode
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

    @field:org.hibernate.validator.constraints.UUID(message = "Forventet gyldig UUID, men var '\${validatedValue}'")
    @Schema(hidden = true)
    val søknadId: String = UUID.randomUUID().toString(),

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    @Schema(hidden = true)
    val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    val språk: Språk,
    val søkerNorskIdent: String? = null, // TODO: Fjern nullable når vi har lansert og mellomlagring inneholder dette feltet.

    @field:Valid
    val barn: BarnDetaljer,

    @field:Valid
    val arbeidsgivere: List<ArbeidsgiverOLP>,
    val vedlegg: List<URL> = listOf(), // TODO: Fjern listof() når krav om legeerklæring er påkrevd igjen.

    @JsonFormat(pattern = "yyyy-MM-dd")
    val fraOgMed: LocalDate,

    @JsonFormat(pattern = "yyyy-MM-dd")
    val tilOgMed: LocalDate,
    val medlemskap: Medlemskap,
    @field:Valid val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden?, // TODO: fjern nullable når vi har lansert og mellomlagring inneholder dette feltet.

    val ferieuttakIPerioden: FerieuttakIPerioden?,
    val opptjeningIUtlandet: List<OpptjeningIUtlandet>,
    val utenlandskNæring: List<UtenlandskNæring>,

    @field:AssertTrue(message = "Opplysningene må bekreftes for å sende inn søknad")
    val harBekreftetOpplysninger: Boolean,

    @field:AssertTrue(message = "Må ha forstått rettigheter og plikter for å sende inn søknad")
    val harForståttRettigheterOgPlikter: Boolean,

    @field:Valid
    val frilans: FrilansOLP? = null,

    @field:Valid
    val selvstendigNæringsdrivende: SelvstendigNæringsdrivendeOLP? = null,
    val stønadGodtgjørelse: StønadGodtgjørelse? = null,
    val harVærtEllerErVernepliktig: Boolean? = null,
    val dataBruktTilUtledningAnnetData: String? = null,

    @field:Valid
    val ettersendingAvVedlegg: EttersendingAvVedlegg? = null, // TODO: fjern nullable når vi har lansert ettersendingAvVedlegg og mellomlagring inneholder dette feltet.

    @field:Valid
    val kurs: Kurs
) : Innsending {

    internal fun leggTilIdentifikatorPåBarnHvisMangler(barnFraOppslag: List<BarnOppslag>) {
        if (barn.manglerIdentitetsnummer()) {
            barn oppdaterNorskIdentifikator barnFraOppslag.hentIdentitetsnummerForBarn(barn.aktørId)
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
            fødselsattestVedleggId = barn.fødselsattestVedleggUrls?.map { it.toURI().dokumentId() } ?: listOf(),
            arbeidsgivere = arbeidsgivere,
            medlemskap = medlemskap,
            utenlandsoppholdIPerioden = utenlandsoppholdIPerioden,
            ferieuttakIPerioden = ferieuttakIPerioden,
            opptjeningIUtlandet = opptjeningIUtlandet,
            utenlandskNæring = utenlandskNæring,
            harBekreftetOpplysninger = harBekreftetOpplysninger,
            harForståttRettigheterOgPlikter = harForståttRettigheterOgPlikter,
            frilans = frilans,
            stønadGodtgjørelse = stønadGodtgjørelse,
            selvstendigNæringsdrivende = selvstendigNæringsdrivende,
            harVærtEllerErVernepliktig = harVærtEllerErVernepliktig,
            ettersendingAvVedlegg = ettersendingAvVedlegg,
            kurs = kurs,
            k9FormatSøknad = k9Format as K9Søknad
        )
    }

    override fun søkerNorskIdent(): String? = søkerNorskIdent

    override fun ytelse(): Ytelse = Ytelse.OPPLARINGSPENGER

    override fun innsendingId(): String = søknadId

    override fun vedlegg(): List<URL> = mutableListOf<URL>().apply {
        addAll(vedlegg)
        barn.fødselsattestVedleggUrls?.let { addAll(it) }
    }

    override fun valider(): List<String> = mutableListOf<String>().apply {
        addAll(opptjeningIUtlandet.valider())
        addAll(utenlandskNæring.valider("utenlandskNæring"))
        addAll(medlemskap.valider("medlemskap"))

        ferieuttakIPerioden?.let { addAll(it.valider(("ferieuttakIPerioden"))) }

        vedlegg.mapIndexed { index, url ->
            krever(url.path.matches(Regex("/vedlegg/.*")), "vedlegg[$index] inneholder ikke gyldig url")
        }

        if (isNotEmpty()) throw ValidationErrorResponseException(ValidationProblemDetailsString(this))

    }

    override fun søknadValidator(): SøknadValidator<no.nav.k9.søknad.Søknad> = OpplæringspengerSøknadValidator()

    override fun somK9Format(søker: Søker, metadata: MetaInfo): no.nav.k9.søknad.Innsending {
        val søknadsperiode = if (kurs.enkeltdagEllerPeriode == KursVarighetType.PERIODE) kurs.kursperioder else kurs.kursdager?.map { Periode(it.dato, it.dato) }

        val olp = Opplæringspenger()
            .medSøknadsperiode(søknadsperiode)
            .medBarn(barn.tilK9Barn())
            .medOpptjeningAktivitet(byggK9OpptjeningAktivitet())
            .medArbeidstid(byggK9Arbeidstid(søknadsperiode))
            .medUttak(byggK9Uttak(søknadsperiode))
            .medBosteder(medlemskap.tilK9Bosteder())
            .medKurs(kurs.tilK9Format())
            .medDataBruktTilUtledning(byggK9DataBruktTilUtledning(metadata)) as Opplæringspenger

        barn.relasjonTilBarnet?.let { olp.medOmsorg(byggK9Omsorg()) }

        ferieuttakIPerioden?.let {
            if (it.ferieuttak.isNotEmpty() && it.skalTaUtFerieIPerioden) {
                olp.medLovbestemtFerie(ferieuttakIPerioden.tilK9LovbestemtFerie())
            }
        }

        if (utenlandsoppholdIPerioden != null && utenlandsoppholdIPerioden.skalOppholdeSegIUtlandetIPerioden == true) {
            olp.medUtenlandsopphold(utenlandsoppholdIPerioden.tilK9Utenlandsopphold())
        }

        return K9Søknad(SøknadId.of(søknadId), k9FormatVersjon, mottatt, søker.somK9Søker(), olp)
            .medKildesystem(Kildesystem.SØKNADSDIALOG)
            .medSpråk(K9Språk.of(språk.name))
    }

    fun byggK9Uttak(perioder: List<K9Periode>?): Uttak {
        if (perioder.isNullOrEmpty()){
            throw IllegalArgumentException("Perioder må være satt for å kunne lage Uttak")
        }

        val uttaksPerioder = mutableMapOf<K9Periode, Uttak.UttakPeriodeInfo>()

        perioder.forEach { periode ->
            uttaksPerioder[periode] = Uttak.UttakPeriodeInfo(Duration.ofHours(7).plusMinutes(30))
        }

        return Uttak().medPerioder(uttaksPerioder)
    }

    private fun byggK9OpptjeningAktivitet() = OpptjeningAktivitet().apply {
        frilans?.let { medFrilanser(it.somK9Frilanser()) }
        this@OpplæringspengerSøknad.selvstendigNæringsdrivende?.let {
            medSelvstendigNæringsdrivende(it.somK9SelvstendigNæringsdrivende())
        }
    }

    private fun byggK9Arbeidstid(perioder: List<Periode>?) = Arbeidstid().apply {
        if (perioder.isNullOrEmpty()){
            throw IllegalArgumentException("Perioder må være satt for å kunne lage Arbeidstid")
        }

        if (arbeidsgivere.isNotEmpty()) {
            medArbeidstaker(arbeidsgivere.somK9Arbeidstaker(perioder))
        }

        selvstendigNæringsdrivende?.let {
            medSelvstendigNæringsdrivendeArbeidstidInfo(it.somK9ArbeidstidInfo(perioder))
        }

        when (frilans) {
            null -> medFrilanserArbeidstid(arbeidstidInfoMedNullTimer(perioder))
            else -> medFrilanserArbeidstid(frilans.somK9Arbeidstid(perioder))
        }
    }

    fun byggK9DataBruktTilUtledning(metadata: MetaInfo): DataBruktTilUtledning = DataBruktTilUtledning()
        .medHarBekreftetOpplysninger(harBekreftetOpplysninger)
        .medHarForståttRettigheterOgPlikter(harForståttRettigheterOgPlikter)
        .medSoknadDialogCommitSha(metadata.soknadDialogCommitSha)
        .medAnnetData(dataBruktTilUtledningAnnetData)

    fun byggK9Omsorg() = Omsorg()
        .medRelasjonTilBarnet(
            when (barn.relasjonTilBarnet) {
                BarnRelasjon.FAR -> Omsorg.BarnRelasjon.FAR
                BarnRelasjon.MOR -> Omsorg.BarnRelasjon.MOR
                BarnRelasjon.FOSTERFORELDER -> Omsorg.BarnRelasjon.FOSTERFORELDER
                BarnRelasjon.MEDMOR -> Omsorg.BarnRelasjon.MEDMOR
                BarnRelasjon.ANNET -> Omsorg.BarnRelasjon.ANNET
                else -> null
            }
        ).medBeskrivelseAvOmsorgsrollen(barn .relasjonTilBarnetBeskrivelse?.let { StringUtils.saniter(it) })

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
