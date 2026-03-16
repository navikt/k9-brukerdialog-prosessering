package no.nav.brukerdialog.integrasjon.ungbrukerdialogapi


import no.nav.ung.brukerdialog.kontrakt.oppgaver.LøsOppgaveRequest
import no.nav.ung.deltakelseopplyser.kontrakt.oppgave.felles.OppgaveDTO
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
class UngBrukerdialogApiService(
    @Qualifier("ungBrukerdialogApiClient")
    private val ungBrukerdialogApiClient: RestTemplate,
) {
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(UngBrukerdialogApiService::class.java)

        private val markerOppgaveSomLøstUrl = UriComponentsBuilder
            .fromUriString("/ung/brukerdialog/ekstern/api/oppgave/{oppgaveReferanse}/løs")
            .build()
            .toUriString()

        private val markerOppgaveSomLøstFeil = IllegalStateException("Feilet med å markere oppgave som løst.")
    }

    fun markerOppgaveSomLøst(oppgaveReferanse: UUID, løsOppgaveRequest: LøsOppgaveRequest): OppgaveDTO {
        logger.info("Markerer oppgave med id=$oppgaveReferanse som løst.")
        val response = ungBrukerdialogApiClient.exchange(
            markerOppgaveSomLøstUrl,
            HttpMethod.POST,
            HttpEntity(løsOppgaveRequest),
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
