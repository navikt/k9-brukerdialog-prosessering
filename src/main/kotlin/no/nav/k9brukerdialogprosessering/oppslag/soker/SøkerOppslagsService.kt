package no.nav.k9brukerdialogprosessering.oppslag.soker

import no.nav.k9brukerdialogprosessering.api.ytelse.Ytelse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
@Retryable(
    exclude = [HttpClientErrorException.Unauthorized::class, HttpClientErrorException.Forbidden::class, ResourceAccessException::class],
    backoff = Backoff(
        delayExpression = "\${spring.rest.retry.initialDelay}",
        multiplierExpression = "\${spring.rest.retry.multiplier}",
        maxDelayExpression = "\${spring.rest.retry.maxDelay}"
    ),
    maxAttemptsExpression = "\${spring.rest.retry.maxAttempts}"
)
class SøkerOppslagsService(
    @Qualifier("k9OppslagsKlient")
    private val k9OppslagsKlient: RestTemplate
) {
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(SøkerOppslagsService::class.java)

        val søkerUrl = UriComponentsBuilder
            .fromUriString("/meg")
            .queryParam(
                "a",
                "aktør_id",
                "fornavn",
                "mellomnavn",
                "etternavn",
                "fødselsdato"
            )
            .build()
    }

    fun hentSøker(ytelse: Ytelse): SøkerOppslagRespons {
        logger.info("Slår opp søker...")

        val exchange = k9OppslagsKlient.exchange(
            søkerUrl.toUriString(),
            HttpMethod.GET,
            HttpEntity(null, ytelse.somHttpHeader()),
            SøkerOppslagRespons::class.java)
        logger.info("Fikk response {} for oppslag av søker.", exchange.statusCode)

        return exchange.body!!
    }

    @Recover
    private fun recover(error: HttpServerErrorException): SøkerOppslagRespons {
        logger.error("Error response = '${error.responseBodyAsString}' fra '${søkerUrl.toUriString()}'")
        throw IllegalStateException("Feil ved henting av søkers personinformasjon")
    }

    @Recover
    private fun recover(error: HttpClientErrorException): SøkerOppslagRespons {
        logger.error("Error response = '${error.responseBodyAsString}' fra '${søkerUrl.toUriString()}'")
        throw IllegalStateException("Feil ved henting av søkers personinformasjon")
    }

    @Recover
    private fun recover(error: ResourceAccessException): SøkerOppslagRespons {
        logger.error("{}", error.message)
        throw IllegalStateException("Timeout ved henting av søkers personinformasjon")
    }
}
