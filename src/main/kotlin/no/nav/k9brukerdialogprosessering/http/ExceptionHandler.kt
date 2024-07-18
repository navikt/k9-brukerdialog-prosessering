package no.nav.k9brukerdialogprosessering.http

import jakarta.validation.ConstraintViolationException
import no.nav.k9brukerdialogprosessering.validation.ParameterType
import no.nav.k9brukerdialogprosessering.validation.ValidationProblemDetails
import no.nav.k9brukerdialogprosessering.validation.Violation
import no.nav.security.token.support.core.exceptions.JwtTokenMissingException
import no.nav.security.token.support.core.exceptions.JwtTokenValidatorException
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.Charset
import java.util.stream.Collectors

@RestControllerAdvice
class ExceptionHandler : ResponseEntityExceptionHandler() {

    companion object {
        private val log: Logger =
            LoggerFactory.getLogger(no.nav.k9brukerdialogprosessering.http.ExceptionHandler::class.java)
    }


    @ExceptionHandler(value = [Exception::class])
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    fun håndtereGeneriskException(exception: Exception, request: ServletWebRequest): ProblemDetail {
        val problemDetails = request.respondProblemDetails(
            status = INTERNAL_SERVER_ERROR,
            title = "Et uventet feil har oppstått",
            type = URI("/problem-details/internal-server-error"),
            detail = exception.message ?: ""
        )
        log.error("{}", problemDetails, exception)
        return problemDetails
    }

    @ExceptionHandler(value = [JwtTokenUnauthorizedException::class])
    @ResponseStatus(UNAUTHORIZED)
    fun håndtereTokenUnauthorizedException(
        exception: JwtTokenUnauthorizedException,
        request: ServletWebRequest,
    ): ProblemDetail {
        val problemDetails = request.respondProblemDetails(
            status = UNAUTHORIZED,
            title = "Ikke autentisert",
            type = URI("/problem-details/uautentisert-forespørsel"),
            detail = exception.message ?: ""
        )
        log.debug("{}", problemDetails)
        return problemDetails
    }

    @ExceptionHandler(JwtTokenValidatorException::class)
    @ResponseStatus(FORBIDDEN)
    fun håndtereTokenUnauthenticatedException(
        exception: JwtTokenValidatorException,
        request: ServletWebRequest,
    ): ProblemDetail {
        val problemDetails = request.respondProblemDetails(
            status = FORBIDDEN,
            title = "Ikke uautorisert",
            type = URI("/problem-details/uautorisert-forespørsel"),
            detail = exception.message ?: ""
        )
        log.debug("{}", problemDetails)
        return problemDetails
    }

    @ExceptionHandler(JwtTokenMissingException::class)
    @ResponseStatus(UNAUTHORIZED)
    fun håndtereJwtTokenMissingException(
        exception: JwtTokenMissingException,
        request: ServletWebRequest,
    ): ProblemDetail {
        val problemDetails = request.respondProblemDetails(
            status = UNAUTHORIZED,
            title = "Ingen token funnet.",
            type = URI("/problem-details/mangler-token"),
            detail = exception.message ?: ""
        )
        log.debug("{}", problemDetails)
        return problemDetails
    }

    @ExceptionHandler(value = [ConstraintViolationException::class])
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun håndtereConstraintViolationException(
        exception: ConstraintViolationException,
        request: ServletWebRequest,
    ): ProblemDetail {
        val violations = exception.constraintViolations
            .map {
                Violation(
                    parameterName = it.propertyPath.toString(),
                    parameterType = ParameterType.ENTITY,
                    reason = it.message,
                    invalidValue = it.invalidValue,
                )
            }.toSortedSet { o1, o2 -> o1.parameterName.compareTo(o2.parameterName) }
        val validationProblemDetails = ValidationProblemDetails(violations)

        val problemDetails = request.respondProblemDetails(
            status = HttpStatus.valueOf(validationProblemDetails.status),
            title = validationProblemDetails.title!!,
            type = validationProblemDetails.type,
            properties = validationProblemDetails.properties!!,
            detail = validationProblemDetails.detail!!
        )

        log.error("Validerigsfeil: {}", problemDetails)
        return problemDetails
    }

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        val violations = ex.allErrors.stream()
            .map { it.somViolation() }
            .sorted(Comparator.comparing(Violation::parameterName).thenComparing(Violation::reason))
            .collect(Collectors.toSet())

        val validationProblemDetails = ValidationProblemDetails(violations)

        val servletWebRequest = request as ServletWebRequest

        val problemDetails = servletWebRequest.respondProblemDetails(
            status = HttpStatus.valueOf(validationProblemDetails.status),
            title = validationProblemDetails.title!!,
            type = validationProblemDetails.type,
            properties = validationProblemDetails.properties!!,
            detail = validationProblemDetails.detail!!
        )

        log.error("Valideringsfeil: {}", problemDetails)
        return ResponseEntity(problemDetails, headers, status)
    }

    private fun ObjectError.somViolation(): Violation {
        this as FieldError
        return Violation(
            parameterName = "$objectName.$field",
            parameterType = ParameterType.ENTITY,
            reason = defaultMessage!!,
            invalidValue = rejectedValue,
        )
    }

    private fun ServletWebRequest.respondProblemDetails(
        status: HttpStatus,
        title: String,
        type: URI,
        properties: Map<String, Any> = mapOf(),
        detail: String,
    ): ProblemDetail {
        val problemDetail = ProblemDetail.forStatusAndDetail(status, detail)
        problemDetail.title = title
        problemDetail.type = type
        problemDetail.instance = URI(URLDecoder.decode(request.requestURL.toString(), Charset.defaultCharset()))
        properties.forEach {
            problemDetail.setProperty(it.key, it.value)
        }
        return problemDetail
    }
}
