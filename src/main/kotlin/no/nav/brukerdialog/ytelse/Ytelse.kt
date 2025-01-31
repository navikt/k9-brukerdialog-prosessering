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
        fun fraMDC(): Ytelse {
            val ytelse: String = MDCUtil.fromMDC(Constants.YTELSE)
                ?: throw ValidationErrorResponseException(
                    ValidationProblemDetails(
                        violations = setOf(
                            Violation(
                                parameterName = NavHeaders.X_K9_YTELSE,
                                parameterType = ParameterType.HEADER,
                                reason = "Påkrevd Ytelse mangler i MDC. Sjekk at headeren '${NavHeaders.X_K9_YTELSE}' er satt."
                            )
                        )
                    )
                )

            return runCatching { valueOf(ytelse) }
                .getOrElse {
                    throw ValidationErrorResponseException(
                        ValidationProblemDetails(
                            violations = setOf(
                                Violation(
                                    parameterName = NavHeaders.X_K9_YTELSE,
                                    parameterType = ParameterType.HEADER,
                                    reason = "Ukjent Ytelse '$ytelse'."
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
