package no.nav.brukerdialog.ytelse.omsorgpengerutbetalingsnf.api.domene

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.SøknadValidator
import no.nav.k9.søknad.felles.Kildesystem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.opptjening.OpptjeningAktivitet
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.DataBruktTilUtledning
import no.nav.k9.søknad.ytelse.omsorgspenger.v1.OmsorgspengerUtbetaling
import no.nav.k9.søknad.ytelse.omsorgspenger.v1.OmsorgspengerUtbetalingSøknadValidator
import no.nav.brukerdialog.domenetjenester.innsending.Innsending
import no.nav.brukerdialog.ytelse.Ytelse
import no.nav.brukerdialog.ytelse.fellesdomene.Bekreftelser
import no.nav.brukerdialog.ytelse.fellesdomene.Bosted
import no.nav.brukerdialog.ytelse.fellesdomene.Bosted.Companion.somK9Bosteder
import no.nav.brukerdialog.ytelse.fellesdomene.Bosted.Companion.somK9Utenlandsopphold
import no.nav.brukerdialog.ytelse.fellesdomene.Opphold
import no.nav.brukerdialog.ytelse.fellesdomene.Utbetalingsperiode
import no.nav.brukerdialog.ytelse.fellesdomene.Utbetalingsperiode.Companion.somK9FraværPeriode
import no.nav.brukerdialog.ytelse.fellesdomene.Virksomhet
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingsnf.api.domene.Barn.Companion.kunFosterbarn
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingsnf.api.domene.Barn.Companion.somK9BarnListe
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.integrasjon.k9mellomlagring.dokumentId
import no.nav.brukerdialog.oppslag.barn.BarnOppslag
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.k9.søknad.felles.type.Språk
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as K9Søknad

data class OmsorgspengerutbetalingSnfSøknad(
    @field:org.hibernate.validator.constraints.UUID(message = "Forventet gyldig UUID, men var '\${validatedValue}'")
    @Schema(hidden = true)
    val søknadId: String = UUID.randomUUID().toString(),

    @Schema(hidden = true)
    val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    
    val språk: String,
    val søkerNorskIdent: String? = null, // TODO: Fjern nullable når vi har lansert og mellomlagring inneholder dette feltet.
    @field:Valid val bosteder: List<Bosted>,
    @field:Valid val opphold: List<Opphold>,
    val spørsmål: List<SpørsmålOgSvar>,
    val harDekketTiFørsteDagerSelv: Boolean? = null,
    val harSyktBarn: Boolean? = null,
    val harAleneomsorg: Boolean? = null,
    @field:Valid val bekreftelser: Bekreftelser,
    @field:Valid val utbetalingsperioder: List<Utbetalingsperiode>,
    @field:Valid val barn: List<Barn>,
    @field:Valid val frilans: Frilans? = null,
    @field:Valid val selvstendigNæringsdrivende: Virksomhet? = null,
    val erArbeidstakerOgså: Boolean,
    val vedlegg: List<URL> = listOf(),
    val dataBruktTilUtledningAnnetData: String? = null,
) : Innsending {

    companion object {
        private val k9FormatVersjon = Versjon.of("1.1.0")
    }

    override fun valider() = mutableListOf<String>()

    internal fun leggTilIdentifikatorPåBarnHvisMangler(barnFraOppslag: List<BarnOppslag>) {
        barn.forEach { it.leggTilIdentifikatorHvisMangler(barnFraOppslag) }
    }

    override fun somK9Format(søker: Søker, metadata: MetaInfo): no.nav.k9.søknad.Søknad {
        return K9Søknad(
            SøknadId.of(søknadId),
            k9FormatVersjon,
            mottatt,
            søker.somK9Søker(),
            Språk.of(språk),
            OmsorgspengerUtbetaling(
                barn.somK9BarnListe(),
                byggK9OpptjeningAktivitet(),
                utbetalingsperioder.somK9FraværPeriode(),
                null,
                bosteder.somK9Bosteder(),
                opphold.somK9Utenlandsopphold()
            ).medDataBruktTilUtledning(byggK9DataBruktTilUtledning(metadata)) as OmsorgspengerUtbetaling
        ).medKildesystem(Kildesystem.SØKNADSDIALOG)
    }

    fun byggK9DataBruktTilUtledning(metadata: MetaInfo): DataBruktTilUtledning = DataBruktTilUtledning()
        .medHarBekreftetOpplysninger(bekreftelser.harBekreftetOpplysninger)
        .medHarForståttRettigheterOgPlikter(bekreftelser.harForståttRettigheterOgPlikter)
        .medSoknadDialogCommitSha(metadata.soknadDialogCommitSha)
        .medAnnetData(dataBruktTilUtledningAnnetData)

    private fun byggK9OpptjeningAktivitet() = OpptjeningAktivitet().apply {
        frilans?.let { medFrilanser(it.somK9Frilanser()) }
        this@OmsorgspengerutbetalingSnfSøknad.selvstendigNæringsdrivende?.let { medSelvstendigNæringsdrivende(it.somK9SelvstendigNæringsdrivende()) }
    }

    override fun somKomplettSøknad(
        søker: Søker,
        k9Format: no.nav.k9.søknad.Innsending?,
        titler: List<String>,
    ): OmsorgspengerutbetalingSnfKomplettSøknad {
        requireNotNull(k9Format)
        return OmsorgspengerutbetalingSnfKomplettSøknad(
            søknadId = SøknadId.of(søknadId),
            mottatt = mottatt,
            språk = språk,
            søker = søker,
            bosteder = bosteder,
            opphold = opphold,
            spørsmål = spørsmål,
            harDekketTiFørsteDagerSelv = harDekketTiFørsteDagerSelv,
            harSyktBarn = harSyktBarn,
            harAleneomsorg = harAleneomsorg,
            bekreftelser = bekreftelser,
            utbetalingsperioder = utbetalingsperioder,
            erArbeidstakerOgså = erArbeidstakerOgså,
            barn = barn.kunFosterbarn(),
            frilans = frilans,
            selvstendigNæringsdrivende = selvstendigNæringsdrivende,
            vedleggId = vedlegg.map { it.toURI().dokumentId() },
            titler = titler,
            k9FormatSøknad = k9Format as Søknad
        )
    }

    override fun søkerNorskIdent(): String? = søkerNorskIdent

    override fun ytelse(): Ytelse = Ytelse.OMSORGSPENGER_UTBETALING_SNF
    override fun søknadId(): String = søknadId
    override fun vedlegg(): List<URL> = vedlegg
    override fun søknadValidator(): SøknadValidator<no.nav.k9.søknad.Søknad> = OmsorgspengerUtbetalingSøknadValidator()
}
