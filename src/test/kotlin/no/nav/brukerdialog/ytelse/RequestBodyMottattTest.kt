package no.nav.brukerdialog.ytelse

import no.nav.brukerdialog.config.JacksonConfiguration
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

class RequestBodyMottattTest {

    private val objectMapper = JacksonConfiguration.configureObjectMapper()

    @Test
    fun `ingen request body fra rest-kontrollere kan sette mottatt`() {
        val requestBodyTyper = requestBodyTyper()
        assertTrue(requestBodyTyper.size > 1, "Fant ikke request body-typer: $requestBodyTyper")

        val overstyrbare = requestBodyTyper.flatMap { type ->
            objectMapper.deserializationConfig
                .introspect(objectMapper.constructType(type))
                .findProperties()
                .filter { it.name == "mottatt" || it.name == "mottattDato" }
                .map { "${type.simpleName}.${it.name}" }
        }

        assertTrue(overstyrbare.isEmpty(), "Mangler @JsonIgnore på: $overstyrbare")
    }

    private fun requestBodyTyper(): Set<Class<*>> =
        ClassPathScanningCandidateComponentProvider(false)
            .apply { addIncludeFilter(AnnotationTypeFilter(RestController::class.java)) }
            .findCandidateComponents("no.nav.brukerdialog")
            .map { Class.forName(it.beanClassName) }
            .flatMap { it.declaredMethods.asList() }
            .flatMap { it.parameters.asList() }
            .filter { it.isAnnotationPresent(RequestBody::class.java) }
            .map { it.type }
            .toSet()
}
