package no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser


import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.OppgaveDTO
import no.nav.ung.deltakelseopplyser.kontrakt.register.DeltakelseKomposittDTO
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

        private val markerOppgaveSomLøstUrl = UriComponentsBuilder
            .fromUriString("/deltakelse/register/oppgave/{oppgaveReferanse}/løst")
            .build()
            .toUriString()

        private val oppgaveDataFeil = IllegalStateException("Feilet med henting av oppgave.")
        private val markerDeltakelseSøktFeil = IllegalStateException("Feilet med å markere deltakelse som søkt.")
        private val markerOppgaveSomLøstFeil = IllegalStateException("Feilet med å markere oppgave som løst.")
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

    fun markerDeltakelseSomSøkt(deltakelseId: UUID): DeltakelseKomposittDTO {
        logger.info("Markerer deltakelse som søkt.")
        val response = ungDeltakelseOpplyserClient.exchange(
            markerDeltakelseSomSøktUrl,
            HttpMethod.PUT,
            null,
            object : ParameterizedTypeReference<DeltakelseKomposittDTO>() {},
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
    private fun recoverMarkerDeltakelseSomSøkt(error: HttpServerErrorException): DeltakelseKomposittDTO {
        logger.error("Error response = '{}' fra '{}'", error.responseBodyAsString, markerDeltakelseSomSøktUrl)
        throw markerDeltakelseSøktFeil
    }

    @Recover
    private fun recoverMarkerDeltakelseSomSøkt(error: HttpClientErrorException): DeltakelseKomposittDTO {
        logger.error("Error response = '{}' fra '{}'", error.responseBodyAsString, markerDeltakelseSomSøktUrl)
        throw markerDeltakelseSøktFeil
    }

    @Recover
    private fun recoverMarkerDeltakelseSomSøkt(error: ResourceAccessException): DeltakelseKomposittDTO {
        logger.error("{}", error.message)
        throw markerDeltakelseSøktFeil
    }

    fun markerOppgaveSomLøst(oppgaveReferanse: UUID): OppgaveDTO {
        logger.info("Markerer oppgave med id=$oppgaveReferanse som løst.")
        val response = ungDeltakelseOpplyserClient.exchange(
            markerOppgaveSomLøstUrl,
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<OppgaveDTO>() {},
            oppgaveReferanse
        )

        return if (response.statusCode.is2xxSuccessful) {
            response.body!!
        } else {
            logger.error(
                "Feilet med å markere oppgave som løst: {}, respons: {}",
                response.statusCode,
                response.body
            )
            throw markerOppgaveSomLøstFeil
        }
    }

    @Recover
    private fun recoverMarkerOppgaveSomLøst(error: HttpServerErrorException): OppgaveDTO {
        logger.error("Error response = '{}' fra '{}'", error.responseBodyAsString, markerOppgaveSomLøstUrl)
        throw markerOppgaveSomLøstFeil
    }

    @Recover
    private fun recoverMarkerOppgaveSomLøst(error: HttpClientErrorException): OppgaveDTO {
        logger.error("Error response = '{}' fra '{}'", error.responseBodyAsString, markerOppgaveSomLøstUrl)
        throw markerOppgaveSomLøstFeil
    }

    @Recover
    private fun recoverMarkerOppgaveSomLøst(error: ResourceAccessException): OppgaveDTO {
        logger.error("{}", error.message)
        throw markerOppgaveSomLøstFeil
    }
}
