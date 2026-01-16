package no.nav.brukerdialog.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.brukerdialog.config.JacksonConfiguration.Companion.zonedDateTimeFormatter
import no.nav.k9.søknad.JsonUtils
import org.springframework.boot.jackson2.autoconfigure.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Configuration
class JacksonConfiguration {
    companion object {
        val zonedDateTimeFormatter = DateTimeFormatter.ISO_INSTANT.withZone(UTC)
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
    fun objectMapper(kotlinModule: KotlinModule, javaTimeModule: JavaTimeModule): ObjectMapper {
        return configureObjectMapper(kotlinModule, javaTimeModule)
    }

    @Bean
    fun kotlinModule(): KotlinModule = configureKotlinModule()

    @Bean
    fun javaTimeModule(): JavaTimeModule = configureJavaTimeModule()

    @Bean
    fun jackson2ObjectMapperBuilderCustomizer(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { builder ->
            builder.visibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            builder.visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            builder.featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            builder.modules(configureKotlinModule(), configureJavaTimeModule())
        }
    }
}

class CustomZonedDateTimeSerializer : JsonSerializer<ZonedDateTime?>() {
    override fun serialize(zdt: ZonedDateTime?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        val formattedDate = zdt?.format(zonedDateTimeFormatter)
        gen?.writeString(formattedDate)
    }
}

class CustomZonedDateTimeDeSerializer : JsonDeserializer<ZonedDateTime?>() {

    override fun deserialize(p0: JsonParser?, p1: DeserializationContext?): ZonedDateTime? {
        return ZonedDateTime.parse(p0?.valueAsString, zonedDateTimeFormatter)
    }
}
