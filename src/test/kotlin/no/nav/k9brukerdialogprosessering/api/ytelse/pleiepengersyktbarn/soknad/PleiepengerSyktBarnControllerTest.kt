package no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import no.nav.k9brukerdialogprosessering.api.innsending.InnsendingCache
import no.nav.k9brukerdialogprosessering.api.innsending.InnsendingService
import no.nav.k9brukerdialogprosessering.api.ytelse.MetrikkService
import no.nav.k9brukerdialogprosessering.api.ytelse.Ytelse
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.SøknadUtils
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.Arbeidsgiver
import no.nav.k9brukerdialogprosessering.config.JacksonConfiguration
import no.nav.k9brukerdialogprosessering.oppslag.barn.BarnService
import no.nav.k9brukerdialogprosessering.utils.CallIdGenerator
import no.nav.k9brukerdialogprosessering.utils.NavHeaders
import no.nav.k9brukerdialogprosessering.utils.TokenTestUtils.mockContext
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension::class)
@WebMvcTest(
    controllers = [PleiepengerSyktBarnController::class]
)
@Import(
    JacksonConfiguration::class,
    CallIdGenerator::class
)
@ActiveProfiles("test")
class PleiepengerSyktBarnControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var innsendingService: InnsendingService

    @MockkBean
    private lateinit var innsendingCache: InnsendingCache

    @MockkBean
    private lateinit var barnService: BarnService

    @MockkBean
    private lateinit var springTokenValidationContextHolder: SpringTokenValidationContextHolder

    @MockkBean
    private lateinit var metrikkService: MetrikkService

    @BeforeEach
    fun setUp() {
        springTokenValidationContextHolder.mockContext()
    }

    @Test
    fun `Innsending av søknad feiler`() {
        val defaultSøknad = SøknadUtils.defaultSøknad()

        val jsonPayload = objectMapper.writeValueAsString(
            defaultSøknad.copy(
                harForståttRettigheterOgPlikter = false,
                harBekreftetOpplysninger = false,
                barn = defaultSøknad.barn.copy(
                    fødselsnummer = "123ABC",
                    navn = ""
                ),
                arbeidsgivere = listOf(
                    Arbeidsgiver(
                        organisasjonsnummer = "123ABC",
                        navn = "",
                        erAnsatt = false,
                        arbeidsforhold = null
                    )
                )
            )
        )
        mockMvc.post("/pleiepenger-sykt-barn/innsending") {
            headers {
                set(NavHeaders.BRUKERDIALOG_YTELSE, Ytelse.PLEIEPENGER_SYKT_BARN.name)
                set(NavHeaders.BRUKERDIALOG_GIT_SHA, UUID.randomUUID().toString())
            }
            contentType = MediaType.APPLICATION_JSON
            content = jsonPayload.trimIndent()
        }
            .andExpect {
                status { isBadRequest() }
                content {
                    json(
                        """
                        {
                          "type": "/problem-details/invalid-request-parameters",
                          "instance": "http://localhost/pleiepenger-sykt-barn/innsending",
                          "title": "invalid-request-parameters",
                          "status": 400,
                          "detail": "Forespørselen inneholder valideringsfeil",
                          "violations": [
                            {
                              "invalidValue": "123ABC",
                              "parameterName": "pleiepengerSyktBarnSøknad.barn.fødselsnummer",
                              "parameterType": "ENTITY",
                              "reason": "size must be between 11 and 11"
                            },
                            {
                              "invalidValue": false,
                              "parameterName": "pleiepengerSyktBarnSøknad.harBekreftetOpplysninger",
                              "parameterType": "ENTITY",
                              "reason": "Opplysningene må bekreftes for å sende inn søknad"
                            },
                            {
                              "invalidValue": "123ABC",
                              "parameterName": "pleiepengerSyktBarnSøknad.arbeidsgivere[0].organisasjonsnummer",
                              "parameterType": "ENTITY",
                              "reason": "'123ABC' matcher ikke tillatt pattern '^\\d+$'"
                            },
                            {
                              "invalidValue": "",
                              "parameterName": "pleiepengerSyktBarnSøknad.arbeidsgivere[0].navn",
                              "parameterType": "ENTITY",
                              "reason": "navn kan ikke være tomt eller blankt"
                            },
                            {
                              "invalidValue": "123ABC",
                              "parameterName": "pleiepengerSyktBarnSøknad.barn.fødselsnummer",
                              "parameterType": "ENTITY",
                              "reason": "'123ABC' matcher ikke tillatt pattern '^\\d+$'"
                            },
                            {
                              "invalidValue": false,
                              "parameterName": "pleiepengerSyktBarnSøknad.harForståttRettigheterOgPlikter",
                              "parameterType": "ENTITY",
                              "reason": "Må ha forstått rettigheter og plikter for å sende inn søknad"
                            }
                          ]
                        }
                        """.trimIndent(), false,
                    )
                }
            }
    }

    @Test
    fun `Innsending av søknad er OK`() {
        val defaultSøknad = SøknadUtils.defaultSøknad()

        mockMvc.post("/pleiepenger-sykt-barn/innsending") {
            headers {
                set(NavHeaders.BRUKERDIALOG_YTELSE, Ytelse.PLEIEPENGER_SYKT_BARN.name)
                set(NavHeaders.BRUKERDIALOG_GIT_SHA, UUID.randomUUID().toString())
            }
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(defaultSøknad)
        }
            .andExpect {
                status { isAccepted() }
            }
    }
}
