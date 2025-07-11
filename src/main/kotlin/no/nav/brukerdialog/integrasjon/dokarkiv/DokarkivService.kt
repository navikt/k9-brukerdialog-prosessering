package no.nav.brukerdialog.integrasjon.dokarkiv

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.brukerdialog.integrasjon.dokarkiv.dto.DokarkivJournalpostRequest
import no.nav.brukerdialog.integrasjon.dokarkiv.dto.DokarkivJournalpostResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
class DokarkivService(
    private val dokarkivRestTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
) {

    private companion object {
        private val logger = LoggerFactory.getLogger(DokarkivService::class.java)
        val journalpostUrl = UriComponentsBuilder
            .fromPath("/rest/journalpostapi/v1/journalpost")
            .build()
            .toUri()
    }

    suspend fun journalfør(
        request: DokarkivJournalpostRequest,
    ): DokarkivJournalpostResponse = kotlin.runCatching {
        dokarkivRestTemplate.exchange(
            journalpostUrl.path,
            HttpMethod.POST,
            HttpEntity(request),
            DokarkivJournalpostResponse::class.java
        )
    }.fold(
        onSuccess = { response: ResponseEntity<DokarkivJournalpostResponse> ->
            val journalføringsResponse = response.body!!
            logger.info("Dokumenter journalført på journalpost med id: ${journalføringsResponse.journalpostId}")
            journalføringsResponse
        },
        { error: Throwable ->
            when (error) {
                is HttpClientErrorException.Conflict -> {
                    logger.info("Journalpost finnes fra før.")
                    return@fold objectMapper.readValue(
                        error.responseBodyAsString,
                        DokarkivJournalpostResponse::class.java
                    )
                }

                is HttpStatusCodeException -> {
                    logger.error(
                        "Feil ved journalføring. Error response: ${error.responseBodyAsString} fra '${journalpostUrl.path}'",
                        error
                    )
                }

                else -> {
                    logger.error("Feil ved journalføring. Feilmelding: ${error.message}")
                }
            }
            throw error
        }
    )
}
