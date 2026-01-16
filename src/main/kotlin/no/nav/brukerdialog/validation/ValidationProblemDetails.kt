package no.nav.brukerdialog.validation

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.ErrorResponseException
import java.net.URI

class ValidationProblemDetails(violations: Set<Violation>): ProblemDetail(400) {
    init {
        type = URI("/problem-details/invalid-request-parameters")
        title = "invalid-request-parameters"
        detail = "Forespørselen inneholder valideringsfeil"
        setProperty("violations", violations)
    }

    @get:JsonIgnore
    val violations: Set<Violation>
        get() = getProperties()?.get("violations") as? Set<Violation> ?: emptySet()
}

class ValidationProblemDetailsString(violations: List<String>): ProblemDetail(400) {
    init {
        title = "invalid-request-parameters"
        detail = "Requesten inneholder ugyldige parametere"
        setProperty("invalid_parameters", violations)
    }

    @get:JsonIgnore
    val violations: List<String>
        get() = getProperties()?.get("invalid_parameters") as? List<String> ?: emptyList()
}

class ValidationErrorResponseException(val validationProblemDetails: ProblemDetail) : ErrorResponseException(HttpStatus.BAD_REQUEST, validationProblemDetails, null)

data class Violation(val parameterName : String, val parameterType: ParameterType, val reason: String, val invalidValue : Any? = null) {
    override fun toString(): String {
        val safeInvalidValue = if (invalidValue == null) "null" else "[...]"
        return "Violation(parameterName='$parameterName', parameterType=$parameterType, reason='$reason', invalidValue=$safeInvalidValue)"
    }
}
enum class ParameterType {
    QUERY,
    PATH,
    HEADER,
    ENTITY,
    FORM
}
