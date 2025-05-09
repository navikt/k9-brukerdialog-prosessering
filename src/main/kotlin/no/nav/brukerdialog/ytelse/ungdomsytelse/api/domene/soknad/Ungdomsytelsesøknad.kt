package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.soknad

import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.AssertTrue
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.domenetjenester.innsending.Innsending
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.brukerdialog.ytelse.Ytelse
import no.nav.k9.søknad.Søknad
import no.nav.k9.søknad.SøknadValidator
import no.nav.k9.søknad.felles.Kildesystem
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.type.Språk
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.ung.v1.UngSøknadstype
import no.nav.k9.søknad.ytelse.ung.v1.Ungdomsytelse
import no.nav.k9.søknad.ytelse.ung.v1.UngdomsytelseSøknadValidator
import java.net.URL
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as UngSøknad

data class Ungdomsytelsesøknad(
    @field:org.hibernate.validator.constraints.UUID(message = "Forventet gyldig UUID, men var '\${validatedValue}'")
    @Schema(hidden = true)
    val søknadId: String = UUID.randomUUID().toString(),
    val språk: String,

    @Schema(hidden = true)
    val mottatt: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),

    val startdato: LocalDate,
    val søkerNorskIdent: String,

    @Hidden
    var barn: List<Barn>? = null, // Settes i UngdomsytelseService.
    @field:AssertTrue(message = "Må bekrefte at opplysningene om barn er riktige for å sende inn søknad")
    val barnErRiktig: Boolean,

    val kontonummerFraRegister: String? = null,
    @field:AssertTrue(message = "Må bekrefte at kontonummeret er riktig for å sende inn søknad")
    val kontonummerErRiktig: Boolean? = null,

    @field:AssertTrue(message = "Opplysningene må bekreftes for å sende inn søknad")
    val harBekreftetOpplysninger: Boolean,
    @field:AssertTrue(message = "Må ha forstått rettigheter og plikter for å sende inn søknad")
    val harForståttRettigheterOgPlikter: Boolean,

    ) : Innsending {
    companion object {
        private val K9_SØKNAD_VERSJON = Versjon.of("1.0.0")
        private val SØKNAD_TYPE = UngSøknadstype.DELTAKELSE_SØKNAD
    }

    @Hidden
    @AssertTrue(message = "Dersom kontonummerFraRegister er satt, må kontonummerErRiktig være satt")
    fun isKontonummerErRiktig(): Boolean = if (!kontonummerFraRegister.isNullOrBlank()) {
        kontonummerErRiktig !== null
    } else true

    override fun somKomplettSøknad(
        søker: Søker,
        k9Format: no.nav.k9.søknad.Innsending?,
        titler: List<String>,
    ): UngdomsytelseKomplettSøknad {
        requireNotNull(k9Format)
        return UngdomsytelseKomplettSøknad(
            søknadId = søknadId,
            mottatt = mottatt,
            søker = søker,
            språk = språk,
            startdato = startdato,
            barn = barn!!,
            barnErRiktig = barnErRiktig,
            kontonummerFraRegister = kontonummerFraRegister,
            kontonummerErRiktig = kontonummerErRiktig,
            harForståttRettigheterOgPlikter = harForståttRettigheterOgPlikter,
            harBekreftetOpplysninger = harBekreftetOpplysninger,
            k9Format = k9Format as UngSøknad
        )
    }

    override fun valider() = mutableListOf<String>()

    override fun somK9Format(søker: Søker, metadata: MetaInfo): UngSøknad {
        val ytelse = Ungdomsytelse()
            .medStartdato(startdato)
            .medSøknadType(SØKNAD_TYPE)

        return UngSøknad()
            .medVersjon(K9_SØKNAD_VERSJON)
            .medMottattDato(mottatt)
            .medSpråk(Språk.of(språk))
            .medSøknadId(SøknadId(søknadId))
            .medSøker(søker.somK9Søker())
            .medYtelse(ytelse)
            .medKildesystem(Kildesystem.SØKNADSDIALOG)
    }

    override fun søkerNorskIdent(): String? = søkerNorskIdent
    override fun ytelse(): Ytelse = Ytelse.UNGDOMSYTELSE
    override fun søknadId(): String = søknadId
    override fun vedlegg(): List<URL> = mutableListOf()
    override fun søknadValidator(): SøknadValidator<Søknad> = UngdomsytelseSøknadValidator()
}
