package no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser


import no.nav.ung.deltakelseopplyser.kontrakt.register.DeltakelseDTO
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
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
import java.util.*

@Service
@Retryable(
    exclude = [
        HttpClientErrorException.Unauthorized::class,
        HttpClientErrorException.Forbidden::class,
        ResourceAccessException::class
    ],
    backoff = Backoff(
        delayExpression = "\${spring.rest.retry.initialDelay}",
        multiplierExpression = "\${spring.rest.retry.multiplier}",
        maxDelayExpression = "\${spring.rest.retry.maxDelay}"
    ),
    maxAttemptsExpression = "\${spring.rest.retry.maxAttempts}"
)
class UngDeltakelseOpplyserService(
    @Qualifier("ungDeltakelseOpplyserClient")
    private val ungDeltakelseOpplyserClient: RestTemplate,
) {
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(UngDeltakelseOpplyserService::class.java)


        private val markerDeltakelseSomSøktUrl = UriComponentsBuilder
            .fromUriString("/deltakelse/register/{id}/marker-har-sokt")
            .build()
            .toUriString()

        private val markerDeltakelseSøktFeil = IllegalStateException("Feilet med å markere deltakelse som søkt.")
    }

    fun markerDeltakelseSomSøkt(deltakelseId: UUID): DeltakelseDTO {
        logger.info("Markerer deltakelse som søkt.")
        val response = ungDeltakelseOpplyserClient.exchange(
            markerDeltakelseSomSøktUrl,
            HttpMethod.PUT,
            null,
            object : ParameterizedTypeReference<DeltakelseDTO>() {},
            deltakelseId
        )

        return if (response.statusCode.is2xxSuccessful) {
            response.body!!
        } else {
            logger.error(
                "Feilet med å markere deltakelse som søkt: {}, respons: {}",
                response.statusCode,
                response.body
            )
            throw markerDeltakelseSøktFeil
        }
    }

    @Recover
    private fun recoverMarkerDeltakelseSomSøkt(error: HttpServerErrorException): DeltakelseDTO {
        logger.error("Error response = '{}' fra '{}'", error.responseBodyAsString, markerDeltakelseSomSøktUrl)
        throw markerDeltakelseSøktFeil
    }

    @Recover
    private fun recoverMarkerDeltakelseSomSøkt(error: HttpClientErrorException): DeltakelseDTO {
        logger.error("Error response = '{}' fra '{}'", error.responseBodyAsString, markerDeltakelseSomSøktUrl)
        throw markerDeltakelseSøktFeil
    }

    @Recover
    private fun recoverMarkerDeltakelseSomSøkt(error: ResourceAccessException): DeltakelseDTO {
        logger.error("{}", error.message)
        throw markerDeltakelseSøktFeil
    }

}
