package no.nav.k9brukerdialogprosessering.mellomlagring.soknad

import no.nav.k9brukerdialogprosessering.utils.RestTemplateUtils
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import java.time.Duration

@Configuration
class K9BrukerdialogCacheClientsConfig(
    oauth2Config: ClientConfigurationProperties,
    @Value("\${no.nav.integration.k9-brukerdialog-cache-base-url}") private val baseUrl: String
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(K9BrukerdialogCacheClientsConfig::class.java)
    }

    private val tokenxK9MellomlagringClientProperties = oauth2Config.registration["tokenx-k9-brukerdialog-cache"]
        ?: throw RuntimeException("could not find oauth2 client config for tokenx-k9-brukerdialog-cache")

    @Bean
    fun k9BrukerdialogCacheRestTemplate(
        restTemplateBuilder: RestTemplateBuilder,
        clientConfigurationProperties: ClientConfigurationProperties,
        oAuth2AccessTokenService: OAuth2AccessTokenService
    ): RestTemplate {
        logger.info("Konfigurerer opp tokenx klient for k9-brukerdialog-cache.")
        return restTemplateBuilder
            .rootUri(baseUrl)
            .setReadTimeout(Duration.ofSeconds(20))
            .additionalInterceptors(
                RestTemplateUtils.authorizationInterceptor(tokenxK9MellomlagringClientProperties, oAuth2AccessTokenService),
                RestTemplateUtils.requestLoggerInterceptor(logger),
                RestTemplateUtils.requestTracingInterceptor()
            )
            .build()
    }
}
