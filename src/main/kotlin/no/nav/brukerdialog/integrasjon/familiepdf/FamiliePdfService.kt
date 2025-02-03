package no.nav.brukerdialog.integrasjon.familiepdf

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.brukerdialog.integrasjon.familiepdf.dto.FamiliePdfPostResponse
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
        val familiePdfUrl =
            UriComponentsBuilder
                .fromPath("/api/v1/pdf/opprett-pdf")
                .build()
                .toUri()
    }

    suspend fun lagPdfKvittering(request: Any): Any =
        kotlin
            .runCatching {
                familiePdfRestTemplate.exchange(
                    familiePdfUrl.path,
                    HttpMethod.POST,
                    HttpEntity(request),
                    FamiliePdfPostResponse::class.java,
                )
            }.fold(
                onSuccess = { response: ResponseEntity<FamiliePdfPostResponse> ->
                    println(response)
                },
                { error: Throwable ->
                    when (error) {
                        is HttpClientErrorException.Conflict -> {
                            logger.info("Noe gikk galt")
                        }

                        else -> {
                            logger.error("Noe gikk galt")
                        }
                    }
                    throw error
                },
            )
}
