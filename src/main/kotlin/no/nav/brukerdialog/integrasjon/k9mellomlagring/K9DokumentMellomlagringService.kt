package no.nav.brukerdialog.integrasjon.k9mellomlagring

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import no.nav.brukerdialog.mellomlagring.dokument.Dokument
import no.nav.brukerdialog.mellomlagring.dokument.DokumentEier
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.net.URL

@Service
class K9DokumentMellomlagringService(
    private val k9MellomlagringRestTemplate: RestTemplate,
) {
    private companion object {
        private val logger = LoggerFactory.getLogger(K9DokumentMellomlagringService::class.java)
        val dokumentUrl = UriComponentsBuilder
            .fromPath("/v1/dokument")
            .build()
            .toUri()
    }

    internal suspend fun hentDokument(dokumentId: String, dokumentEier: DokumentEier): Dokument {
        val headers = HttpHeaders(
            LinkedMultiValueMap(
                mapOf(HttpHeaders.ACCEPT to listOf(MediaType.APPLICATION_JSON_VALUE))
            )
        )
        return kotlin.runCatching {
            k9MellomlagringRestTemplate
                .postForEntity(
                    "${dokumentUrl.path}/$dokumentId",
                    HttpEntity(dokumentEier, headers),
                    Dokument::class.java
                ).body!!
        }
            .fold(
                onSuccess = { dokument: Dokument -> dokument },
                onFailure = { error: Throwable ->
                    if (error is RestClientException) {
                        logger.error("Feil ved henting av dokument. Feilmelding: ${error.message}")
                    }
                    throw error
                }
            )
    }

    internal suspend fun hentDokumenter(dokumentIder: List<URL>, dokumentEier: DokumentEier): List<Dokument> {
        return coroutineScope {
            val deferred = dokumentIder.map { dokumentId: URL ->
                async {
                    hentDokument(dokumentId.toURI().dokumentId(), dokumentEier)
                }
            }
            deferred.awaitAll()
        }
    }

    internal suspend fun hentDokumenterMedId(dokumentIder: List<String>, dokumentEier: DokumentEier): List<Dokument> {
        return coroutineScope {
            val deferred = dokumentIder.map { dokumentId: String ->
                async {
                    hentDokument(dokumentId, dokumentEier)
                }
            }
            deferred.awaitAll()
        }
    }

    internal suspend fun hentDokumenterMedString(dokumentIder: List<String>, dokumentEier: DokumentEier): List<Dokument> {
        val uris: List<URL> = dokumentIder.map { URI.create(it).toURL() }
        return hentDokumenter(uris, dokumentEier)
    }

    internal suspend fun lagreDokument(dokument: Dokument): URI {
        return kotlin.runCatching {
            k9MellomlagringRestTemplate.postForLocation(dokumentUrl.path, HttpEntity(dokument))
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
                    slettDokument(dokumentId, dokumentEier)
                }
            }
            deferred.awaitAll()
        }
    }

    internal suspend fun slettDokument(
        dokumentId: String,
        dokumentEier: DokumentEier,
    ) {
        val slettDokumentUrl: URI = UriComponentsBuilder.fromUri(dokumentUrl)
            .path("/$dokumentId")
            .build()
            .toUri()

        kotlin.runCatching {
            k9MellomlagringRestTemplate.exchange(
                slettDokumentUrl.path,
                HttpMethod.DELETE,
                HttpEntity(dokumentEier),
                Unit::class.java
            )
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

    internal suspend fun persisterVedlegg(
        dokumentId: String,
        eier: DokumentEier,
    ) {
        val persisterVedleggUrl: URI = UriComponentsBuilder.fromUri(dokumentUrl)
            .path("/$dokumentId/persister")
            .build()
            .toUri()

        kotlin.runCatching {
            val requestEntity = RequestEntity
                .put(persisterVedleggUrl.path)
                .body(eier)

            k9MellomlagringRestTemplate.exchange(requestEntity, Unit::class.java)
        }
            .fold(
                { _ -> logger.info("Vellykket persistering av vedlegg") },
                { error: Throwable ->
                    if (error is RestClientException) {
                        logger.error("Feil ved persistering av dokument med id: $dokumentId. Feilmelding: ${error.message}")
                    }
                    throw RuntimeException("Feil ved persistering av dokument med id: $dokumentId", error)
                }
            )
    }

    internal suspend fun persisterDokumenter(
        dokumentIder: List<URL>,
        eier: DokumentEier,
    ) {
        coroutineScope {
            val deferred = dokumentIder.map { dokumentId ->
                async {
                    persisterVedlegg(dokumentId.toURI().dokumentId(), eier)
                }
            }
            deferred.awaitAll()
        }
    }

    internal suspend fun fjernHoldPåPersisterteDokumenter(
        dokumentId: String,
        dokumentEier: DokumentEier,
    ) {

        val fjernHoldPåPersisterteDokumentUrl: URI = UriComponentsBuilder.fromUri(dokumentUrl)
            .path("/persistert/$dokumentId")
            .build()
            .toUri()

        kotlin.runCatching {
            val requestEntity = RequestEntity
                .put(fjernHoldPåPersisterteDokumentUrl.path)
                .body(dokumentEier)

            k9MellomlagringRestTemplate.exchange(
                requestEntity,
                Unit::class.java
            )

        }.fold(
            onSuccess = { logger.info("Fjernet hold på dokument med id: $dokumentId") },
            onFailure = { error: Throwable ->
                if (error is RestClientException) {
                    logger.error("Feil ved fjerning av hold på dokument med id: $dokumentId. Feilmelding: ${error.message}")
                }
                throw RuntimeException("Feil ved fjerning av hold på dokument med id: $dokumentId", error)
            }
        )
    }

    internal suspend fun fjernHoldPåPersisterteDokumenter(
        dokumentIder: List<URL>,
        dokumentEier: DokumentEier,
    ) {
        coroutineScope {
            val deferred = dokumentIder.map { dokumentId ->
                async {
                    fjernHoldPåPersisterteDokumenter(dokumentId.toURI().dokumentId(), dokumentEier)
                }
            }
            deferred.awaitAll()
        }
    }
}

fun URI.dokumentId() = this.toString().substringAfterLast("/")
