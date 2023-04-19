package no.nav.k9brukerdialogprosessering.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.k9.søknad.JsonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Configuration
class JacksonConfiguration {

    @Bean
    @Primary
    @Autowired
    fun objectMapper(kotlinModule: KotlinModule, javaTimeModule: JavaTimeModule): ObjectMapper {
        return JsonUtils.getObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC))
            .registerModule(kotlinModule)
            .registerModule(javaTimeModule)
    }

    @Bean
    fun kotlinModule(): KotlinModule {
        return KotlinModule.Builder().build().also {
            it.addSerializer(ZonedDateTime::class.java, CustomZonedDateTimeSerializer())
        }
    }

    @Bean
    fun javaTimeModule(): JavaTimeModule {
        return JavaTimeModule().also {
            it.addSerializer(ZonedDateTime::class.java, CustomZonedDateTimeSerializer())
        }
    }
}

class CustomZonedDateTimeSerializer : JsonSerializer<ZonedDateTime?>() {
    companion object {
        private val FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS]X").withZone(ZoneOffset.UTC)
    }

    override fun serialize(zdt: ZonedDateTime?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        val formattedDate = zdt?.format(FORMATTER)
        gen?.writeString(formattedDate)
    }
}
