package no.nav.brukerdialog.ytelse.omsorgpengerutbetalingat.api.domene

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import no.nav.brukerdialog.ytelse.fellesdomene.Utbetalingsperiode
import no.nav.brukerdialog.ytelse.fellesdomene.Utbetalingsperiode.Companion.somK9FraværPeriode
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingat.api.domene.Utbetalingsårsak.KONFLIKT_MED_ARBEIDSGIVER
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingat.api.domene.Utbetalingsårsak.NYOPPSTARTET_HOS_ARBEIDSGIVER
import no.nav.k9.søknad.felles.fravær.SøknadÅrsak

class Arbeidsgiver(
    @field:NotBlank(message = "Kan ikke være blankt eller tomt")
    private val navn: String,

    @field:NotBlank(message = "Kan ikke være blankt eller tomt")
    @field:Size(max = 20, message = "Kan ikke være lengre enn 20 tegn")
    @field:Pattern(regexp = "^\\d+$", message = "'\${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private val organisasjonsnummer: String,

    private val utbetalingsårsak: Utbetalingsårsak,

    @field:Valid
    @field:NotEmpty(message = "Kan ikke være tom")
    private val perioder: List<Utbetalingsperiode>,

    private val konfliktForklaring: String? = null,
    @get:JsonProperty("årsakNyoppstartet")
    private val årsakNyoppstartet: ÅrsakNyoppstartet? = null,

    @field:NotNull(message = "Kan ikke være null")
    private val arbeidsgiverHarUtbetaltLønn: Boolean,

    @field:NotNull(message = "Kan ikke være null")
    private val harHattFraværHosArbeidsgiver: Boolean,
) {
    companion object {
        internal fun List<Arbeidsgiver>.somK9Fraværsperiode() = this.flatMap { it.somK9Fraværsperiode() }
    }

    @Hidden
    @AssertTrue(message = "årsakNyoppstartet må være satt dersom Utbetalingsårsak=NYOPPSTARTET_HOS_ARBEIDSGIVER")
    fun isÅrsakNyoppstartet(): Boolean {
        return when (utbetalingsårsak) {
            NYOPPSTARTET_HOS_ARBEIDSGIVER -> årsakNyoppstartet != null
            else -> true
        }
    }

    @Hidden
    @AssertTrue(message = "konfliktForklaring må være satt dersom Utbetalingsårsak=KONFLIKT_MED_ARBEIDSGIVER")
    fun isKonfliktForklaring(): Boolean {
        return when (utbetalingsårsak) {
            KONFLIKT_MED_ARBEIDSGIVER -> !konfliktForklaring.isNullOrBlank()
            else -> true
        }
    }

    internal fun somK9Fraværsperiode() =
        perioder.somK9FraværPeriode(utbetalingsårsak.somSøknadÅrsak(), organisasjonsnummer)
}

enum class Utbetalingsårsak {
    ARBEIDSGIVER_KONKURS,
    NYOPPSTARTET_HOS_ARBEIDSGIVER,
    KONFLIKT_MED_ARBEIDSGIVER;

    fun somSøknadÅrsak() = when (this) {
        ARBEIDSGIVER_KONKURS -> SøknadÅrsak.ARBEIDSGIVER_KONKURS
        NYOPPSTARTET_HOS_ARBEIDSGIVER -> SøknadÅrsak.NYOPPSTARTET_HOS_ARBEIDSGIVER
        KONFLIKT_MED_ARBEIDSGIVER -> SøknadÅrsak.KONFLIKT_MED_ARBEIDSGIVER
    }

}

enum class ÅrsakNyoppstartet {
    JOBBET_HOS_ANNEN_ARBEIDSGIVER,
    VAR_FRILANSER,
    VAR_SELVSTENDIGE,
    SØKTE_ANDRE_UTBETALINGER,
    ARBEID_I_UTLANDET,
    UTØVDE_VERNEPLIKT,
    ANNET
}
