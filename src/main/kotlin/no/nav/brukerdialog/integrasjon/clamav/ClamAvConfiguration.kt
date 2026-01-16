package no.nav.brukerdialog.integrasjon.clamav

import no.nav.brukerdialog.utils.RestTemplateUtils
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.restclient.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import java.time.Duration

@Configuration
class ClamAvConfiguration(
    @Value("\${no.nav.integration.clam-av-base-url}") val baseUrl: String,
) {
    private companion object {
        private val logger = LoggerFactory.getLogger(ClamAvConfiguration::class.java)
    }

    @Bean
    fun clamAvRestTemplate(
        restTemplateBuilder: RestTemplateBuilder,
        clientConfigurationProperties: ClientConfigurationProperties,
        oAuth2AccessTokenService: OAuth2AccessTokenService,
    ): RestTemplate {
        return restTemplateBuilder
            .rootUri(baseUrl)
            .readTimeout(Duration.ofSeconds(120))
            .connectTimeout(Duration.ofSeconds(120))
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
            .additionalInterceptors(
                RestTemplateUtils.requestLoggerInterceptor(logger),
                RestTemplateUtils.requestTracingInterceptor()
            )
            .build()
    }
}
