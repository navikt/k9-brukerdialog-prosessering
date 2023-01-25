package no.nav.k9brukerdialogprosessering.utils

import no.nav.k9brukerdialogprosessering.common.Constants.CORRELATION_ID_KEY
import no.nav.k9brukerdialogprosessering.common.Constants.NAV_CALL_ID
import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import org.slf4j.Logger
import org.slf4j.MDC
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import java.util.*

object RestTemplateUtils {
    fun authorizationInterceptor(
        clientProperties: ClientProperties,
        oAuth2AccessTokenService: OAuth2AccessTokenService,
    ): ClientHttpRequestInterceptor {
        return ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution ->
            request.headers[HttpHeaders.AUTHORIZATION] =
                "Bearer ${accessToken(clientProperties, oAuth2AccessTokenService)}"
            execution.execute(request, body)
        }
    }

    fun requestLoggerInterceptor(logger: Logger) =
        ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution ->
            logger.info("HTTP Request: {} {}", request.method, request.uri)
            val response = execution.execute(request, body)
            logger.info("HTTP Response: {} {} {}", response.statusCode, request.method, request.uri)
            response
        }

    fun requestTracingInterceptor() =
        ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution ->
            val correlationId = MDC.get(CORRELATION_ID_KEY) ?: UUID.randomUUID().toString()
            request.headers[NAV_CALL_ID] = correlationId
            request.headers[CORRELATION_ID_KEY] = correlationId
            execution.execute(request, body)
        }

    fun accessToken(clientProperties: ClientProperties, oAuth2AccessTokenService: OAuth2AccessTokenService): String {
        return oAuth2AccessTokenService.getAccessToken(clientProperties).accessToken
    }
}
