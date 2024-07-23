package no.nav.k9brukerdialogprosessering.api.ytelse.omsorgspengerutvidetrett.domene

import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.SøknadValidator
import no.nav.k9.søknad.felles.Kildesystem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.DataBruktTilUtledning
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.OmsorgspengerKroniskSyktBarn
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.OmsorgspengerKroniskSyktBarnSøknadValidator
import no.nav.k9brukerdialogprosessering.api.innsending.Innsending
import no.nav.k9brukerdialogprosessering.api.ytelse.Ytelse
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Barn
import no.nav.k9brukerdialogprosessering.common.MetaInfo
import no.nav.k9brukerdialogprosessering.mellomlagring.dokument.dokumentId
import no.nav.k9brukerdialogprosessering.oppslag.barn.BarnOppslag
import no.nav.k9brukerdialogprosessering.oppslag.soker.Søker
import no.nav.k9brukerdialogprosessering.utils.StringUtils.FritekstPattern
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as K9Søknad

data class OmsorgspengerKroniskSyktBarnSøknad(
    val søknadId: String = UUID.randomUUID().toString(),
    val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    val språk: String,
    @field:Valid var barn: Barn,
    val legeerklæring: List<URL> = listOf(),
    val samværsavtale: List<URL>? = null,
    val relasjonTilBarnet: SøkerBarnRelasjon? = null,
    val kroniskEllerFunksjonshemming: Boolean,

    @field:AssertTrue(message = "Må ha forstått rettigheter og plikter for å sende inn søknad")
    val harForståttRettigheterOgPlikter: Boolean,

    @field:AssertTrue(message = "Opplysningene må bekreftes for å sende inn søknad")
    val harBekreftetOpplysninger: Boolean,

    @field:NotNull(message = "Kan ikke være null") val sammeAdresse: BarnSammeAdresse?,
    val høyereRisikoForFravær: Boolean? = null,

    @field:Pattern(regexp = FritekstPattern, message = "Matcher ikke tillatt mønster: '{regexp}'")
    @field:Size(min = 1, max = 1000, message = "Må være mellom 1 og 1000 tegn")
    val høyereRisikoForFraværBeskrivelse: String? = null, // skal valideres hvis høyereRisikoForFravær er true

    val dataBruktTilUtledningAnnetData: String? = null,
) : Innsending {

    companion object {
        private val k9FormatVersjon = Versjon.of("1.0.0")
    }

    internal fun leggTilIdentifikatorPåBarnHvisMangler(barnFraOppslag: List<BarnOppslag>) {
        if (barn.manglerIdentifikator()) barn.leggTilIdentifikatorHvisMangler(barnFraOppslag)
    }

    override fun somK9Format(søker: Søker, metadata: MetaInfo): K9Søknad {
        return K9Søknad(
            SøknadId.of(søknadId),
            k9FormatVersjon,
            mottatt,
            søker.somK9Søker(),
            OmsorgspengerKroniskSyktBarn(
                barn.somK9Barn(),
                kroniskEllerFunksjonshemming
            )
                .medDataBruktTilUtledning(byggK9DataBruktTilUtledning(metadata)) as OmsorgspengerKroniskSyktBarn
        ).medKildesystem(Kildesystem.SØKNADSDIALOG)
    }

    fun byggK9DataBruktTilUtledning(metadata: MetaInfo): DataBruktTilUtledning = DataBruktTilUtledning()
        .medHarBekreftetOpplysninger(harBekreftetOpplysninger)
        .medHarForståttRettigheterOgPlikter(harForståttRettigheterOgPlikter)
        .medSoknadDialogCommitSha(metadata.soknadDialogCommitSha)
        .medAnnetData(dataBruktTilUtledningAnnetData)

    override fun somKomplettSøknad(
        søker: Søker,
        k9Format: no.nav.k9.søknad.Innsending?,
        titler: List<String>,
    ): OmsorgspengerKroniskSyktBarnKomplettSøknad {
        requireNotNull(k9Format)
        return OmsorgspengerKroniskSyktBarnKomplettSøknad(
            språk = språk,
            søknadId = søknadId,
            mottatt = mottatt,
            kroniskEllerFunksjonshemming = kroniskEllerFunksjonshemming,
            søker = søker,
            barn = barn,
            relasjonTilBarnet = relasjonTilBarnet,
            sammeAdresse = sammeAdresse,
            høyereRisikoForFravær = høyereRisikoForFravær,
            høyereRisikoForFraværBeskrivelse = høyereRisikoForFraværBeskrivelse,
            legeerklæringVedleggId = legeerklæring.map { it.toURI().dokumentId() },
            samværsavtaleVedleggId = samværsavtale?.map { it.toURI().dokumentId() } ?: listOf(),
            harForståttRettigheterOgPlikter = harForståttRettigheterOgPlikter,
            harBekreftetOpplysninger = harBekreftetOpplysninger,
            k9FormatSøknad = k9Format as Søknad
        )
    }

    @AssertTrue(message = "Dersom 'høyereRisikoForFravær' er true, må 'høyereRisikoForFraværBeskrivelse' være satt")
    fun isHøyereRisikoForFraværBeskrivelse(): Boolean {
        if (høyereRisikoForFravær == true) {
            return høyereRisikoForFraværBeskrivelse != null
        }
        return true
    }

    override fun valider() = mutableListOf<String>()

    override fun søknadValidator(): SøknadValidator<no.nav.k9.søknad.Søknad> =
        OmsorgspengerKroniskSyktBarnSøknadValidator()

    override fun ytelse(): Ytelse = Ytelse.OMSORGSPENGER_UTVIDET_RETT
    override fun søknadId(): String = søknadId
    override fun vedlegg(): List<URL> = mutableListOf<URL>().apply {
        addAll(legeerklæring)
        samværsavtale?.let { addAll(it) }
    }
}
