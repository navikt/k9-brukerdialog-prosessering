package no.nav.brukerdialog.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.annotation.EnableRetry
import org.springframework.retry.support.RetryTemplate

@Configuration
@EnableRetry
class RetryTemplateConfiguration(
    @Value("\${spring.rest.retry.maxAttempts:3}") private val maxAttempts: Int,
    @Value("\${spring.rest.retry.initialDelay:1000}") private val initialIntervalMs: Long,
    @Value("\${spring.rest.retry.multiplier:3}") private val multiplier: Int,
    @Value("\${spring.rest.retry.maxDelay:60000}") private val maxIntervalMs: Long,
) {

    @Bean
    fun retryTemplate(): RetryTemplate {
        val retryTemplate = RetryTemplate.builder()
            .maxAttempts(maxAttempts)
            .exponentialBackoff(initialIntervalMs, multiplier.toDouble(), maxIntervalMs)
            .build()

        retryTemplate.setThrowLastExceptionOnExhausted(true)
        return retryTemplate
    }
}
