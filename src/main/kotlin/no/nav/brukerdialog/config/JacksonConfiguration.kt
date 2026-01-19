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
import org.springframework.boot.http.converter.autoconfigure.ServerHttpMessageConvertersCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.ByteArrayHttpMessageConverter
import org.springframework.http.converter.HttpMessageConverters
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
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
   fun objectMapper(): ObjectMapper {
        return configureObjectMapper(configureKotlinModule(), configureJavaTimeModule())
    }

    @Bean
    fun serverHttpMessageConvertersCustomizer(objectMapper: ObjectMapper): ServerHttpMessageConvertersCustomizer {
        return ServerHttpMessageConvertersCustomizer { converters: HttpMessageConverters.ServerBuilder ->
            //Må være først for at swagger skal funke
            converters.addCustomConverter(ByteArrayHttpMessageConverter())
            //Brukes til å parse request i Controllere
            converters.addCustomConverter(MappingJackson2HttpMessageConverter(objectMapper))
        }
    }
}

class CustomZonedDateTimeSerializer : JsonSerializer<ZonedDateTime>() {
    override fun serialize(zdt: ZonedDateTime?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        val formattedDate = zdt?.format(zonedDateTimeFormatter)
        gen?.writeString(formattedDate)
    }
}

class CustomZonedDateTimeDeSerializer : JsonDeserializer<ZonedDateTime>() {

    override fun deserialize(p0: JsonParser?, p1: DeserializationContext?): ZonedDateTime {
        return ZonedDateTime.parse(p0?.valueAsString, zonedDateTimeFormatter)
    }
}
