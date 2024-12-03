package no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag

import no.nav.brukerdialog.utils.RestTemplateUtils
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpRequest
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate
import java.time.Duration

@Configuration
class OppslagsKlientKonfig(
    @Value("\${no.nav.integration.k9-selvbetjening-oppslag-base-url}") private val oppslagsUrl: String,
    oauth2Config: ClientConfigurationProperties,
    private val oAuth2AccessTokenService: OAuth2AccessTokenService,
) {

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(OppslagsKlientKonfig::class.java)

        const val TOKEN_X_K9_SELVBETJENING_OPPSLAG = "tokenx-k9-selvbetjening-oppslag"
    }

    private val tokenxK9SelvbetjeningOppslagClientProperties =
        oauth2Config.registration[TOKEN_X_K9_SELVBETJENING_OPPSLAG]
            ?: throw RuntimeException("could not find oauth2 client config for $TOKEN_X_K9_SELVBETJENING_OPPSLAG")

    @Bean(name = ["k9OppslagsKlient"])
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate {
        return builder
            .connectTimeout(Duration.ofSeconds(20))
            .readTimeout(Duration.ofSeconds(20))
            .defaultHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .rootUri(oppslagsUrl)
            .defaultMessageConverters()
            .additionalInterceptors(
                bearerTokenInterceptor(),
                RestTemplateUtils.requestLoggerInterceptor(logger),
                RestTemplateUtils.requestTracingInterceptor()
            )
            .build()
    }

    private fun bearerTokenInterceptor(): ClientHttpRequestInterceptor {
        return ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution ->
            when {
                request.uri.path == "/isalive" -> {} // ignorer

                else -> {
                    oAuth2AccessTokenService.getAccessToken(tokenxK9SelvbetjeningOppslagClientProperties).access_token?.let {
                        request.headers.setBearerAuth(it)
                    } ?: throw SecurityException("Accesstoken er null")
                }
            }
            execution.execute(request, body)
        }
    }

}

