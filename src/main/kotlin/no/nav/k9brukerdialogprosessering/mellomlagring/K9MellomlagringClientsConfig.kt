package no.nav.k9brukerdialogprosessering.mellomlagring

import no.nav.k9brukerdialogprosessering.common.Constants.CORRELATION_ID_KEY
import no.nav.k9brukerdialogprosessering.common.Constants.NAV_CALL_ID
import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate
import java.util.*

@Configuration
class K9MellomlagringClientsConfig(
    oauth2Config: ClientConfigurationProperties,
    private val oAuth2AccessTokenService: OAuth2AccessTokenService,
    @Value("\${no.nav.integration.k9-mellomlagring-base-url}") private val baseUrl: String
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(K9MellomlagringClientsConfig::class.java)
    }

    private val azureK9MellomlagringClientProperties = oauth2Config.registration["azure-k9-mellomlagring"]
        ?: throw RuntimeException("could not find oauth2 client config for azure-k9-mellomlagring")

    @Bean
    fun k9MellomlagringRestTemplate(
        restTemplateBuilder: RestTemplateBuilder,
        clientConfigurationProperties: ClientConfigurationProperties,
        oAuth2AccessTokenService: OAuth2AccessTokenService
    ): RestTemplate {
        val correlationID = MDC.get(CORRELATION_ID_KEY) ?: UUID.randomUUID().toString()
        logger.info("Konfigurerer opp azure klient for k9-mellomlagring.")
        return restTemplateBuilder
            .rootUri(baseUrl)
            .defaultHeader(NAV_CALL_ID, correlationID)
            .defaultHeader(CORRELATION_ID_KEY, correlationID)
            .additionalInterceptors(authorizationInterceptor(), requestLoggerInterceptor())
            .build()
    }

    private fun authorizationInterceptor(): ClientHttpRequestInterceptor {
        return ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution ->
            request.headers[AUTHORIZATION] = "Bearer ${accessToken(azureK9MellomlagringClientProperties)}"
            execution.execute(request, body)
        }
    }

    private fun requestLoggerInterceptor() =
        ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution ->
            logger.info("{} {}", request.method, request.uri)
            execution.execute(request, body)
        }

    private fun accessToken(clientProperties: ClientProperties): String {
        return oAuth2AccessTokenService.getAccessToken(clientProperties).accessToken
    }
}
