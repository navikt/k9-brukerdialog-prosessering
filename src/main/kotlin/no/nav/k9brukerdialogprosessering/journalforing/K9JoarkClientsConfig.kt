package no.nav.k9brukerdialogprosessering.journalforing

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
import java.util.*

@Configuration
class K9JoarkClientsConfig(
    oauth2Config: ClientConfigurationProperties,
    @Value("\${no.nav.integration.k9-joark-base-url}") private val baseUrl: String,
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(K9JoarkClientsConfig::class.java)
    }

    private val azureK9JoarkClientProperties = oauth2Config.registration["azure-k9-joark"]
        ?: throw RuntimeException("could not find oauth2 client config for azure-k9-joark")

    @Bean
    fun k9JoarkRestTemplate(
        restTemplateBuilder: RestTemplateBuilder,
        clientConfigurationProperties: ClientConfigurationProperties,
        oAuth2AccessTokenService: OAuth2AccessTokenService
    ): RestTemplate {
        logger.info("Konfigurerer opp azure klient for k9-joark.")
        return restTemplateBuilder
            .rootUri(baseUrl)
            .setReadTimeout(Duration.ofSeconds(120))
            .additionalInterceptors(
                RestTemplateUtils.authorizationInterceptor(azureK9JoarkClientProperties, oAuth2AccessTokenService),
                RestTemplateUtils.requestLoggerInterceptor(logger),
                RestTemplateUtils.requestTracingInterceptor()
            )
            .build()
    }
}
