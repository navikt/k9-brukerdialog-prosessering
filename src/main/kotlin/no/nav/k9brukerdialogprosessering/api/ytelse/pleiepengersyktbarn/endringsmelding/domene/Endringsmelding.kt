package no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.endringsmelding.domene

import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.SøknadValidator
import no.nav.k9.søknad.felles.Kildesystem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.felles.type.Språk
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarn
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarnSøknadValidator
import no.nav.k9brukerdialogprosessering.api.innsending.Innsending
import no.nav.k9brukerdialogprosessering.api.innsending.KomplettInnsending
import no.nav.k9brukerdialogprosessering.api.ytelse.Ytelse
import no.nav.k9brukerdialogprosessering.common.MetaInfo
import no.nav.k9brukerdialogprosessering.oppslag.soker.Søker
import java.net.URL
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.felles.personopplysninger.Søker as K9Søker

data class Endringsmelding(
    @field:org.hibernate.validator.constraints.UUID(message = "Forventet gyldig UUID, men var '\${validatedValue}'") val søknadId: String = UUID.randomUUID().toString(),
    val mottattDato: ZonedDateTime? = ZonedDateTime.now(UTC),
    val språk: String,
    var pleietrengendeNavn: String? = null,
    var gyldigeEndringsPerioder: List<Periode>? = null,

    @field:AssertTrue(message = "Opplysningene må bekreftes for å sende inn endringsmelding")
    val harBekreftetOpplysninger: Boolean,

    @field:AssertTrue(message = "Må ha forstått rettigheter og plikter for å sende inn endringsmelding")
    val harForståttRettigheterOgPlikter: Boolean,

    @field:Valid val ytelse: PleiepengerSyktBarn,
) : Innsending {
    override fun ytelse(): Ytelse = Ytelse.ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN

    override fun søknadId(): String = søknadId

    override fun vedlegg(): List<URL> = listOf()

    override fun valider(): List<String> = mutableListOf()

    override fun søknadValidator(): SøknadValidator<Søknad> = PleiepengerSyktBarnSøknadValidator()

    override fun somK9Format(søker: Søker, metadata: MetaInfo): no.nav.k9.søknad.Innsending {
        return Søknad(
            SøknadId(søknadId),
            Versjon("1.0.0"),
            mottattDato,
            K9Søker(NorskIdentitetsnummer.of(søker.fødselsnummer)),
            Språk.of(språk),
            ytelse
        )
            .medKildesystem(Kildesystem.ENDRINGSDIALOG)
    }

    override fun gyldigeEndringsPerioder(): List<Periode>? = gyldigeEndringsPerioder

    override fun somKomplettSøknad(
        søker: Søker,
        k9Format: no.nav.k9.søknad.Innsending?,
        titler: List<String>,
    ): KomplettInnsending = KomplettEndringsmelding(
        søknadId = søknadId,
        søker = søker,
        pleietrengendeNavn = pleietrengendeNavn!!,
        harBekreftetOpplysninger = harBekreftetOpplysninger,
        harForståttRettigheterOgPlikter = harForståttRettigheterOgPlikter,
        k9Format = k9Format as Søknad
    )
}


data class KomplettEndringsmelding(
    val søknadId: String,
    val søker: Søker,
    val harBekreftetOpplysninger: Boolean,
    val harForståttRettigheterOgPlikter: Boolean,
    val k9Format: Søknad,
    val pleietrengendeNavn: String,
) : KomplettInnsending
