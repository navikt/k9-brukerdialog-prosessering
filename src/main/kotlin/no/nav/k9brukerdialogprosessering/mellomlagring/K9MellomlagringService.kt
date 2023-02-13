package no.nav.k9brukerdialogprosessering.mellomlagring

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import no.nav.k9brukerdialogprosessering.utils.RetryContextUtils.logHttpRetries
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.retry.RetryContext
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class K9MellomlagringService(
    private val k9MellomlagringRestTemplate: RestTemplate,
    private val retryTemplate: RetryTemplate,
    @Value("\${no.nav.integration.k9-mellomlagring-base-url}") private val baseUrl: String,
) {
    private companion object {
        private val logger = LoggerFactory.getLogger(K9MellomlagringService::class.java)
        val dokumentUrl = UriComponentsBuilder
            .fromPath("/v1/dokument")
            .build()
            .toUri()
    }

    internal suspend fun lagreDokument(dokument: Dokument): URI {
        return kotlin.runCatching {
            retryTemplate.execute<URI, Throwable> { context: RetryContext ->
                context.logHttpRetries(logger, "$baseUrl$dokumentUrl.path")
                k9MellomlagringRestTemplate.postForLocation(dokumentUrl.path, HttpEntity(dokument))
            }
        }
            .fold(
                onSuccess = { dokumentIdUrl: URI? -> dokumentIdUrl!! },
                onFailure = { error: Throwable ->
                    if (error is RestClientException) {
                        logger.error("Feil ved lagring av dokument. Feilmelding: ${error.message}")
                    }
                    throw error
                }
            )
    }

    internal suspend fun slettDokumenter(
        dokumentIder: List<String>,
        dokumentEier: DokumentEier,
    ) {
        coroutineScope {
            val deferred = dokumentIder.map { dokumentId ->
                async {
                    val slettDokumentUrl: URI = UriComponentsBuilder.fromUri(dokumentUrl)
                        .path("/$dokumentId")
                        .build()
                        .toUri()

                    kotlin.runCatching {
                        retryTemplate.execute<Unit, Throwable> { context ->
                            context.logHttpRetries(logger, "$baseUrl$slettDokumentUrl.path")
                            k9MellomlagringRestTemplate.exchange(
                                slettDokumentUrl.path,
                                HttpMethod.DELETE,
                                HttpEntity(dokumentEier),
                                Unit::class.java
                            )
                        }
                    }.fold(
                        onSuccess = { logger.info("Slettet dokument med id: $dokumentId") },
                        onFailure = { error: Throwable ->
                            if (error is RestClientException) {
                                logger.error("Feil ved sletting av dokument med id: $dokumentId. Feilmelding: ${error.message}")
                            }
                            throw RuntimeException("Feil ved sletting av dokument med id: $dokumentId", error)
                        }
                    )
                }
            }
            deferred.awaitAll()
        }
    }

}

fun URI.dokumentId() = this.toString().substringAfterLast("/")
