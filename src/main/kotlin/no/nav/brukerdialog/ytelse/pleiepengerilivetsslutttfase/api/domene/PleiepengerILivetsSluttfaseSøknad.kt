package no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.api.domene

import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.SøknadValidator
import no.nav.k9.søknad.felles.Kildesystem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.opptjening.OpptjeningAktivitet
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.DataBruktTilUtledning
import no.nav.k9.søknad.ytelse.pls.v1.PleiepengerLivetsSluttfaseSøknadValidator
import no.nav.k9.søknad.ytelse.pls.v1.PleipengerLivetsSluttfase
import no.nav.k9.søknad.ytelse.psb.v1.Uttak
import no.nav.k9.søknad.ytelse.psb.v1.Uttak.UttakPeriodeInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstid
import no.nav.brukerdialog.api.innsending.Innsending
import no.nav.brukerdialog.api.ytelse.Ytelse
import no.nav.brukerdialog.api.ytelse.fellesdomene.ArbeidUtils.SYV_OG_EN_HALV_TIME
import no.nav.brukerdialog.api.ytelse.fellesdomene.ArbeidUtils.arbeidstidInfoMedNullTimer
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.api.domene.Arbeidsgiver.Companion.somK9Arbeidstaker
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.api.domene.OpptjeningIUtlandet.Companion.valider
import no.nav.brukerdialog.ytelse.pleiepengerilivetsslutttfase.api.domene.UtenlandskNæring.Companion.valider
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.mellomlagring.dokument.dokumentId
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.brukerdialog.utils.krever
import no.nav.brukerdialog.validation.ValidationErrorResponseException
import no.nav.brukerdialog.validation.ValidationProblemDetailsString
import java.net.URL
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as K9Søknad

