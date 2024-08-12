package no.nav.brukerdialog.api.ytelse.omsorgsdageraleneomsorg.domene

import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotEmpty
import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.SøknadValidator
import no.nav.k9.søknad.felles.Kildesystem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.DataBruktTilUtledning
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.OmsorgspengerAleneOmsorg
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.OmsorgspengerAleneOmsorgSøknadValidator
import no.nav.brukerdialog.api.innsending.Innsending
import no.nav.brukerdialog.api.ytelse.Ytelse
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.oppslag.barn.BarnOppslag
import no.nav.brukerdialog.oppslag.soker.Søker
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as K9Søknad

data class OmsorgsdagerAleneOmOmsorgenSøknad(
    @field:org.hibernate.validator.constraints.UUID(message = "Forventet gyldig UUID, men var '\${validatedValue}'")
    val søknadId: String = UUID.randomUUID().toString(),

    val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    val språk: String,

    @field:Valid
    @field:NotEmpty(message = "Kan ikke være en tom liste")
    val barn: List<Barn> = listOf(),

    @field:AssertTrue(message = "Opplysningene må bekreftes for å sende inn søknad")
    val harBekreftetOpplysninger: Boolean,

    @field:AssertTrue(message = "Må ha forstått rettigheter og plikter for å sende inn søknad")
    val harForståttRettigheterOgPlikter: Boolean,

    val dataBruktTilUtledningAnnetData: String? = null,
) : Innsending {
    internal fun gjelderFlereBarn() = barn.size > 1
    internal fun manglerIdentifikatorPåBarn() = barn.any { it.manglerIdentifikator() }

    internal fun leggTilIdentifikatorPåBarnHvisMangler(barnFraOppslag: List<BarnOppslag>) {
        barn.forEach { it.leggTilIdentifikatorHvisMangler(barnFraOppslag) }
    }

    internal fun splittTilEgenSøknadPerBarn(): List<OmsorgsdagerAleneOmOmsorgenSøknad> {
        return barn.map {
            OmsorgsdagerAleneOmOmsorgenSøknad(
                mottatt = this.mottatt,
                språk = this.språk,
                barn = listOf(it),
                harForståttRettigheterOgPlikter = this.harForståttRettigheterOgPlikter,
                harBekreftetOpplysninger = this.harBekreftetOpplysninger
            )
        }
    }

    override fun somK9Format(søker: Søker, metadata: MetaInfo): K9Søknad {
        // Innsendt søknad blir splittet opp i 1 søknad per barn. Derfor skal det kun være et barn i lista.
        require(barn.size == 1) { "Søknad etter splitt kan kun inneholdet et barn" }

        return K9Søknad()
            .medSøknadId(SøknadId(søknadId))
            .medMottattDato(mottatt)
            .medVersjon(Versjon.of("1.0.0"))
            .medSøker(søker.somK9Søker())
            .medKildesystem(Kildesystem.SØKNADSDIALOG)
            .medYtelse(
                OmsorgspengerAleneOmsorg(
                    barn.first().somK9Barn(),
                    Periode(barn.first().k9PeriodeFraOgMed(), null)
                ).medDataBruktTilUtledning(byggK9DataBruktTilUtledning(metadata)) as OmsorgspengerAleneOmsorg
            )
    }

    fun byggK9DataBruktTilUtledning(metadata: MetaInfo): DataBruktTilUtledning = DataBruktTilUtledning()
        .medHarBekreftetOpplysninger(harBekreftetOpplysninger)
        .medHarForståttRettigheterOgPlikter(harForståttRettigheterOgPlikter)
        .medSoknadDialogCommitSha(metadata.soknadDialogCommitSha)
        .medAnnetData(dataBruktTilUtledningAnnetData)

    override fun valider() = mutableListOf<String>()

    override fun somKomplettSøknad(
        søker: Søker,
        k9Format: no.nav.k9.søknad.Innsending?,
        titler: List<String>,
    ): OmsorgsdagerAleneOmOmsorgenKomplettSøknad {
        require(barn.size == 1) { "Søknad etter splitt kan kun inneholdet et barn" }
        requireNotNull(k9Format)
        return OmsorgsdagerAleneOmOmsorgenKomplettSøknad(
            mottatt = mottatt,
            søker = søker,
            søknadId = søknadId,
            språk = språk,
            barn = this.barn[0],
            k9Søknad = k9Format as Søknad,
            harBekreftetOpplysninger = harBekreftetOpplysninger,
            harForståttRettigheterOgPlikter = harForståttRettigheterOgPlikter
        )
    }

    override fun ytelse(): Ytelse = Ytelse.OMSORGSDAGER_ALENEOMSORG
    override fun søknadId(): String = søknadId
    override fun vedlegg(): List<URL> = listOf()

    override fun søknadValidator(): SøknadValidator<Søknad> = OmsorgspengerAleneOmsorgSøknadValidator()
}
