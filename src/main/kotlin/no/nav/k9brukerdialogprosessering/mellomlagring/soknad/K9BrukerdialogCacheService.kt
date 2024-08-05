package no.nav.k9brukerdialogprosessering.mellomlagring.soknad

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder


@Service
class K9BrukerdialogCacheService(
    private val k9BrukerdialogCacheRestTemplate: RestTemplate,
) {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(K9BrukerdialogCacheService::class.java)
        val cacheUrl = UriComponentsBuilder
            .fromPath("/api/cache")
            .build()
            .toUri()
    }

    fun mellomlagreSøknad(cacheRequest: CacheRequest): CacheResponse {
        return kotlin.runCatching {
            k9BrukerdialogCacheRestTemplate.postForEntity(
                cacheUrl.path,
                HttpEntity(cacheRequest),
                CacheResponse::class.java
            ).body!!
        }.fold(
            onSuccess = { cacheResponse: CacheResponse -> cacheResponse },
            onFailure = { error: Throwable ->
                if (error is RestClientException) {
                    logger.error("Feil ved mellomlagring av søknad. Feilmelding: ${error.message}")
                }
                throw RuntimeException("Feil ved mellomlagring av søknad.", error)
            }
        )
    }

    fun hentMellomlagretSøknad(nøkkelPrefiks: String): CacheResponse? {
        return kotlin.runCatching {
            k9BrukerdialogCacheRestTemplate.getForEntity(
                "${cacheUrl.path}/$nøkkelPrefiks",
                CacheResponse::class.java
            ).body
        }.fold(
            onSuccess = { cacheResponse: CacheResponse? -> cacheResponse },
            onFailure = { error: Throwable ->
                if (error is RestClientException) {
                    if (error is HttpClientErrorException.NotFound) {
                        return null
                    }
                    logger.error("Feil ved henting av mellomlagret søknad. Feilmelding: ${error.message}")
                }
                throw RuntimeException("Feil ved henting av mellomlagret søknad.", error)
            }
        )
    }

    fun oppdaterMellomlagretSøknad(cacheRequest: CacheRequest): CacheResponse {
        return kotlin.runCatching {
            k9BrukerdialogCacheRestTemplate.exchange(
                "${cacheUrl.path}/${cacheRequest.nøkkelPrefiks}",
                HttpMethod.PUT,
                HttpEntity(cacheRequest),
                CacheResponse::class.java
            ).body!!
        }.fold(
            onSuccess = { cacheResponse: CacheResponse -> cacheResponse },
            onFailure = { error: Throwable ->
                if (error is RestClientException) {
                    logger.error("Feil ved oppdatering av mellomlagret søknad. Feilmelding: ${error.message}")
                }
                throw RuntimeException("Feil ved oppdatering av mellomlagret søknad.", error)
            }
        )
    }

    fun slettMellomlagretSøknad(nøkkelPrefiks: String): Boolean {
        return kotlin.runCatching {
            k9BrukerdialogCacheRestTemplate.delete("${cacheUrl.path}/$nøkkelPrefiks")
        }.fold(
            onSuccess = { true },
            onFailure = { error: Throwable ->
                if (error is RestClientException) {
                    logger.error("Feil ved sletting av mellomlagret søknad. Feilmelding: ${error.message}")
                }
                throw RuntimeException("Feil ved sletting av mellomlagret søknad.", error)
            }
        )
    }
}
