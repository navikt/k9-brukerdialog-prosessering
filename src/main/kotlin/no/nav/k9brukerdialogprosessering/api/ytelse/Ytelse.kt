package no.nav.k9brukerdialogprosessering.api.ytelse

import no.nav.k9brukerdialogprosessering.utils.Constants
import no.nav.k9brukerdialogprosessering.utils.MDCUtil
import no.nav.k9brukerdialogprosessering.utils.NavHeaders
import no.nav.k9brukerdialogprosessering.validation.ParameterType
import no.nav.k9brukerdialogprosessering.validation.ValidationErrorResponseException
import no.nav.k9brukerdialogprosessering.validation.ValidationProblemDetails
import no.nav.k9brukerdialogprosessering.validation.Violation
import org.springframework.http.HttpHeaders

enum class Ytelse(val dialog: String) {
    OMSORGSPENGER_UTVIDET_RETT("omsorgspengesoknad"),
    OMSORGSPENGER_MIDLERTIDIG_ALENE("ekstra-omsorgsdager-andre-forelder-ikke-tilsyn"),
    ETTERSENDING("sif-ettersending"),
    OMSORGSDAGER_ALENEOMSORG("omsorgsdager-aleneomsorg-dialog"),
    OMSORGSPENGER_UTBETALING_ARBEIDSTAKER("omsorgspengerutbetaling-arbeidstaker-soknad"),
    OMSORGSPENGER_UTBETALING_SNF("omsorgspengerutbetaling-soknad"),
    PLEIEPENGER_LIVETS_SLUTTFASE("pleiepenger-i-livets-sluttfase-soknad"),
    ETTERSENDING_PLEIEPENGER_SYKT_BARN("sif-ettersending"),
    ETTERSENDING_PLEIEPENGER_LIVETS_SLUTTFASE("sif-ettersending"),
    ETTERSENDING_OMP("sif-ettersending"),
    PLEIEPENGER_SYKT_BARN("pleiepengesoknad"),
    ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN("endringsmelding-pleiepenger"),
    DINE_PLEIEPENGER("dine-pleiepenger");

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
