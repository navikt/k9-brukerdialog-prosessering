package no.nav.brukerdialog.ytelse.omsorgspengermidlertidigalene.api.domene

import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotEmpty
import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.SøknadValidator
import no.nav.k9.søknad.felles.Kildesystem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.DataBruktTilUtledning
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.OmsorgspengerMidlertidigAlene
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.OmsorgspengerMidlertidigAleneSøknadValidator
import no.nav.brukerdialog.domenetjenester.innsending.Innsending
import no.nav.brukerdialog.ytelse.Ytelse
import no.nav.brukerdialog.ytelse.fellesdomene.Barn
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.oppslag.barn.BarnOppslag
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.k9.søknad.felles.type.Språk
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as K9Søknad

data class OmsorgspengerMidlertidigAleneSøknad(
    @field:org.hibernate.validator.constraints.UUID(message = "Forventet gyldig UUID, men var '\${validatedValue}'")
    val søknadId: String = UUID.randomUUID().toString(),
    val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    val id: String,
    val språk: String,
    val søkerNorskIdent: String? = null, // TODO: Fjern nullable når vi har lansert og mellomlagring inneholder dette feltet.
    @field:Valid val annenForelder: AnnenForelder,

    @field:Valid
    @field:NotEmpty
    val barn: List<Barn> = listOf(),

    @field:AssertTrue(message = "Opplysningene må bekreftes for å sende inn søknad")
    val harBekreftetOpplysninger: Boolean,

    @field:AssertTrue(message = "Må ha forstått rettigheter og plikter for å sende inn søknad")
    val harForståttRettigheterOgPlikter: Boolean,

    val dataBruktTilUtledningAnnetData: String? = null,
) : Innsending {

    companion object {
        private val k9FormatVersjon = Versjon.of("1.0.0")
    }

    override fun somK9Format(søker: Søker, metadata: MetaInfo): no.nav.k9.søknad.Søknad {
        return K9Søknad(
            SøknadId.of(søknadId),
            k9FormatVersjon,
            mottatt,
            søker.somK9Søker(),
            Språk.of(språk),
            OmsorgspengerMidlertidigAlene(
                barn.map { it.somK9Barn() },
                annenForelder.somK9AnnenForelder(),
                null
            ).medDataBruktTilUtledning(byggK9DataBruktTilUtledning(metadata)) as OmsorgspengerMidlertidigAlene
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
    ): OmsorgspengerMdlertidigAleneKomplettSøknad {
        requireNotNull(k9Format)
        return OmsorgspengerMdlertidigAleneKomplettSøknad(
            mottatt = mottatt,
            søker = søker,
            søknadId = søknadId,
            id = id,
            språk = språk,
            annenForelder = annenForelder,
            barn = barn,
            k9Format = k9Format as Søknad,
            harBekreftetOpplysninger = harBekreftetOpplysninger,
            harForståttRettigheterOgPlikter = harForståttRettigheterOgPlikter
        )
    }

    internal fun leggTilIdentifikatorPåBarnHvisMangler(barnFraOppslag: List<BarnOppslag>) {
        barn.forEach { barn ->
            if (barn.manglerIdentifikator()) barn.leggTilIdentifikatorHvisMangler(barnFraOppslag)
        }
    }

    override fun valider() = mutableListOf<String>()
    override fun søkerNorskIdent(): String? = søkerNorskIdent

    override fun ytelse(): Ytelse = Ytelse.OMSORGSPENGER_MIDLERTIDIG_ALENE
    override fun søknadId(): String = søknadId
    override fun vedlegg(): List<URL> = listOf()
    override fun søknadValidator(): SøknadValidator<no.nav.k9.søknad.Søknad> =
        OmsorgspengerMidlertidigAleneSøknadValidator()
}
