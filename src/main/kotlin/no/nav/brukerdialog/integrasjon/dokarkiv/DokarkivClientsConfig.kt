package no.nav.brukerdialog.integrasjon.dokarkiv

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
class DokarkivClientsConfig(
    oauth2Config: ClientConfigurationProperties,
    @Value("\${no.nav.integration.dokarkiv-base-url}") private val baseUrl: String,
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(DokarkivClientsConfig::class.java)
    }

    private val azureDokarkivClientProperties = oauth2Config.registration["azure-dokarkiv"]
        ?: throw RuntimeException("could not find oauth2 client config for azure-dokarkiv")

    @Bean
    fun dokarkivRestTemplate(
        restTemplateBuilder: RestTemplateBuilder,
        clientConfigurationProperties: ClientConfigurationProperties,
        oAuth2AccessTokenService: OAuth2AccessTokenService,
    ): RestTemplate {
        logger.info("Konfigurerer opp azure klient for dokarkiv.")
        return restTemplateBuilder
            .rootUri(baseUrl)
            .readTimeout(Duration.ofSeconds(120))
            .connectTimeout(Duration.ofSeconds(120))
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .additionalInterceptors(
                RestTemplateUtils.exchangeBearerTokenInterceptor(
                    azureDokarkivClientProperties,
                    oAuth2AccessTokenService
                ),
                RestTemplateUtils.requestLoggerInterceptor(logger),
                RestTemplateUtils.requestTracingInterceptor()
            )
            .build()
    }
}
