package no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser


import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.OppgaveDTO
import no.nav.ung.deltakelseopplyser.kontrakt.register.DeltakelseOpplysningDTO
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
        private val hentOppgaveUrl = UriComponentsBuilder
            .fromUriString("/deltakelse/register/oppgave/{oppgaveReferanse}")
            .build()
            .toUriString()

        private val markerDeltakelseSomSøktUrl = UriComponentsBuilder
            .fromUriString("/deltakelse/register/{id}/marker-har-sokt")
            .build()
            .toUriString()

        private val oppgaveDataFeil = IllegalStateException("Feilet med henting av oppgave.")
        private val markerDeltakelseSøktFeil = IllegalStateException("Feilet med å markere deltakelse som søkt.")
    }

    fun hentOppgaveForDeltakelse(oppgaveReferanse: UUID): OppgaveDTO {
        val exchange = ungDeltakelseOpplyserClient.exchange(
            hentOppgaveUrl,
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<OppgaveDTO>() {},
            oppgaveReferanse
        )
        logger.info("Fikk response {} for henting av oppgave", exchange.statusCode)

        return if (exchange.statusCode.is2xxSuccessful) {
            exchange.body!!
        } else {
            logger.error(
                "Henting av oppgave feilet med status: {}, respons: {}",
                exchange.statusCode,
                exchange.body
            )
            throw oppgaveDataFeil
        }
    }

    @Recover
    private fun recover(error: HttpServerErrorException): OppgaveDTO {
        logger.error("Error response = '{}' fra '{}'", error.responseBodyAsString, hentOppgaveUrl)
        throw oppgaveDataFeil
    }

    @Recover
    private fun recover(error: HttpClientErrorException): OppgaveDTO {
        logger.error("Error response = '{}' fra '{}'", error.responseBodyAsString, hentOppgaveUrl)
        throw oppgaveDataFeil
    }

    @Recover
    private fun recover(error: ResourceAccessException): OppgaveDTO {
        logger.error("{}", error.message)
        throw oppgaveDataFeil
    }

    fun markerDeltakelseSomSøkt(deltakelseId: UUID): DeltakelseOpplysningDTO {
        logger.info("Markerer deltakelse som søkt.")
        val response = ungDeltakelseOpplyserClient.exchange(
            markerDeltakelseSomSøktUrl,
            HttpMethod.PUT,
            null,
            object : ParameterizedTypeReference<DeltakelseOpplysningDTO>() {},
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
    private fun recoverMarkerDeltakelseSomSøkt(error: HttpServerErrorException): DeltakelseOpplysningDTO {
        logger.error("Error response = '{}' fra '{}'", error.responseBodyAsString, markerDeltakelseSomSøktUrl)
        throw markerDeltakelseSøktFeil
    }

    @Recover
    private fun recoverMarkerDeltakelseSomSøkt(error: HttpClientErrorException): DeltakelseOpplysningDTO {
        logger.error("Error response = '{}' fra '{}'", error.responseBodyAsString, markerDeltakelseSomSøktUrl)
        throw markerDeltakelseSøktFeil
    }

    @Recover
    private fun recoverMarkerDeltakelseSomSøkt(error: ResourceAccessException): DeltakelseOpplysningDTO {
        logger.error("{}", error.message)
        throw markerDeltakelseSøktFeil
    }
}
