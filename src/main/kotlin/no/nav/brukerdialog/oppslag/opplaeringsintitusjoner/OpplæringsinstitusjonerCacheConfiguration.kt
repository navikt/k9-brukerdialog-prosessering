package no.nav.brukerdialog.oppslag.opplaeringsintitusjoner

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class OpplæringsinstitusjonerCacheConfiguration(
    @Value("\${no.nav.cache.opplaeringsinstitusjoner.expiry-in-minutes}") private val expiryInMinutes: Long
) {

    @Bean
    internal fun opplaeringsinstitusjonerCache(): Cache<Unit, List<Opplæringsinstitusjon>> {
        return Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(expiryInMinutes))
            .maximumSize(1)
            .build()
    }
}
