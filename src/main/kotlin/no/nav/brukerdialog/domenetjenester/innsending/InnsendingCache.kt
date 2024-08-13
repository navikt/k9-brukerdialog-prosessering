package no.nav.brukerdialog.domenetjenester.innsending

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.stereotype.Service
import org.springframework.web.ErrorResponseException
import java.net.URI
import java.time.Duration

@Service
class InnsendingCache(
    @Value("\${no.nav.cache.innsending.expiry-in-seconds}") private val expireSeconds: Long
) {

    private val cache: Cache<String, String> = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(Duration.ofSeconds(expireSeconds))
        .evictionListener<String, String> { _, _, _ -> logger.info("Tømmer cache for ugått innsending.") }
        .build()

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @kotlin.jvm.Throws(ErrorResponseException::class)
    fun put(key: String) {
        if (duplikatEksisterer(key)) {
            throw DuplikatInnsendingProblem()
        }
        cache.put(key, "") // verdi er ikke relevant. Vi er kun interessert i key.
    }

    private fun duplikatEksisterer(key: String): Boolean = when (cache.getIfPresent(key)) {
        null -> false
        else -> true
    }
}

class DuplikatInnsendingProblem(body: ProblemDetail = defaultProblemDetail()) :
    ErrorResponseException(HttpStatus.valueOf(body.status), body, null) {
    companion object {
        fun defaultProblemDetail(): ProblemDetail {
            val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
            problemDetail.type = URI("/problem-details/duplikat-innsending")
            problemDetail.title = "Duplikat innsending"
            problemDetail.detail = "Det ble funnet en eksisterende innsending på søker med samme ytelse."
            problemDetail.instance = URI("")
            problemDetail.status = HttpStatus.BAD_REQUEST.value()
            return problemDetail
        }
    }
}
