package no.nav.brukerdialog.ytelse.omsorgspengermidlertidigalene.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.every
import no.nav.brukerdialog.config.JacksonConfiguration
import no.nav.brukerdialog.domenetjenester.innsending.DuplikatInnsendingSjekker
import no.nav.brukerdialog.domenetjenester.innsending.InnsendingService
import no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag.BarnService
import no.nav.brukerdialog.metrikk.MetrikkService
import no.nav.brukerdialog.utils.CallIdGenerator
import no.nav.brukerdialog.utils.NavHeaders
import no.nav.brukerdialog.utils.TokenTestUtils.mockContext
import no.nav.brukerdialog.ytelse.fellesdomene.Barn
import no.nav.brukerdialog.ytelse.omsorgspengermidlertidigalene.api.domene.AnnenForelder
import no.nav.brukerdialog.ytelse.omsorgspengermidlertidigalene.api.domene.Situasjon
import no.nav.brukerdialog.ytelse.omsorgspengermidlertidigalene.utils.SøknadUtils
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.LocalDate
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension::class)
@WebMvcTest(controllers = [OmsorgspengerMidlertidigAleneController::class])
@Import(
    JacksonConfiguration::class,
    CallIdGenerator::class
)
class OmsorgspengerMidlertidigAleneControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var innsendingService: InnsendingService

    @MockkBean
    private lateinit var duplikatInnsendingSjekker: DuplikatInnsendingSjekker

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
        every { duplikatInnsendingSjekker.forsikreIkkeDuplikatInnsending(any()) } returns Unit
        coEvery { innsendingService.registrer(any(), any()) } returns Unit
        every { metrikkService.registrerMottattInnsending(any()) } returns Unit

        val defaultSøknad = SøknadUtils.defaultSøknad

        mockMvc.post("/omsorgspenger-midlertidig-alene/innsending") {
            headers {
                set(NavHeaders.BRUKERDIALOG_GIT_SHA, UUID.randomUUID().toString())
            }
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(defaultSøknad)
        }
            .andExpect {
                status { isAccepted() }
                header { exists(NavHeaders.X_CORRELATION_ID) }
            }
    }

    @Test
    fun `Innsending av søknad med feile verdier responderer med bad request`() {
        val fødselsdatoIFremtiden = LocalDate.now().plusDays(1)
        val jsonPayload = objectMapper.writeValueAsString(
            SøknadUtils.defaultSøknad.copy(
                annenForelder = AnnenForelder(
                    navn = "",
                    fnr = "123ABC",
                    situasjon = Situasjon.SYKDOM,
                    situasjonBeskrivelse = "",
                    periodeOver6Måneder = null,
                    periodeFraOgMed = LocalDate.parse("2020-01-01"),
                    periodeTilOgMed = null
                ),
                barn = listOf(
                    Barn(
                        navn = "",
                        norskIdentifikator = "123ABC",
                        aktørId = null,
                        fødselsdato = fødselsdatoIFremtiden
                    )
                ),
                harBekreftetOpplysninger = false,
                harForståttRettigheterOgPlikter = false
            )
        )
        mockMvc.post("/omsorgspenger-midlertidig-alene/innsending") {
            headers {
                set(NavHeaders.BRUKERDIALOG_GIT_SHA, UUID.randomUUID().toString())
            }
            contentType = MediaType.APPLICATION_JSON
            content = jsonPayload.trimIndent()
        }
            .andExpect {
                status { isBadRequest() }
                header { exists(NavHeaders.X_CORRELATION_ID) }
                content {
                    json(
                        """
                        {
                          "type": "/problem-details/invalid-request-parameters",
                          "instance": "http://localhost/omsorgspenger-midlertidig-alene/innsending",
                          "title": "invalid-request-parameters",
                          "status": 400,
                          "detail": "Forespørselen inneholder valideringsfeil",
                          "properties": {
                            "violations": [
                              {
                                "invalidValue": "123ABC",
                                "parameterName": "omsorgspengerMidlertidigAleneSøknad.barn[0].norskIdentifikator",
                                "parameterType": "ENTITY",
                                "reason": "size must be between 11 and 11"
                              },
                              {
                                "invalidValue": "123ABC",
                                "parameterName": "omsorgspengerMidlertidigAleneSøknad.barn[0].norskIdentifikator",
                                "parameterType": "ENTITY",
                                "reason": "'123ABC' matcher ikke tillatt pattern '^\\d+$'"
                              },
                               {
                                "parameterName": "omsorgspengerMidlertidigAleneSøknad.barn[0].fødselsdato",
                                "parameterType": "ENTITY",
                                "reason": "Kan ikke være i fremtiden"
                              },
                              {
                                "invalidValue": "",
                                "parameterName": "omsorgspengerMidlertidigAleneSøknad.barn[0].navn",
                                "parameterType": "ENTITY",
                                "reason": "Kan ikke være tomt eller blankt"
                              },
                              {
                                "invalidValue": "123ABC",
                                "parameterName": "omsorgspengerMidlertidigAleneSøknad.annenForelder.fnr",
                                "parameterType": "ENTITY",
                                "reason": "size must be between 11 and 11"
                              },
                              {
                                "invalidValue": "123ABC",
                                "parameterName": "omsorgspengerMidlertidigAleneSøknad.annenForelder.fnr",
                                "parameterType": "ENTITY",
                                "reason": "'123ABC' matcher ikke tillatt pattern '^\\d+$'"
                              },
                              {
                                "invalidValue": "",
                                "parameterName": "omsorgspengerMidlertidigAleneSøknad.annenForelder.navn",
                                "parameterType": "ENTITY",
                                "reason": "Kan ikke være tomt eller blankt"
                              },
                              {
                                "parameterName": "omsorgspengerMidlertidigAleneSøknad.annenForelder.situasjonBeskrivelse",
                                "parameterType": "ENTITY",
                                "reason": "Derom 'situasjon' er 'SYKDOM', eller 'ANNET' må 'situasjonBeskrivelse' være satt"
                              },
                              {
                                "invalidValue": false,
                                "parameterName": "omsorgspengerMidlertidigAleneSøknad.annenForelder.situasjon_innlagt_i_helseinstitusjon_sykdom_eller_annet",
                                "parameterType": "ENTITY",
                                "reason": "Derom 'situasjon' er 'INNLAGT_I_HELSEINSTITUSJON', 'SYKDOM', eller 'ANNET' må 'periodeTilOgMed' eller 'periodeOver6Måneder' være satt"
                              },
                              {
                                "invalidValue": false,
                                "parameterName": "omsorgspengerMidlertidigAleneSøknad.harForståttRettigheterOgPlikter",
                                "parameterType": "ENTITY",
                                "reason": "Må ha forstått rettigheter og plikter for å sende inn søknad"
                              },
                              {
                                "invalidValue": false,
                                "parameterName": "omsorgspengerMidlertidigAleneSøknad.harBekreftetOpplysninger",
                                "parameterType": "ENTITY",
                                "reason": "Opplysningene må bekreftes for å sende inn søknad"
                              },
                            ]
                          }
                        }
                        """.trimIndent(),
                        JsonCompareMode.LENIENT,
                    )
                }
            }
    }
}
