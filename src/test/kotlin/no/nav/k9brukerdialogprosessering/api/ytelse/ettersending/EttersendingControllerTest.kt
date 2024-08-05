package no.nav.k9brukerdialogprosessering.api.ytelse.ettersending

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import no.nav.k9.ettersendelse.EttersendelseType
import no.nav.k9brukerdialogprosessering.api.innsending.InnsendingCache
import no.nav.k9brukerdialogprosessering.api.innsending.InnsendingService
import no.nav.k9brukerdialogprosessering.api.ytelse.MetrikkService
import no.nav.k9brukerdialogprosessering.api.ytelse.Ytelse
import no.nav.k9brukerdialogprosessering.api.ytelse.ettersending.domene.Pleietrengende
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
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.LocalDate
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension::class)
@WebMvcTest(
    controllers = [EttersendingController::class]
)
@Import(
    JacksonConfiguration::class,
    CallIdGenerator::class
)
class EttersendingControllerTest {

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
    fun `Innsending av søknad er OK`() {
        val ettersendelse = EttersendelseUtils.defaultEttersendelse

        mockMvc.post("/ettersending/innsending") {
            headers {
                set(NavHeaders.BRUKERDIALOG_YTELSE, Ytelse.ETTERSENDING_PLEIEPENGER_SYKT_BARN.name)
                set(NavHeaders.BRUKERDIALOG_GIT_SHA, UUID.randomUUID().toString())
            }
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(ettersendelse)
        }
            .andExpect {
                status { isAccepted() }
            }
    }

    @Test
    fun `Innsending av ettersendelse med feile verdier responderer med bad request`() {
        val ettersendelse = EttersendelseUtils.defaultEttersendelse

        val fødselsdatoIFremtiden = LocalDate.now().plusDays(1)
        val jsonPayload = objectMapper.writeValueAsString(
            ettersendelse.copy(
                harForståttRettigheterOgPlikter = false,
                harBekreftetOpplysninger = false,
                vedlegg = listOf(),
                ettersendelsesType = EttersendelseType.LEGEERKLÆRING,
                pleietrengende = Pleietrengende(
                    norskIdentitetsnummer = "123ABC",
                    fødselsdato = fødselsdatoIFremtiden
                )
            )
        )
        mockMvc.post("/ettersending/innsending") {
            headers {
                set(NavHeaders.BRUKERDIALOG_YTELSE, Ytelse.ETTERSENDING_PLEIEPENGER_SYKT_BARN.name)
                set(NavHeaders.BRUKERDIALOG_GIT_SHA, UUID.randomUUID().toString())
            }
            contentType = MediaType.APPLICATION_JSON
            content = jsonPayload.trimIndent()
        }
            .andExpect {
                status { isBadRequest() }
                header { exists(NavHeaders.PROBLEM_DETAILS) }
                content {
                    json(
                        """
                        {
                          "type": "/problem-details/invalid-request-parameters",
                          "instance": "http://localhost/ettersending/innsending",
                          "title": "invalid-request-parameters",
                          "status": 400,
                          "detail": "Forespørselen inneholder valideringsfeil",
                          "violations": [
                            {
                              "invalidValue": [],
                              "parameterName": "ettersendelse.vedlegg",
                              "parameterType": "ENTITY",
                              "reason": "Kan ikke være tom"
                            },
                            {
                              "invalidValue": "123ABC",
                              "parameterName": "ettersendelse.pleietrengende.norskIdentitetsnummer",
                              "parameterType": "ENTITY",
                              "reason": "size must be between 11 and 11"
                            },
                            {
                              "invalidValue": "123ABC",
                              "parameterName": "ettersendelse.pleietrengende.norskIdentitetsnummer",
                              "parameterType": "ENTITY",
                              "reason": "'123ABC' matcher ikke tillatt pattern '^\\d+$'"
                            },
                            {
                              "invalidValue": "$fødselsdatoIFremtiden",
                              "parameterName": "ettersendelse.pleietrengende.fødselsdato",
                              "parameterType": "ENTITY",
                              "reason": "Kan ikke være i fremtiden"
                            },
                            {
                              "invalidValue": false,
                              "parameterName": "ettersendelse.harForståttRettigheterOgPlikter",
                              "parameterType": "ENTITY",
                              "reason": "Må ha forstått rettigheter og plikter for å sende inn ettersendelse"
                            },
                            {
                              "invalidValue": false,
                              "parameterName": "ettersendelse.harBekreftetOpplysninger",
                              "parameterType": "ENTITY",
                              "reason": "Opplysningene må bekreftes for å sende inn ettersendelse"
                            },
                          ]
                        }
                        """.trimIndent(),
                        false,
                    )
                }
            }
    }
}
