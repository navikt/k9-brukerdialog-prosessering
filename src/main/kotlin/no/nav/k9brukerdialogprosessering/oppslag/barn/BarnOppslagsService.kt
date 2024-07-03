package no.nav.k9brukerdialogprosessering.oppslag.barn

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
class BarnOppslagsService(
    @Qualifier("k9OppslagsKlient")
    private val k9OppslagsKlient: RestTemplate,
) {
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(BarnOppslagsService::class.java)
        val barnUrl = UriComponentsBuilder
            .fromUriString("/meg")
            .queryParam(
                "a",
                "barn[].aktør_id",
                "barn[].fornavn",
                "barn[].mellomnavn",
                "barn[].etternavn",
                "barn[].fødselsdato",
                "barn[].identitetsnummer"
            )
            .build()
    }

    fun hentBarn(): List<BarnOppslagRespons> {
        val ytelse = Ytelse.fraMDC()
        logger.info("Slår opp barn...")
        val exchange = k9OppslagsKlient.exchange(
            barnUrl.toUriString(),
            HttpMethod.GET,
            HttpEntity(null, ytelse.somHttpHeader()),
            BarnOppslagResponsListe::class.java
        )
        logger.info("Fikk response {} fra oppslag av barn.", exchange.statusCode)

        return exchange.body?.barn ?: listOf()
    }

    @Recover
    private fun recoverBarn(error: HttpServerErrorException): List<BarnOppslagRespons> {
        logger.error("Error response = '${error.responseBodyAsString}' fra '${barnUrl.toUriString()}'")
        throw IllegalStateException("Feil ved henting av søkers barn")
    }

    @Recover
    private fun recoverBarn(error: HttpClientErrorException): List<BarnOppslagRespons> {
        logger.error("Error response = '${error.responseBodyAsString}' fra '${barnUrl.toUriString()}'")
        throw IllegalStateException("Feil ved henting av søkers barn")
    }

    @Recover
    private fun recoverBarn(error: ResourceAccessException): List<BarnOppslagRespons> {
        logger.error("{}", error.message)
        throw IllegalStateException("Timeout ved henting av søkers barn")
    }
}
