package no.nav.brukerdialog.validation

import com.ninjasquad.springmockk.MockkBean
import no.nav.brukerdialog.config.JacksonConfiguration
import no.nav.brukerdialog.utils.CallIdGenerator
import no.nav.brukerdialog.utils.TokenTestUtils.mockContext
import no.nav.security.mock.oauth2.http.json
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@ExtendWith(SpringExtension::class)
@WebMvcTest(controllers = [FeltValideringController::class])
@Import(
    JacksonConfiguration::class,
    CallIdGenerator::class
)
class FeltValideringControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var springTokenValidationContextHolder: SpringTokenValidationContextHolder

    @BeforeEach
    fun setUp() {
        springTokenValidationContextHolder.mockContext()
    }

    @Test
    fun `Forventer å akseptere gyldig fritekstfelt`() {
        val gyldigVerdi = """
            {
                "navnPåFelt": "FeltNavn",
                "verdi": "ABCDEFGHIJKLMNOPQRSTUVWXYZÆØÅ abcdefghijklmnopqrstuvwxyzæøå 0123456789 !\"#${'$'}%&'()*+,-./:;<=>?@[\\]^_`{|}~ €£¥ «Test» § ÆØÅ æøå éñ αβγ"
            }
        """.trimIndent()

        mockMvc.post("/valider/friteksfelt") {
            contentType = MediaType.APPLICATION_JSON
            content = gyldigVerdi
        }
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `Forventer å gi valideringsfeil for fritekstfelt med ugyldig tegn`() {
        val ugyldigVerdi = """
            {
                "navnPåFelt": "FeltNavn",
                "verdi": "Gyldig tekst med € og «sitat» og §, men med en ugyldig null-byte \u0000 og en zero-width space ¨"
            }
        """.trimIndent()

        mockMvc.post("/valider/friteksfelt") {
            contentType = MediaType.APPLICATION_JSON
            content = ugyldigVerdi
        }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.instance") { value("http://localhost/valider/friteksfelt") }
                jsonPath("$.type") { value("/problem-details/invalid-request-parameters") }
                jsonPath("$.violations[0].parameterName") { value("friteksfelt.verdi") }
                jsonPath("$.violations[0].reason") { value("Ugyldige tegn funnet i teksten: \\u0000, ¨") }
            }
    }
}

