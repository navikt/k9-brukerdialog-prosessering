package no.nav.k9brukerdialogprosessering.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.k9.søknad.JsonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class JacksonConfiguration {

    @Bean
    @Primary
    @Autowired
    fun objectMapper(kotlinModule: KotlinModule): ObjectMapper {
        return JsonUtils.getObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .registerModule(kotlinModule)
    }

    @Bean
    fun kotlinModule(): KotlinModule {
        return KotlinModule.Builder().build()
    }
}
