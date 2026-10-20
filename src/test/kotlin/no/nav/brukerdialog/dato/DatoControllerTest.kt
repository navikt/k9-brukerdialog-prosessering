package no.nav.brukerdialog.dato

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.brukerdialog.config.JacksonConfiguration
import no.nav.brukerdialog.utils.CallIdGenerator
import no.nav.brukerdialog.utils.TokenTestUtils.mockContext
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.hamcrest.Matchers.matchesPattern
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@ExtendWith(SpringExtension::class)
@WebMvcTest(controllers = [DatoController::class])
@Import(
    JacksonConfiguration::class,
    CallIdGenerator::class
)
class DatoControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var springTokenValidationContextHolder: SpringTokenValidationContextHolder

    @Test
    fun `Forventer å returnere dagens dato med gyldig tokenkontekst`() {
        springTokenValidationContextHolder.mockContext()

        mockMvc.get("/dato/dagens")
            .andExpect {
                status { isOk() }
                jsonPath("$.dato") { value(matchesPattern("^\\d{4}-\\d{2}-\\d{2}$")) }
            }
    }

    @Test
    fun `Forventer unauthorized når forespørsel mangler token`() {
        every { springTokenValidationContextHolder.getTokenValidationContext() } returns TokenValidationContext(emptyMap())

        mockMvc.get("/dato/dagens")
            .andExpect {
                status { isUnauthorized() }
            }
    }
}
