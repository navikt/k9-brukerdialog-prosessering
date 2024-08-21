package no.nav.brukerdialog.ytelse.omsorgspengerkronisksyktbarn.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.every
import no.nav.brukerdialog.domenetjenester.innsending.InnsendingCache
import no.nav.brukerdialog.domenetjenester.innsending.InnsendingService
import no.nav.brukerdialog.metrikk.MetrikkService
import no.nav.brukerdialog.ytelse.Ytelse
import no.nav.brukerdialog.config.JacksonConfiguration
import no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag.BarnService
import no.nav.brukerdialog.utils.CallIdGenerator
import no.nav.brukerdialog.utils.NavHeaders
import no.nav.brukerdialog.utils.TokenTestUtils.mockContext
import no.nav.brukerdialog.ytelse.omsorgspengerkronisksyktbarn.utils.SøknadUtils
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
@WebMvcTest(controllers = [OmsorgspengerUtvidetRettController::class])
@Import(
    JacksonConfiguration::class,
    CallIdGenerator::class
)
class OmsorgspengerUtvidetRettControllerTest {

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
        coEvery { barnService.hentBarn() } returns emptyList()
        every { innsendingCache.put(any()) } returns Unit
        coEvery { innsendingService.registrer(any(), any()) } returns Unit
        every { metrikkService.registrerMottattSøknad(any()) } returns Unit

        val defaultSøknad = SøknadUtils.defaultSøknad

        mockMvc.post("/omsorgspenger-utvidet-rett/innsending") {
            headers {
                set(NavHeaders.BRUKERDIALOG_YTELSE, Ytelse.OMSORGSPENGER_UTVIDET_RETT.dialog)
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

    @Test
    fun `Innsending av søknad med feile verdier responderer med bad request`() {
        coEvery { barnService.hentBarn() } returns emptyList()
        coEvery { innsendingService.registrer(any(), any()) } answers { callOriginal() }
        coEvery { innsendingService.forsikreValidert(any()) } answers { callOriginal() }
        every { innsendingCache.put(any()) } returns Unit

        val defaultSøknad = SøknadUtils.defaultSøknad

        val fødselsdatoIFremtiden = LocalDate.now().plusDays(1)
        val jsonPayload = objectMapper.writeValueAsString(
            defaultSøknad.copy(
                harForståttRettigheterOgPlikter = false,
                harBekreftetOpplysninger = false,
                barn = defaultSøknad.barn.copy(
                    norskIdentifikator = "123ABC", // Feil format
                    navn = "", // Tomt navn
                    fødselsdato = fødselsdatoIFremtiden // Fødselsdato i fremtiden er ikke gyldig
                ),
                høyereRisikoForFravær = true,
                høyereRisikoForFraværBeskrivelse = "¨ er ikke tillatt",
            )
        )
        mockMvc.post("/omsorgspenger-utvidet-rett/innsending") {
            headers {
                set(NavHeaders.BRUKERDIALOG_YTELSE, Ytelse.OMSORGSPENGER_UTVIDET_RETT.dialog)
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
                          "instance": "http://localhost/omsorgspenger-utvidet-rett/innsending",
                          "title": "invalid-request-parameters",
                          "status": 400,
                          "detail": "Forespørselen inneholder valideringsfeil",
                          "violations": [
                            {
                              "invalidValue": "123ABC",
                              "parameterName": "barn.norskIdentifikator",
                              "parameterType": "ENTITY",
                              "reason": "size must be between 11 and 11"
                            },
                            {
                              "invalidValue": "123ABC",
                              "parameterName": "barn.norskIdentifikator",
                              "parameterType": "ENTITY",
                              "reason": "'123ABC' matcher ikke tillatt pattern '^\\d+$'"
                            },
                             {
                              "invalidValue": "$fødselsdatoIFremtiden",
                              "parameterName": "barn.fødselsdato",
                              "parameterType": "ENTITY",
                              "reason": "Kan ikke være i fremtiden"
                            },
                            {
                              "invalidValue": "",
                              "parameterName": "barn.navn",
                              "parameterType": "ENTITY",
                              "reason": "Kan ikke være tomt eller blankt"
                            },
                            {
                              "invalidValue": "¨ er ikke tillatt",
                              "parameterName": "høyereRisikoForFraværBeskrivelse",
                              "parameterType": "ENTITY",
                              "reason": "Matcher ikke tillatt mønster: '^[\\p{Punct}\\p{L}\\p{M}\\p{N}\\p{Sc}\\p{Space}«»–§�\\u2018\\u2019\\u201a\\u201b\\u201c\\u201d\\u201e\\u201f\\u00b4\\u2026]*${'$'}'"
                            },
                            {
                              "invalidValue": false,
                              "parameterName": "harForståttRettigheterOgPlikter",
                              "parameterType": "ENTITY",
                              "reason": "Må ha forstått rettigheter og plikter for å sende inn søknad"
                            },
                            {
                              "invalidValue": false,
                              "parameterName": "harBekreftetOpplysninger",
                              "parameterType": "ENTITY",
                              "reason": "Opplysningene må bekreftes for å sende inn søknad"
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
