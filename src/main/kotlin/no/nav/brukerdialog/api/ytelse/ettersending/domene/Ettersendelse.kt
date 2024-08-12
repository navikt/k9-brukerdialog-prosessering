package no.nav.brukerdialog.api.ytelse.ettersending.domene

import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotEmpty
import no.nav.k9.ettersendelse.Ettersendelse
import no.nav.k9.ettersendelse.EttersendelseType
import no.nav.k9.ettersendelse.EttersendelseValidator
import no.nav.k9.søknad.SøknadValidator
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.brukerdialog.api.innsending.Innsending
import no.nav.brukerdialog.api.ytelse.Ytelse
import no.nav.brukerdialog.api.ytelse.pleiepengersyktbarn.soknad.domene.hentIdentitetsnummerForBarn
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.mellomlagring.dokument.dokumentId
import no.nav.brukerdialog.oppslag.barn.BarnOppslag
import no.nav.brukerdialog.oppslag.soker.Søker
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

data class Ettersendelse(
    @field:org.hibernate.validator.constraints.UUID(message = "Forventet gyldig UUID, men var '\${validatedValue}'") val søknadId: String = UUID.randomUUID().toString(),
    val språk: String,
    val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
    @field:NotEmpty(message = "Kan ikke være tom") val vedlegg: List<URL>,
    val beskrivelse: String? = null,
    val søknadstype: Søknadstype,
    val ettersendelsesType: EttersendelseType,
    @field:Valid val pleietrengende: Pleietrengende? = null,

    @field:AssertTrue(message = "Opplysningene må bekreftes for å sende inn ettersendelse")
    val harBekreftetOpplysninger: Boolean,

    @field:AssertTrue(message = "Må ha forstått rettigheter og plikter for å sende inn ettersendelse")
    val harForståttRettigheterOgPlikter: Boolean,
) : Innsending {

    @AssertTrue(message = "Pleietrengende må være satt dersom ettersendelsen gjelder legeerklæring")
    fun isPleietrengende(): Boolean {
        if (ettersendelsesType == EttersendelseType.LEGEERKLÆRING) {
            return pleietrengende != null
        }
        return true
    }

    override fun valider() = mutableListOf<String>()

    override fun somKomplettSøknad(
        søker: Søker,
        k9Format: no.nav.k9.søknad.Innsending?,
        titler: List<String>,
    ): KomplettEttersendelse {
        requireNotNull(k9Format)
        return KomplettEttersendelse(
            søker = søker,
            språk = språk,
            mottatt = mottatt,
            vedleggId = vedlegg.map { it.toURI().dokumentId() },
            søknadId = søknadId,
            harForståttRettigheterOgPlikter = harForståttRettigheterOgPlikter,
            harBekreftetOpplysninger = harBekreftetOpplysninger,
            beskrivelse = beskrivelse,
            søknadstype = søknadstype,
            ettersendelsesType = ettersendelsesType,
            pleietrengende = pleietrengende,
            titler = titler,
            k9Format = k9Format as Ettersendelse
        )
    }

    override fun somK9Format(søker: Søker, metadata: MetaInfo): Ettersendelse {
        val ettersendelse = Ettersendelse.builder()
            .søknadId(SøknadId(søknadId))
            .mottattDato(mottatt)
            .søker(søker.somK9Søker())
            .ytelse(søknadstype.somK9Ytelse())
            .type(ettersendelsesType)

        pleietrengende?.let { ettersendelse.pleietrengende(it.tilK9Pleietrengende()) }
        return ettersendelse.build()
    }

    override fun ytelse(): Ytelse = Ytelse.ETTERSENDING

    override fun søknadId(): String = søknadId

    override fun vedlegg(): List<URL> = vedlegg

    override fun ettersendelseValidator(): SøknadValidator<Ettersendelse> = EttersendelseValidator()
    fun leggTilIdentifikatorPåBarnHvisMangler(barnFraOppslag: List<BarnOppslag>) {
        if (pleietrengende != null && pleietrengende.manglerIdentitetsnummer()) {
            pleietrengende oppdaterFødselsnummer barnFraOppslag.hentIdentitetsnummerForBarn(pleietrengende.aktørId)
        }
    }
}
