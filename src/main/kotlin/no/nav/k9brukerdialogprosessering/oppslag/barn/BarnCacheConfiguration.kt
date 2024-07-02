package no.nav.k9brukerdialogprosessering.oppslag.barn

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class BarnCacheConfiguration(
    @Value("\${no.nav.cache.barn.max-size}") private val maxSize: Long,
    @Value("\${no.nav.cache.barn.expiry-in-minutes}") private val expiryInMinutes: Long
) {

    @Bean
    internal fun barnCache(): Cache<String, List<BarnOppslag>> {
        return Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(expiryInMinutes))
            .maximumSize(maxSize)
            .build()
    }
}
