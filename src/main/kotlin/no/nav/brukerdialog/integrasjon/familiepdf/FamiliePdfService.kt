package no.nav.brukerdialog.integrasjon.familiepdf

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.brukerdialog.integrasjon.familiepdf.dto.FamiliePdfPostRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
class FamiliePdfService(
    private val familiePdfRestTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
) {
    private companion object {
        private val logger = LoggerFactory.getLogger(FamiliePdfService::class.java)
        val familiePdfUri =
            UriComponentsBuilder
                .fromPath("/api/v1/pdf/opprett-pdf")
                .build()
                .toUri()
    }

    suspend fun lagPdfKvittering(request: FamiliePdfPostRequest): ByteArray =
        kotlin
            .runCatching {
                familiePdfRestTemplate.exchange(
                    familiePdfUri.path,
                    HttpMethod.POST,
                    HttpEntity(request),
                    ByteArray::class.java,
                )
            }.fold(
                onSuccess = { response: ResponseEntity<ByteArray> ->
                    println(response)
                    return response.body ?: throw IllegalStateException("Response body er null")
                },
                onFailure = { error: Throwable ->
                    when (error) {
                        is HttpClientErrorException.Conflict -> {
                            logger.info("Konfliktfeil: ${error.responseBodyAsString}")
                            return@fold objectMapper.readValue(
                                error.responseBodyAsString,
                                ByteArray::class.java
                            )
                        }

                        is HttpClientErrorException.BadRequest -> {
                            logger.warn("Feil i forespørsel: ${error.responseBodyAsString}")
                        }

                        is HttpClientErrorException.Unauthorized -> {
                            logger.warn("Uautorisert feil: ${error.responseBodyAsString}")
                        }

                        is HttpClientErrorException.Forbidden -> {
                            logger.warn("Forbudt feil: ${error.responseBodyAsString}")
                        }

                        is HttpClientErrorException.NotFound -> {
                            logger.warn("Ikke funnet feil: ${error.responseBodyAsString}")
                        }

                        else -> {
                            logger.error("Uventet feil: ${error.message}", error)
                        }
                    }
                    throw error
                },
            )
}
