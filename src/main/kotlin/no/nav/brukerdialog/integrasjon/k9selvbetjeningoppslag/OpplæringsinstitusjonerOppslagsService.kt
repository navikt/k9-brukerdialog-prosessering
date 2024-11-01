package no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag

import no.nav.brukerdialog.oppslag.opplaeringsintitusjoner.Opplæringsinstitusjon
import no.nav.brukerdialog.ytelse.Ytelse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
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
class OpplæringsinstitusjonerOppslagsService(
    @Qualifier("k9OppslagsKlient")
    private val k9OppslagsKlient: RestTemplate,
) {
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(OpplæringsinstitusjonerOppslagsService::class.java)
        val url = UriComponentsBuilder
            .fromUriString("/opplæringsinsititusjon/alle")
            .build()
    }

    fun hentOpplæringsinstitusjoner(): List<Opplæringsinstitusjon> {
        logger.info("Slår opp opplæringsinstitusjoner…")
        val exchange = k9OppslagsKlient.exchange(
            url.toUriString(),
            HttpMethod.GET,
            HttpEntity(null, Ytelse.OPPLARINGSPENGER.somHttpHeader()),
            object : ParameterizedTypeReference<List<Opplæringsinstitusjon>>() {}
        )
        logger.info("Fikk response {} fra opplæringsinstitusjoner.", exchange.statusCode)

        return exchange.body ?: listOf()
    }

    @Recover
    private fun recoverOpplæringsinstitusjoner(error: HttpServerErrorException): List<Opplæringsinstitusjon> {
        logger.error("Error response = '${error.responseBodyAsString}' fra '${url.toUriString()}'")
        throw IllegalStateException("Feil ved henting av opplæringsinstitusjoner")
    }

    @Recover
    private fun recoverOpplæringsinstitusjoner(error: HttpClientErrorException): List<Opplæringsinstitusjon> {
        logger.error("Error response = '${error.responseBodyAsString}' fra '${url.toUriString()}'")
        throw IllegalStateException("Feil ved henting av opplæringsinstitusjoner")
    }

    @Recover
    private fun recoverOpplæringsinstitusjoner(error: ResourceAccessException): List<Opplæringsinstitusjon> {
        logger.error("{}", error.message)
        throw IllegalStateException("Timeout ved henting av opplæringsinstitusjoner")
    }
}

