package no.nav.brukerdialog.ytelse.opplæringspenger.api

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
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.BarnRelasjon
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid.*
import no.nav.brukerdialog.ytelse.opplæringspenger.utils.SøknadUtils
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
@WebMvcTest(
    controllers = [OpplæringspengerSøknadController::class],
    properties = [
        "ENABLE_OPPLAERINGSPENGER=true",
    ]
)
@Import(
    JacksonConfiguration::class,
    CallIdGenerator::class
)
class OpplæringspengerSøknadControllerTest {

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

    private val OLP_INNSEND_SØKNAD_URL = "/opplaringspenger/innsending"

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

        val defaultSøknad = SøknadUtils.defaultSøknad()

        mockMvc.post(OLP_INNSEND_SØKNAD_URL) {
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
        coEvery { barnService.hentBarn() } returns emptyList()
        coEvery { innsendingService.registrer(any(), any()) } answers { callOriginal() }
        coEvery { innsendingService.forsikreValidert(any()) } answers { callOriginal() }
        coEvery { innsendingService.forsikreInnloggetBrukerErSøker(any()) } returns Unit
        every { duplikatInnsendingSjekker.forsikreIkkeDuplikatInnsending(any()) } returns Unit

        val defaultSøknad = SøknadUtils.defaultSøknad()
        val fødselsdatoIFremtiden = LocalDate.now().plusDays(1)

        val ugyldigSøknad = defaultSøknad.copy(
            harForståttRettigheterOgPlikter = false,
            harBekreftetOpplysninger = false,
            barn = defaultSøknad.barn.copy(
                norskIdentifikator = "123ABC", // Feil format
                navn = "", // Tomt navn
                fødselsdato = fødselsdatoIFremtiden, // Fødselsdato i fremtiden er ikke gyldig
                relasjonTilBarnet = BarnRelasjon.ANNET,
                relasjonTilBarnetBeskrivelse = null, // Må være satt dersom barnRelasjon = ANNET

            ),
            arbeidsgivere = listOf(
                ArbeidsgiverOLP(
                    organisasjonsnummer = "123ABC", // Feil format
                    navn = "", // Tomt navn
                    erAnsatt = false,
                    arbeidsforhold = ArbeidsforholdOLP(
                        jobberNormaltTimer = 40.0,
                        arbeidIPeriode = ArbeidIPeriode(
                            jobberIPerioden = JobberIPeriodeSvar.REDUSERT,
                            enkeltdager = emptyList() // Kan ikke være tom liste
                        ),
                    )
                )
            ),
            frilans = FrilansOLP(
                harHattInntektSomFrilanser = null, // Kan ikke være null,
                startdato = LocalDate.parse("2024-07-22"),
                sluttdato = null, // Må være satt om jobberFortsattSomFrilans er false
                jobberFortsattSomFrilans = false,
                arbeidsforhold = ArbeidsforholdOLP(
                    jobberNormaltTimer = 40.0,
                    arbeidIPeriode = ArbeidIPeriode(
                        jobberIPerioden = JobberIPeriodeSvar.REDUSERT,
                        enkeltdager = emptyList() // Kan ikke være tom liste
                    )
                )
            )
        )

        val jsonPayload = objectMapper.writeValueAsString(ugyldigSøknad)
        mockMvc.post(OLP_INNSEND_SØKNAD_URL) {
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
                          "instance": "http://localhost/opplaringspenger/innsending",
                          "title": "invalid-request-parameters",
                          "status": 400,
                          "detail": "Forespørselen inneholder valideringsfeil",
                          "properties": {
                            "violations": [
                              {
                                "invalidValue": "123ABC",
                                "parameterName": "barn.norskIdentifikator",
                                "parameterType": "ENTITY",
                                "reason": "size must be between 11 and 11"
                              },
                              {
                                "invalidValue": "",
                                "parameterName": "barn.navn",
                                "parameterType": "ENTITY",
                                "reason": "kan ikke være tomt eller blankt"
                              },
                              {
                                "invalidValue": false,
                                "parameterName": "barn.relasjonTilBarnetBeskrivelse",
                                "parameterType": "ENTITY",
                                "reason": "Når 'relasjonTilBarnet' er ANNET, kan ikke 'relasjonTilBarnetBeskrivelse' være tom"
                              },
                              {
                                "invalidValue": false,
                                "parameterName": "harBekreftetOpplysninger",
                                "parameterType": "ENTITY",
                                "reason": "Opplysningene må bekreftes for å sende inn søknad"
                              },
                              {
                                "invalidValue": "123ABC",
                                "parameterName": "arbeidsgivere[0].organisasjonsnummer",
                                "parameterType": "ENTITY",
                                "reason": "'123ABC' matcher ikke tillatt pattern '^\\d+$'"
                              },
                              {
                                "invalidValue": "",
                                "parameterName": "arbeidsgivere[0].navn",
                                "parameterType": "ENTITY",
                                "reason": "navn kan ikke være tomt eller blankt"
                              },
                              {
                                "invalidValue": [],
                                "parameterName": "arbeidsgivere[0].arbeidsforhold.arbeidIPeriode.enkeltdager",
                                "parameterType": "ENTITY",
                                "reason": "Kan ikke være tom liste"
                              },
                              {
                                "invalidValue": null,
                                "parameterName": "frilans.harHattInntektSomFrilanser",
                                "parameterType": "ENTITY",
                                "reason": "Kan ikke være null"
                              },
                              {
                                "invalidValue": false,
                                "parameterName": "frilans.jobberFortsattSomFrilans",
                                "parameterType": "ENTITY",
                                "reason": "Dersom 'jobberFortsattSomFrilans' er false, må 'sluttdato' være satt"
                              },
                              {
                                "invalidValue": [],
                                "parameterName": "frilans.arbeidsforhold.arbeidIPeriode.enkeltdager",
                                "parameterType": "ENTITY",
                                "reason": "Kan ikke være tom liste"
                              },
                              {
                                "invalidValue": "$fødselsdatoIFremtiden",
                                "parameterName": "barn.fødselsdato",
                                "parameterType": "ENTITY",
                                "reason": "kan ikke være i fremtiden"
                              },
                              {
                                "invalidValue": "123ABC",
                                "parameterName": "barn.norskIdentifikator",
                                "parameterType": "ENTITY",
                                "reason": "'123ABC' matcher ikke tillatt pattern '^\\d+$'"
                              },
                              {
                                "invalidValue": false,
                                "parameterName": "harForståttRettigheterOgPlikter",
                                "parameterType": "ENTITY",
                                "reason": "Må ha forstått rettigheter og plikter for å sende inn søknad"
                              }
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
