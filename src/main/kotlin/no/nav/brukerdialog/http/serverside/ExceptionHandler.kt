package no.nav.brukerdialog.http.serverside

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import jakarta.validation.ConstraintViolationException
import no.nav.brukerdialog.validation.ParameterType
import no.nav.brukerdialog.validation.ValidationProblemDetails
import no.nav.brukerdialog.validation.Violation
import no.nav.security.token.support.core.exceptions.JwtTokenMissingException
import no.nav.security.token.support.core.exceptions.JwtTokenValidatorException
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.Charset
import java.util.stream.Collectors

@RestControllerAdvice
class ExceptionHandler(
    @Value("\${spring.servlet.multipart.max-file-size}") private val maxFileSize: String,
) : ResponseEntityExceptionHandler() {

    companion object {
        private val log: Logger =
            LoggerFactory.getLogger(no.nav.brukerdialog.http.serverside.ExceptionHandler::class.java)
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
            }.toSortedSet { o1: Violation, o2: Violation ->
                compareValuesBy(o1, o2, Violation::parameterName, Violation::reason)
            }
        val validationProblemDetails = ValidationProblemDetails(violations)

        val problemDetails = request.respondProblemDetails(
            status = HttpStatus.valueOf(validationProblemDetails.status),
            title = validationProblemDetails.title!!,
            type = validationProblemDetails.type,
            violations = validationProblemDetails.violations,
            detail = validationProblemDetails.detail!!
        )

        log.error("Validerigsfeil: {}", problemDetails)
        return problemDetails
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        request as ServletWebRequest
        if (ex.cause is MismatchedInputException) {
            return handleMismatchedInputException(ex, request, headers, status)
        }
        return super.handleHttpMessageNotReadable(ex, headers, status, request)
    }

    private fun handleMismatchedInputException(
        ex: HttpMessageNotReadableException,
        request: ServletWebRequest,
        headers: HttpHeaders,
        status: HttpStatusCode,
    ): ResponseEntity<Any> {
        val violations = (ex.cause as MismatchedInputException).path
            .map {
                val feltNavn = "${it.from.toString().substringAfterLast(".")}.${it.fieldName}"
                Violation(
                    parameterName = feltNavn,
                    parameterType = ParameterType.ENTITY,
                    reason = "$feltNavn er påkrevd, men var ikke satt",
                    invalidValue = null,
                )
            }.toSortedSet { o1: Violation, o2: Violation ->
                compareValuesBy(o1, o2, Violation::parameterName, Violation::reason)
            }
        val validationProblemDetails = ValidationProblemDetails(violations)

        val problemDetails = request.respondProblemDetails(
            status = HttpStatus.valueOf(validationProblemDetails.status),
            title = validationProblemDetails.title!!,
            type = validationProblemDetails.type,
            violations = validationProblemDetails.violations,
            detail = validationProblemDetails.detail!!
        )

        log.error("Validerigsfeil: {}", problemDetails, ex)
        return ResponseEntity(problemDetails, headers, status)
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
            violations = validationProblemDetails.violations,
            detail = validationProblemDetails.detail!!
        )

        log.error("Valideringsfeil: {}", problemDetails)
        return ResponseEntity(problemDetails, headers, status)
    }

    override fun handleMaxUploadSizeExceededException(
        ex: MaxUploadSizeExceededException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        val servletWebRequest = request as ServletWebRequest

        val problemDetails = servletWebRequest.respondProblemDetails(
            status = HttpStatus.PAYLOAD_TOO_LARGE,
            title = "Størrelse på opplastet fil er for stor",
            type = URI("/problem-details/opplastet-fil-for-stor"),
            detail = "Størrelsen på opplastet fil er over tillatt grense på $maxFileSize",
        )
        log.debug("{}", problemDetails)
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
        violations: Set<Violation> = setOf(),
        detail: String,
    ): ProblemDetail {
        val problemDetail = ValidationProblemDetails(violations)
        problemDetail.status = status.value()
        problemDetail.title = title
        problemDetail.type = type
        problemDetail.detail = detail
        problemDetail.instance = URI(URLDecoder.decode(request.requestURL.toString(), Charset.defaultCharset()))

        return problemDetail
    }
}