data class PleiepengerILivetsSluttfaseSøknad(
    @field:org.hibernate.validator.constraints.UUID(message = "Forventet gyldig UUID, men var '\${validatedValue}'") internal val søknadId: String = UUID.randomUUID().toString(),
    val språk: String,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val skalJobbeOgPleieSammeDag: Boolean,
    val dagerMedPleie: List<LocalDate>,
    val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    val vedleggUrls: List<URL> = listOf(),
    val opplastetIdVedleggUrls: List<URL> = listOf(),
    @field:Valid val pleietrengende: Pleietrengende,
    val medlemskap: Medlemskap,
    val utenlandsoppholdIPerioden: UtenlandsoppholdIPerioden,
    @field:Valid val arbeidsgivere: List<Arbeidsgiver>,
    @field:Valid val frilans: Frilans? = null,
    @field:Valid val selvstendigNæringsdrivende: SelvstendigNæringsdrivende? = null,
    val opptjeningIUtlandet: List<OpptjeningIUtlandet>,
    val utenlandskNæring: List<UtenlandskNæring>,
    val harVærtEllerErVernepliktig: Boolean? = null,
    val pleierDuDenSykeHjemme: Boolean? = null,

    @field:AssertTrue(message = "Opplysningene må bekreftes for å sende inn søknad")
    val harBekreftetOpplysninger: Boolean,

    @field:AssertTrue(message = "Må ha forstått rettigheter og plikter for å sende inn søknad")
    val harForståttRettigheterOgPlikter: Boolean,

    private val flereSokere: FlereSokereSvar? = null,
    private val dataBruktTilUtledningAnnetData: String? = null,
) : Innsending {
    companion object {
        private val K9_SØKNAD_VERSJON = Versjon.of("1.0.0")
    }

    override fun somKomplettSøknad(
        søker: Søker,
        k9Format: no.nav.k9.søknad.Innsending?,
        titler: List<String>,
    ): PilsKomplettSøknad {
        requireNotNull(k9Format)
        return PilsKomplettSøknad(
            søknadId = søknadId,
            søker = søker,
            språk = språk,
            fraOgMed = fraOgMed,
            tilOgMed = tilOgMed,
            skalJobbeOgPleieSammeDag = skalJobbeOgPleieSammeDag,
            dagerMedPleie = dagerMedPleie,
            mottatt = mottatt,
            vedleggId = vedleggUrls.map { it.toURI().dokumentId() },
            opplastetIdVedleggId = opplastetIdVedleggUrls.map { it.toURI().dokumentId() },
            medlemskap = medlemskap,
            pleietrengende = pleietrengende,
            utenlandsoppholdIPerioden = utenlandsoppholdIPerioden,
            frilans = frilans,
            arbeidsgivere = arbeidsgivere,
            opptjeningIUtlandet = opptjeningIUtlandet,
            utenlandskNæring = utenlandskNæring,
            selvstendigNæringsdrivende = selvstendigNæringsdrivende,
            harVærtEllerErVernepliktig = harVærtEllerErVernepliktig,
            pleierDuDenSykeHjemme = pleierDuDenSykeHjemme,
            harForståttRettigheterOgPlikter = harForståttRettigheterOgPlikter,
            harBekreftetOpplysninger = harBekreftetOpplysninger,
            flereSokere = flereSokere,
            k9Format = k9Format as Søknad
        )
    }

    override fun valider() = mutableListOf<String>().apply {
        addAll(medlemskap.valider())
        addAll(utenlandskNæring.valider())
        addAll(opptjeningIUtlandet.valider())
        addAll(utenlandsoppholdIPerioden.valider())

        pleierDuDenSykeHjemme?.let { krever(it, "pleierDuDenSykeHjemme må være true.") }
        if (isNotEmpty()) throw ValidationErrorResponseException(ValidationProblemDetailsString(this))
    }

    override fun somK9Format(søker: Søker, metadata: MetaInfo): K9Søknad {
        val ytelse = PleipengerLivetsSluttfase()
            .medSøknadsperiode(Periode(fraOgMed, tilOgMed))
            .medPleietrengende(pleietrengende.somK9Pleietrengende())
            .medBosteder(medlemskap.somK9Bosteder())
            .medOpptjeningAktivitet(byggK9OpptjeningAktivitet())
            .medUttak(byggK9Uttak())
            .medArbeidstid(byggK9Arbeidstid())
            .medDataBruktTilUtledning(byggK9DataBruktTilUtledning(metadata)) as PleipengerLivetsSluttfase

        if (utenlandsoppholdIPerioden.skalOppholdeSegIUtlandetIPerioden == true) {
            ytelse.medUtenlandsopphold(utenlandsoppholdIPerioden.somK9Utenlandsopphold())
        }

        return K9Søknad()
            .medVersjon(K9_SØKNAD_VERSJON)
            .medMottattDato(mottatt)
            .medSøknadId(SøknadId(søknadId))
            .medSøker(søker.somK9Søker())
            .medYtelse(ytelse)
            .medKildesystem(Kildesystem.SØKNADSDIALOG)
    }

    fun byggK9DataBruktTilUtledning(metadata: MetaInfo): DataBruktTilUtledning = DataBruktTilUtledning()
        .medHarBekreftetOpplysninger(harBekreftetOpplysninger)
        .medHarForståttRettigheterOgPlikter(harForståttRettigheterOgPlikter)
        .medSoknadDialogCommitSha(metadata.soknadDialogCommitSha)
        .medAnnetData(dataBruktTilUtledningAnnetData)

    private fun byggK9Uttak() =
        Uttak().medPerioder(mapOf(Periode(fraOgMed, tilOgMed) to UttakPeriodeInfo(SYV_OG_EN_HALV_TIME)))

    private fun byggK9OpptjeningAktivitet() = OpptjeningAktivitet().apply {
        frilans?.let { medFrilanser(it.somK9Frilanser()) }
        this@PleiepengerILivetsSluttfaseSøknad.selvstendigNæringsdrivende?.let { medSelvstendigNæringsdrivende(it.somK9SelvstendigNæringsdrivende()) }
    }

    private fun byggK9Arbeidstid() = Arbeidstid().apply {
        if (arbeidsgivere.isNotEmpty()) medArbeidstaker(arbeidsgivere.somK9Arbeidstaker(fraOgMed, tilOgMed))

        selvstendigNæringsdrivende?.let {
            medSelvstendigNæringsdrivendeArbeidstidInfo(
                it.somK9ArbeidstidInfo(
                    fraOgMed,
                    tilOgMed
                )
            )
        }

        when (frilans) {
            null -> medFrilanserArbeidstid(arbeidstidInfoMedNullTimer(fraOgMed, tilOgMed))
            else -> medFrilanserArbeidstid(frilans.somK9Arbeidstid(fraOgMed, tilOgMed))
        }
    }

    override fun ytelse(): Ytelse = Ytelse.PLEIEPENGER_LIVETS_SLUTTFASE

    override fun søknadId(): String = søknadId
    override fun vedlegg(): List<URL> = mutableListOf<URL>().apply {
        addAll(vedleggUrls)
        addAll(opplastetIdVedleggUrls)
    }

    override fun søknadValidator(): SøknadValidator<no.nav.k9.søknad.Søknad> =
        PleiepengerLivetsSluttfaseSøknadValidator()
}
