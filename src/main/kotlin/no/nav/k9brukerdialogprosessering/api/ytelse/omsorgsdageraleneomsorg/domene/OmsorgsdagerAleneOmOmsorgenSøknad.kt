package no.nav.k9brukerdialogapi.ytelse.omsorgsdageraleneomsorg.domene

import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.k9.søknad.SøknadValidator
import no.nav.k9.søknad.felles.Kildesystem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.DataBruktTilUtledning
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.OmsorgspengerAleneOmsorg
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.OmsorgspengerAleneOmsorgSøknadValidator
import no.nav.k9brukerdialogapi.general.ValidationProblemDetails
import no.nav.k9brukerdialogapi.general.krever
import no.nav.k9brukerdialogapi.innsending.Innsending
import no.nav.k9brukerdialogprosessering.api.ytelse.Ytelse
import no.nav.k9brukerdialogprosessering.common.MetaInfo
import no.nav.k9brukerdialogprosessering.oppslag.barn.BarnOppslag
import no.nav.k9brukerdialogprosessering.oppslag.soker.Søker
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as K9Søknad

class OmsorgsdagerAleneOmOmsorgenSøknad(
    internal val søknadId: String = UUID.randomUUID().toString(),
    private val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    private val språk: String,
    private val barn: List<Barn> = listOf(),
    private val harForståttRettigheterOgPlikter: Boolean,
    private val harBekreftetOpplysninger: Boolean,
    private val dataBruktTilUtledningAnnetData: String? = null
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

    override fun valider() = mutableListOf<String>().apply {
        krever(harBekreftetOpplysninger, "harBekreftetOpplysninger må være true")
        krever(harForståttRettigheterOgPlikter, "harForståttRettigheterOgPlikter må være true")
        krever(barn.isNotEmpty(), "barn kan ikke være en tom liste.")
        barn.forEachIndexed { index, barn -> addAll(barn.valider("barn[$index]")) }

        if (isNotEmpty()) throw Throwblem(ValidationProblemDetails(this))
    }

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
            k9Søknad = k9Format as no.nav.k9.søknad.Søknad,
            harBekreftetOpplysninger = harBekreftetOpplysninger,
            harForståttRettigheterOgPlikter = harForståttRettigheterOgPlikter
        )
    }

    override fun ytelse(): Ytelse = Ytelse.OMSORGSDAGER_ALENEOMSORG
    override fun søknadId(): String = søknadId
    override fun vedlegg(): List<URL> = listOf()

    override fun søknadValidator(): SøknadValidator<no.nav.k9.søknad.Søknad> = OmsorgspengerAleneOmsorgSøknadValidator()
}
