package no.nav.k9brukerdialogprosessering

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.annotation.EnableRetry
import org.springframework.retry.support.RetryTemplate

@Configuration
@EnableRetry
class RetryTemplateConfiguration {

    companion object {
        const val MAX_ATTEMPTS = 3
        const val MULTIPLIER = 3.0
        const val INITAL_INTERVAL_MS = 1_000L
        const val MAX_INTERVAL_MS = 60_000L
        // 1s, 3s, 9s
    }

    @Bean
    fun retryTemplate(): RetryTemplate {
        val retryTemplate = RetryTemplate.builder()
            .maxAttempts(MAX_ATTEMPTS)
            .exponentialBackoff(INITAL_INTERVAL_MS, MULTIPLIER, MAX_INTERVAL_MS)
            .build()

        retryTemplate.setThrowLastExceptionOnExhausted(true)
        return retryTemplate
    }
}
