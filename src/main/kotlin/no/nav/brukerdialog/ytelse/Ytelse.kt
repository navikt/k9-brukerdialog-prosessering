package no.nav.brukerdialog.ytelse

import no.nav.brukerdialog.utils.Constants
import no.nav.brukerdialog.utils.MDCUtil
import no.nav.brukerdialog.utils.NavHeaders
import no.nav.brukerdialog.validation.ParameterType
import no.nav.brukerdialog.validation.ValidationErrorResponseException
import no.nav.brukerdialog.validation.ValidationProblemDetails
import no.nav.brukerdialog.validation.Violation
import org.springframework.http.HttpHeaders

enum class Ytelse {
    OMSORGSPENGER_UTVIDET_RETT,
    OMSORGSPENGER_MIDLERTIDIG_ALENE,
    ETTERSENDING,
    OMSORGSDAGER_ALENEOMSORG,
    OMSORGSPENGER_UTBETALING_ARBEIDSTAKER,
    OMSORGSPENGER_UTBETALING_SNF,
    PLEIEPENGER_LIVETS_SLUTTFASE,
    ETTERSENDING_PLEIEPENGER_SYKT_BARN,
    ETTERSENDING_PLEIEPENGER_LIVETS_SLUTTFASE,
    ETTERSENDING_OMP,
    PLEIEPENGER_SYKT_BARN,
    ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN,
    DINE_PLEIEPENGER,
    OPPLARINGSPENGER,
    UNGDOMSYTELSE,
    UNGDOMSYTELSE_INNTEKTSRAPPORTERING
    ;

    companion object {
        fun utledYtelseFraCallerIMDC(): Ytelse {
            return MDCUtil.fromMDC(Constants.CALLER_CLIENT_ID)?.somYtelse()!!
        }

        fun String.somYtelse(): Ytelse {
            return when(this) {
                "omsorgspengesoknad" -> OMSORGSPENGER_UTVIDET_RETT
                "ekstra-omsorgsdager-andre-forelder-ikke-tilsyn" -> OMSORGSPENGER_MIDLERTIDIG_ALENE
                "sif-ettersending" -> ETTERSENDING
                "omsorgsdager-aleneomsorg-dialog" -> OMSORGSDAGER_ALENEOMSORG
                "omsorgspengerutbetaling-arbeidstaker-soknad" -> OMSORGSPENGER_UTBETALING_ARBEIDSTAKER
                "omsorgspengerutbetaling-soknad" -> OMSORGSPENGER_UTBETALING_SNF
                "pleiepenger-i-livets-sluttfase-soknad" -> PLEIEPENGER_LIVETS_SLUTTFASE
                "pleiepengesoknad" -> PLEIEPENGER_SYKT_BARN
                "endringsmelding-pleiepenger" -> ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN
                "dine-pleiepenger" -> PLEIEPENGER_SYKT_BARN
                "opplaringspenger-soknad" -> OPPLARINGSPENGER
                "ungdomsytelse-deltaker" -> UNGDOMSYTELSE
                else -> throw ValidationErrorResponseException(
                    ValidationProblemDetails(
                        violations = setOf(
                            Violation(
                                parameterName = this,
                                parameterType = ParameterType.HEADER,
                                reason = "Ukjent dialog '$this'."
                            )
                        )
                    )
                )
            }
        }
    }

    fun somHttpHeader(): HttpHeaders {
        val headers = HttpHeaders()
        headers[NavHeaders.X_K9_YTELSE] = name
        return headers
    }
}
