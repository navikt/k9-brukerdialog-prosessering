package no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.every
import no.nav.brukerdialog.api.innsending.InnsendingCache
import no.nav.brukerdialog.api.innsending.InnsendingService
import no.nav.brukerdialog.api.ytelse.MetrikkService
import no.nav.brukerdialog.api.ytelse.Ytelse
import no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.utils.SøknadUtils
import no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.api.domene.Barn
import no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.api.domene.TidspunktForAleneomsorg
import no.nav.brukerdialog.ytelse.omsorgspengeraleneomsorg.api.domene.TypeBarn
import no.nav.brukerdialog.config.JacksonConfiguration
import no.nav.brukerdialog.oppslag.barn.BarnService
import no.nav.brukerdialog.utils.CallIdGenerator
import no.nav.brukerdialog.utils.NavHeaders
import no.nav.brukerdialog.utils.TokenTestUtils.mockContext
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
    controllers = [OmsorgsdagerAleneomsorgController::class]
)
@Import(
    JacksonConfiguration::class,
    CallIdGenerator::class
)
class OmsorgsdagerAleneomsorgControllerTest {

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

        mockMvc.post("/omsorgsdager-aleneomsorg/innsending") {
            headers {
                set(NavHeaders.BRUKERDIALOG_YTELSE, Ytelse.OMSORGSDAGER_ALENEOMSORG.dialog)
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
        val defaultSøknad = SøknadUtils.defaultSøknad

        val fødselsdatoIFremtiden = LocalDate.now().plusDays(1)
        val forLangtNavn =
            "barnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnbarnb"
        val jsonPayload = objectMapper.writeValueAsString(
            defaultSøknad.copy(
                harForståttRettigheterOgPlikter = false,
                harBekreftetOpplysninger = false,
                barn = listOf(
                    Barn(
                        navn = "",
                        type = TypeBarn.FOSTERBARN,
                        fødselsdato = null,
                        aktørId = "123",
                        identitetsnummer = "123ABC",
                        tidspunktForAleneomsorg = TidspunktForAleneomsorg.SISTE_2_ÅRENE,
                        dato = null
                    ),
                    Barn(
                        navn = forLangtNavn,
                        type = TypeBarn.FRA_OPPSLAG,
                        fødselsdato = LocalDate.now().plusDays(1),
                        aktørId = "123",
                        identitetsnummer = "25058118020",
                        tidspunktForAleneomsorg = TidspunktForAleneomsorg.TIDLIGERE,
                        dato = null
                    )
                ),
            )
        )
        mockMvc.post("/omsorgsdager-aleneomsorg/innsending") {
            headers {
                set(NavHeaders.BRUKERDIALOG_YTELSE, Ytelse.OMSORGSDAGER_ALENEOMSORG.dialog)
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
                          "instance": "http://localhost/omsorgsdager-aleneomsorg/innsending",
                          "title": "invalid-request-parameters",
                          "status": 400,
                          "detail": "Forespørselen inneholder valideringsfeil",
                          "violations": [
                            {
                              "invalidValue": "123ABC",
                              "parameterName": "omsorgsdagerAleneOmOmsorgenSøknad.barn[0].identitetsnummer",
                              "parameterType": "ENTITY",
                              "reason": "size must be between 11 and 11"
                            },
                            {
                              "invalidValue": "123ABC",
                              "parameterName": "omsorgsdagerAleneOmOmsorgenSøknad.barn[0].identitetsnummer",
                              "parameterType": "ENTITY",
                              "reason": "'123ABC' matcher ikke tillatt pattern '^\\d+$'"
                            },
                             {
                              "parameterName": "omsorgsdagerAleneOmOmsorgenSøknad.barn[0].fødselsdato",
                              "parameterType": "ENTITY",
                              "reason": "Må være satt når 'type' er annet enn 'FRA_OPPSLAG'"
                            },
                            {
                              "invalidValue": "",
                              "parameterName": "omsorgsdagerAleneOmOmsorgenSøknad.barn[0].navn",
                              "parameterType": "ENTITY",
                              "reason": "Kan ikke være tomt eller blankt"
                            },
                            {
                              "parameterName": "omsorgsdagerAleneOmOmsorgenSøknad.barn[0].dato",
                              "parameterType": "ENTITY",
                              "reason": "Må være satt når 'tidspunktForAleneomsorg' er 'SISTE_2_ÅRENE'"
                            },
                            {
                              "invalidValue": "$forLangtNavn",
                              "parameterName": "omsorgsdagerAleneOmOmsorgenSøknad.barn[1].navn",
                              "parameterType": "ENTITY",
                              "reason": "Kan ikke være mer enn 100 tegn"
                            },
                            {
                              "parameterName": "omsorgsdagerAleneOmOmsorgenSøknad.barn[1].fødselsdato",
                              "parameterType": "ENTITY",
                              "reason": "Kan ikke være i fremtiden"
                            },
                            {
                              "invalidValue": false,
                              "parameterName": "omsorgsdagerAleneOmOmsorgenSøknad.harForståttRettigheterOgPlikter",
                              "parameterType": "ENTITY",
                              "reason": "Må ha forstått rettigheter og plikter for å sende inn søknad"
                            },
                            {
                              "invalidValue": false,
                              "parameterName": "omsorgsdagerAleneOmOmsorgenSøknad.harBekreftetOpplysninger",
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
