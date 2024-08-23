package no.nav.brukerdialog.ytelse.omsorgspengerutbetalingat.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.every
import no.nav.brukerdialog.domenetjenester.innsending.InnsendingCache
import no.nav.brukerdialog.domenetjenester.innsending.InnsendingService
import no.nav.brukerdialog.metrikk.MetrikkService
import no.nav.brukerdialog.ytelse.Ytelse
import no.nav.brukerdialog.ytelse.fellesdomene.Bekreftelser
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingat.api.OmsorgspengerUtbetalingArbeidstakerController
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingat.api.domene.Barn
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingat.api.domene.DineBarn
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingat.api.domene.TypeBarn
import no.nav.brukerdialog.config.JacksonConfiguration
import no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag.BarnService
import no.nav.brukerdialog.utils.CallIdGenerator
import no.nav.brukerdialog.utils.NavHeaders
import no.nav.brukerdialog.utils.TokenTestUtils.mockContext
import no.nav.brukerdialog.ytelse.omsorgspengerutbetalingat.utils.SøknadUtils
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
@WebMvcTest(controllers = [OmsorgspengerUtbetalingArbeidstakerController::class])
@Import(
    JacksonConfiguration::class,
    CallIdGenerator::class
)
class OmsorgspengerUtbetalingArbeidstakerControllerTest {


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

        mockMvc.post("/omsorgspenger-utbetaling-arbeidstaker/innsending") {
            headers {
                set(NavHeaders.BRUKERDIALOG_YTELSE, Ytelse.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER.dialog)
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
        coEvery { innsendingService.forsikreInnloggetBrukerErSøker(any()) } returns Unit
        every { innsendingCache.put(any()) } returns Unit

        val over19ÅrGammel = LocalDate.now().minusYears(20)
        val jsonPayload = objectMapper.writeValueAsString(
            SøknadUtils.defaultSøknad.copy(
                dineBarn = DineBarn(
                    harDeltBosted = true,
                    barn = listOf(
                        Barn(
                            navn = "",
                            fødselsdato = over19ÅrGammel,
                            type = TypeBarn.FRA_OPPSLAG,
                            identitetsnummer = "123ABC"
                        )
                    ),
                ),
                bekreftelser = Bekreftelser(
                    harBekreftetOpplysninger = false,
                    harForståttRettigheterOgPlikter = false
                ),
            )
        )
        mockMvc.post("/omsorgspenger-utbetaling-arbeidstaker/innsending") {
            headers {
                set(NavHeaders.BRUKERDIALOG_YTELSE, Ytelse.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER.dialog)
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
                          "instance": "http://localhost/omsorgspenger-utbetaling-arbeidstaker/innsending",
                          "title": "invalid-request-parameters",
                          "status": 400,
                          "detail": "Forespørselen inneholder valideringsfeil",
                          "violations": [
                            {
                              "parameterName": "dineBarn.barn[0].identitetsnummer",
                              "parameterType": "ENTITY",
                              "reason": "size must be between 11 and 11"
                            },
                            {
                              "parameterName": "dineBarn.barn[0].identitetsnummer",
                              "parameterType": "ENTITY",
                              "reason": "'123ABC' matcher ikke tillatt pattern '^\\d+$'"
                            },
                             {
                              "parameterName": "dineBarn.barn[0].fødselsdato",
                              "parameterType": "ENTITY",
                              "reason": "Kan ikke være eldre enn 19 år"
                            },
                            {
                              "invalidValue": "",
                              "parameterName": "dineBarn.barn[0].navn",
                              "parameterType": "ENTITY",
                              "reason": "Kan ikke være tomt eller blankt"
                            },
                            {
                              "invalidValue": false,
                              "parameterName": "bekreftelser.harBekreftetOpplysninger",
                              "parameterType": "ENTITY",
                              "reason": "Opplysningene må bekreftes for å sende inn søknad"
                            },
                            {
                              "invalidValue": false,
                              "parameterName": "bekreftelser.harForståttRettigheterOgPlikter",
                              "parameterType": "ENTITY",
                              "reason": "Må ha forstått rettigheter og plikter for å sende inn søknad"
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
