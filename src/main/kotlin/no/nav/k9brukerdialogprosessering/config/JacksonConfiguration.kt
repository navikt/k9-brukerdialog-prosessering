package no.nav.k9brukerdialogprosessering.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonDeserializer
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

@Configuration
class JacksonConfiguration {
    companion object {
        fun configureKotlinModule(): KotlinModule {
            return KotlinModule.Builder().build().also {
                it.addSerializer(ZonedDateTime::class.java, CustomZonedDateTimeSerializer())
                it.addDeserializer(ZonedDateTime::class.java, CustomZonedDateTimeDeSerializer())
            }
        }

        fun configureJavaTimeModule(): JavaTimeModule {
            return JavaTimeModule().also {
                it.addSerializer(ZonedDateTime::class.java, CustomZonedDateTimeSerializer())
                it.addDeserializer(ZonedDateTime::class.java, CustomZonedDateTimeDeSerializer())
            }
        }

        fun configureObjectMapper(
            kotlinModule: KotlinModule = configureKotlinModule(),
            javaTimeModule: JavaTimeModule = configureJavaTimeModule(),
        ): ObjectMapper = JsonUtils.getObjectMapper()
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .registerModule(kotlinModule)
            .registerModule(javaTimeModule)
    }

    @Bean
    @Primary
    @Autowired
    fun objectMapper(kotlinModule: KotlinModule, javaTimeModule: JavaTimeModule): ObjectMapper {
        return configureObjectMapper(kotlinModule, javaTimeModule)
    }

    @Bean
    fun kotlinModule(): KotlinModule = configureKotlinModule()

    @Bean
    fun javaTimeModule(): JavaTimeModule = configureJavaTimeModule()
}

class CustomZonedDateTimeSerializer : JsonSerializer<ZonedDateTime?>() {
    companion object {
        val FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS]X").withZone(ZoneOffset.UTC)
    }

    override fun serialize(zdt: ZonedDateTime?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        val formattedDate = zdt?.format(FORMATTER)
        gen?.writeString(formattedDate)
    }
}

class CustomZonedDateTimeDeSerializer : JsonDeserializer<ZonedDateTime?>() {
    companion object {
        val FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS]X").withZone(ZoneOffset.UTC)
    }

    override fun deserialize(p0: JsonParser?, p1: DeserializationContext?): ZonedDateTime? {
        return ZonedDateTime.parse(p0?.valueAsString, FORMATTER)
    }
}
