package no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser

import no.nav.brukerdialog.integrasjon.k9sakinnsynapi.K9SakInnsynApiClientConfig
import no.nav.brukerdialog.utils.RestTemplateUtils
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.spring.validation.interceptor.BearerTokenClientHttpRequestInterceptor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import java.time.Duration

@Configuration
class UngDeltakelseOpplyserClientConfig(
    @Value("\${no.nav.integration.ung-deltakelse-opplyser-base-url}") private val ungDeltakelseOpplyserBaseUrl: String,
    oauth2Config: ClientConfigurationProperties,
    private val oAuth2AccessTokenService: OAuth2AccessTokenService,
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(K9SakInnsynApiClientConfig::class.java)
        const val TOKEN_X_UNG_DELTAKELSE_OPPLYSER = "tokenx-ung-deltakelse-opplyser"
    }

    private val tokenxUngDeltakelseOpplyserClientProperties =
        oauth2Config.registration[TOKEN_X_UNG_DELTAKELSE_OPPLYSER]
            ?: throw RuntimeException("could not find oauth2 client config for $TOKEN_X_UNG_DELTAKELSE_OPPLYSER")

    @Bean(name = ["ungDeltakelseOpplyserClient"])
    fun restTemplate(
        builder: RestTemplateBuilder,
        bearerTokenClientHttpRequestInterceptor: BearerTokenClientHttpRequestInterceptor
    ): RestTemplate {
        return builder
            .connectTimeout(Duration.ofSeconds(20))
            .readTimeout(Duration.ofSeconds(20))
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .rootUri(ungDeltakelseOpplyserBaseUrl)
            .defaultMessageConverters()
            .additionalInterceptors(
                RestTemplateUtils.exchangeBearerTokenInterceptor(tokenxUngDeltakelseOpplyserClientProperties, oAuth2AccessTokenService),
                RestTemplateUtils.requestLoggerInterceptor(logger),
                RestTemplateUtils.requestTracingInterceptor()
            )
            .build()
    }
}
