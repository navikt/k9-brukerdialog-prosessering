package no.nav.k9brukerdialogprosessering.api.ytelse.omsorgspengerutbetalingsnf.domene

import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.SøknadValidator
import no.nav.k9.søknad.felles.Kildesystem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.opptjening.OpptjeningAktivitet
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.DataBruktTilUtledning
import no.nav.k9.søknad.ytelse.omsorgspenger.v1.OmsorgspengerUtbetaling
import no.nav.k9.søknad.ytelse.omsorgspenger.v1.OmsorgspengerUtbetalingSøknadValidator
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Bekreftelser
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Utbetalingsperiode
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Utbetalingsperiode.Companion.somK9FraværPeriode
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Utbetalingsperiode.Companion.valider
import no.nav.k9brukerdialogapi.ytelse.fellesdomene.Virksomhet
import no.nav.k9brukerdialogprosessering.api.innsending.Innsending
import no.nav.k9brukerdialogprosessering.api.ytelse.Ytelse
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Bosted
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Bosted.Companion.somK9Bosteder
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Bosted.Companion.somK9Utenlandsopphold
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Bosted.Companion.valider
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Opphold
import no.nav.k9brukerdialogprosessering.api.ytelse.omsorgspengerutbetalingsnf.domene.Barn.Companion.kunFosterbarn
import no.nav.k9brukerdialogprosessering.api.ytelse.omsorgspengerutbetalingsnf.domene.Barn.Companion.somK9BarnListe
import no.nav.k9brukerdialogprosessering.api.ytelse.omsorgspengerutbetalingsnf.domene.Barn.Companion.valider
import no.nav.k9brukerdialogprosessering.common.MetaInfo
import no.nav.k9brukerdialogprosessering.mellomlagring.dokument.dokumentId
import no.nav.k9brukerdialogprosessering.oppslag.barn.BarnOppslag
import no.nav.k9brukerdialogprosessering.oppslag.soker.Søker
import no.nav.k9brukerdialogprosessering.validation.ValidationErrorResponseException
import no.nav.k9brukerdialogprosessering.validation.ValidationProblemDetailsString
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as K9Søknad

class OmsorgspengerutbetalingSnfSøknad(
    internal val søknadId: SøknadId = SøknadId(UUID.randomUUID().toString()),
    private val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    private val språk: String,
    private val bosteder: List<Bosted>,
    private val opphold: List<Opphold>,
    private val spørsmål: List<SpørsmålOgSvar>,
    private val harDekketTiFørsteDagerSelv: Boolean? = null,
    private val harSyktBarn: Boolean? = null,
    private val harAleneomsorg: Boolean? = null,
    private val bekreftelser: Bekreftelser,
    private val utbetalingsperioder: List<Utbetalingsperiode>,
    private val erArbeidstakerOgså: Boolean,
    private val barn: List<Barn>,
    private val frilans: Frilans? = null,
    private val selvstendigNæringsdrivende: Virksomhet? = null,
    internal val vedlegg: List<URL> = listOf(),
    private val dataBruktTilUtledningAnnetData: String? = null
): Innsending {

    companion object {
        private val k9FormatVersjon = Versjon.of("1.1.0")
    }

    override fun valider() = mutableListOf<String>().apply {
        addAll(bosteder.valider("bosteder"))
        addAll(opphold.valider("opphold"))
        addAll(bekreftelser.valider("bekreftelser"))
        addAll(utbetalingsperioder.valider("utbetalingsperioder"))
        addAll(barn.valider("barn"))
        frilans?.let { addAll(it.valider("frilans")) }
        selvstendigNæringsdrivende?.let { addAll(it.valider("selvstendigNæringsdrivende")) }

        if (isNotEmpty()) throw ValidationErrorResponseException(ValidationProblemDetailsString(this))
    }

    internal fun leggTilIdentifikatorPåBarnHvisMangler(barnFraOppslag: List<BarnOppslag>) {
        barn.forEach { it.leggTilIdentifikatorHvisMangler(barnFraOppslag) }
    }

    override fun somK9Format(søker: Søker, metadata: MetaInfo): no.nav.k9.søknad.Søknad {
        return K9Søknad(
            søknadId,
            k9FormatVersjon,
            mottatt,
            søker.somK9Søker(),
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
            søknadId = søknadId,
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

    override fun ytelse(): Ytelse = Ytelse.OMSORGSPENGER_UTBETALING_SNF
    override fun søknadId(): String = søknadId.id
    override fun vedlegg(): List<URL> = vedlegg
    override fun søknadValidator(): SøknadValidator<no.nav.k9.søknad.Søknad> = OmsorgspengerUtbetalingSøknadValidator()
}
