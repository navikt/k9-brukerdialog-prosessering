package no.nav.brukerdialog.ytelse.omsorgpengerutbetalingat.api.domene

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
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
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingat.api.domene.Arbeidsgiver.Companion.somK9Fraværsperiode
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingat.api.domene.Barn.Companion.somK9BarnListe
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

private val k9FormatVersjon = Versjon.of("1.1.0")

data class OmsorgspengerutbetalingArbeidstakerSøknad(
    @field:org.hibernate.validator.constraints.UUID(message = "Forventet gyldig UUID, men var '\${validatedValue}'")
    val søknadId: String = UUID.randomUUID().toString(),
    val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    val språk: String,
    val vedlegg: List<URL>,
    val søkerNorskIdent: String? = null, // TODO: Fjern nullable når vi har lansert og mellomlagring inneholder dette feltet.

    @field:Valid val bosteder: List<Bosted>,
    @field:Valid val opphold: List<Opphold>,

    @field:Valid private val bekreftelser: Bekreftelser,

    @field:NotEmpty(message = "Må ha minst en arbeidsgiver satt")
    @field:Valid
    private val arbeidsgivere: List<Arbeidsgiver>,

    @field:Valid private val dineBarn: DineBarn,
    private val hjemmePgaSmittevernhensyn: Boolean,
    private val hjemmePgaStengtBhgSkole: Boolean? = null,
    private val dataBruktTilUtledningAnnetData: String? = null,
) : Innsending {
    override fun valider() = mutableListOf<String>()

    override fun somKomplettSøknad(
        søker: Søker,
        k9Format: no.nav.k9.søknad.Innsending?,
        titler: List<String>,
    ): OmsorgspengerutbetalingArbeidstakerKomplettSøknad {
        requireNotNull(k9Format)
        return OmsorgspengerutbetalingArbeidstakerKomplettSøknad(
            søknadId = søknadId,
            språk = språk,
            mottatt = mottatt,
            søker = søker,
            bosteder = bosteder,
            opphold = opphold,
            arbeidsgivere = arbeidsgivere,
            dineBarn = dineBarn,
            bekreftelser = bekreftelser,
            vedleggId = vedlegg.map { it.toURI().dokumentId() },
            titler = titler,
            hjemmePgaSmittevernhensyn = hjemmePgaSmittevernhensyn,
            hjemmePgaStengtBhgSkole = hjemmePgaStengtBhgSkole,
            k9Format = k9Format as Søknad
        )
    }

    internal fun leggTilIdentifikatorPåBarnHvisMangler(barnFraOppslag: List<BarnOppslag>) {
        dineBarn.barn.forEach { it.leggTilIdentifikatorHvisMangler(barnFraOppslag) }
    }

    internal fun leggTilRegistrerteBarn(barnFraOppslag: List<BarnOppslag>) {
        dineBarn.barn += barnFraOppslag.map {
            Barn(
                identitetsnummer = it.identitetsnummer,
                aktørId = it.aktørId,
                fødselsdato = it.fødselsdato,
                navn = it.navn(),
                type = TypeBarn.FRA_OPPSLAG
            )
        }
    }

    override fun somK9Format(søker: Søker, metadata: MetaInfo): no.nav.k9.søknad.Søknad {
        return K9Søknad(
            SøknadId.of(søknadId),
            k9FormatVersjon,
            mottatt,
            søker.somK9Søker(),
            Språk.of(språk),
            OmsorgspengerUtbetaling(
                dineBarn.barn.somK9BarnListe(),
                OpptjeningAktivitet(),
                arbeidsgivere.somK9Fraværsperiode(),
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

    override fun søknadValidator(): SøknadValidator<no.nav.k9.søknad.Søknad> = OmsorgspengerUtbetalingSøknadValidator()
    override fun søkerNorskIdent(): String? = søkerNorskIdent

    override fun ytelse(): Ytelse = Ytelse.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER
    override fun søknadId(): String = søknadId
    override fun vedlegg(): List<URL> = vedlegg
}
