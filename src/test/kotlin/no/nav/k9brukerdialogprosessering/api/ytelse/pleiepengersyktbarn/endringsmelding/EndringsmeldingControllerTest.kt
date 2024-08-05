package no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.endringsmelding

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import no.nav.k9brukerdialogprosessering.api.innsending.InnsendingCache
import no.nav.k9brukerdialogprosessering.api.innsending.InnsendingService
import no.nav.k9brukerdialogprosessering.api.ytelse.MetrikkService
import no.nav.k9brukerdialogprosessering.api.ytelse.Ytelse
import no.nav.k9brukerdialogprosessering.config.JacksonConfiguration
import no.nav.k9brukerdialogprosessering.innsyn.InnsynService
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
import java.time.ZonedDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension::class)
@WebMvcTest(
    controllers = [EndringsmeldingController::class]
)
@Import(
    JacksonConfiguration::class,
    CallIdGenerator::class
)
class EndringsmeldingControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var innsendingService: InnsendingService

    @MockkBean
    private lateinit var innsynService: InnsynService

    @MockkBean
    private lateinit var innsendingCache: InnsendingCache

    @MockkBean
    private lateinit var springTokenValidationContextHolder: SpringTokenValidationContextHolder

    @MockkBean
    private lateinit var metrikkService: MetrikkService

    private companion object {
        private val søkerMedBarn = "02119970078"
        private val barnIdentitetsnummer = "18909798651"
    }

    @BeforeEach
    fun setUp() {
        springTokenValidationContextHolder.mockContext()
    }

    @Test
    fun `Endringsmelding med endringer innenfor eksisterende perioder er OK`() {
        val søknadId = UUID.randomUUID().toString()
        val mottattDato = ZonedDateTime.parse("2021-11-03T07:12:05.530Z")

        //language=json
        val endringsmelding = """
            {
             "søknadId": "$søknadId",
              "id": "123",
              "språk": "nb",
              "mottattDato": "$mottattDato",
              "harBekreftetOpplysninger": true,
              "harForståttRettigheterOgPlikter": true,
              "ytelse": {
                "type": "PLEIEPENGER_SYKT_BARN",
                "barn": {
                  "norskIdentitetsnummer": "$barnIdentitetsnummer"
                },
                "arbeidstid": {
                  "arbeidstakerList": [
                    {
                      "organisasjonsnummer": "917755736",
                      "organisasjonsnavn": "Something Fishy AS",
                      "arbeidstidInfo": {
                        "perioder": {
                          "2021-01-01/2021-01-01": {
                            "jobberNormaltTimerPerDag": "PT1H0M",
                            "faktiskArbeidTimerPerDag": "PT0H"
                          }
                        }
                      }
                    }
                  ]
                },
                "tilsynsordning": {
                  "perioder": {
                    "2021-01-01/2021-01-01": {
                      "etablertTilsynTimerPerDag": "PT2H0M"
                    }
                  }
                },
                "annetDataBruktTilUtledning": {
                    "harBekreftetOpplysninger": true,
                    "harForståttRettigheterOgPlikter": true,
                    "soknadDialogCommitSha": "abc-123",
                    "annetData": "{\"string\": \"tekst\", \"boolean\": false, \"number\": 1, \"array\": [1,2,3], \"object\": {\"key\": \"value\"}}"
                }
              }
            }
        """.trimIndent()

        mockMvc.post("/pleiepenger-sykt-barn/endringsmelding/innsending") {
            headers {
                set(NavHeaders.BRUKERDIALOG_YTELSE, Ytelse.ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN.name)
                set(NavHeaders.BRUKERDIALOG_GIT_SHA, UUID.randomUUID().toString())
            }
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
            content = endringsmelding
        }
            .andExpect {
                status { isAccepted() }
            }
    }

    @Test
    fun `Endringsmelding med feil parametere gir valideringsfeil`() {
        val søknadId = "abc123"
        val mottattDato = ZonedDateTime.parse("2021-11-03T07:12:05.530Z")

        //language=json
        val endringsmelding = """
                {
                  "søknadId": "$søknadId",
                  "språk": "nb",
                  "mottattDato": "$mottattDato",
                  "harBekreftetOpplysninger": false,
                  "harForståttRettigheterOgPlikter": false,
                  "ytelse": {
                    "type": "PLEIEPENGER_SYKT_BARN",
                    "barn": {
                      "norskIdentitetsnummer": "$barnIdentitetsnummer"
                    },
                    "arbeidstid": {
                      "arbeidstakerList": [
                        {
                          "organisasjonsnummer": "917755736",
                          "organisasjonsnavn": "",
                          "arbeidstidInfo": {
                            "perioder": {
                              "2021-01-07/2021-01-07": {
                                "jobberNormaltTimerPerDag": "PT1H0M",
                                "faktiskArbeidTimerPerDag": "PT0H"
                              }
                            }
                          }
                        }
                      ]
                    }
                  }
                }
            """.trimIndent()

        mockMvc.post("/pleiepenger-sykt-barn/endringsmelding/innsending") {
            headers {
                set(NavHeaders.BRUKERDIALOG_YTELSE, Ytelse.ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN.name)
                set(NavHeaders.BRUKERDIALOG_GIT_SHA, UUID.randomUUID().toString())
            }
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
            content = endringsmelding
        }
            .andExpect {
                status { isBadRequest() }
                header { exists(NavHeaders.PROBLEM_DETAILS) }
                content {
                    json(
                        """
                        {
                          "type": "/problem-details/invalid-request-parameters",
                          "instance": "http://localhost/pleiepenger-sykt-barn/endringsmelding/innsending",
                          "title": "invalid-request-parameters",
                          "status": 400,
                          "detail": "Forespørselen inneholder valideringsfeil",
                          "violations": [
                            {
                              "invalidValue": false,
                              "parameterName": "endringsmelding.harBekreftetOpplysninger",
                              "parameterType": "ENTITY",
                              "reason": "Opplysningene må bekreftes for å sende inn endringsmelding"
                            },
                            {
                              "invalidValue": false,
                              "parameterName": "endringsmelding.harForståttRettigheterOgPlikter",
                              "parameterType": "ENTITY",
                              "reason": "Må ha forstått rettigheter og plikter for å sende inn endringsmelding"
                            },
                            {
                              "invalidValue": "abc123",
                              "parameterName": "endringsmelding.søknadId",
                              "parameterType": "ENTITY",
                              "reason": "Forventet gyldig UUID, men var 'abc123'"
                            }
                          ]
                        }
                        """.trimIndent(), false
                    )
                }
            }

    }
}
