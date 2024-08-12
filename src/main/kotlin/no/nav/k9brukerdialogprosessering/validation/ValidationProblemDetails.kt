package no.nav.k9brukerdialogprosessering.validation

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.ErrorResponseException
import java.net.URI

data class ValidationProblemDetails(val violations: Set<Violation>): ProblemDetail(400) {
    init {
        type = URI("/problem-details/invalid-request-parameters")
        title = "invalid-request-parameters"
        detail = "Forespørselen inneholder valideringsfeil"
    }
}
data class ValidationProblemDetailsString(val violations: List<String>): ProblemDetail(400) {
    init {
        title = "invalid-request-parameters"
        detail = "Requesten inneholder ugyldige parametere"
        setProperty("invalid_parameters", violations)
    }
}

class ValidationErrorResponseException(val validationProblemDetails: ProblemDetail) : ErrorResponseException(HttpStatus.BAD_REQUEST, validationProblemDetails, null)

data class Violation(val parameterName : String, val parameterType: ParameterType, val reason: String, val invalidValue : Any? = null)
enum class ParameterType {
    QUERY,
    PATH,
    HEADER,
    ENTITY,
    FORM
}
