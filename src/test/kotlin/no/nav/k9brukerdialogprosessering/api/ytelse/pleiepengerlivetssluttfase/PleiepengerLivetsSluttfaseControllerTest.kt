package no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import no.nav.k9brukerdialogprosessering.api.innsending.InnsendingCache
import no.nav.k9brukerdialogprosessering.api.innsending.InnsendingService
import no.nav.k9brukerdialogprosessering.api.ytelse.MetrikkService
import no.nav.k9brukerdialogprosessering.api.ytelse.Ytelse
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase.domene.ArbeidIPeriode
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase.domene.Arbeidsforhold
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase.domene.Arbeidsgiver
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase.domene.Frilans
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase.domene.JobberIPeriodeSvar
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase.domene.PleiepengerILivetsSluttfaseSøknad
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
    controllers = [PleiepengerLivetsSluttfaseController::class]
)
@Import(
    JacksonConfiguration::class,
    CallIdGenerator::class
)
class PleiepengerLivetsSluttfaseControllerTest {

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
        val defaultSøknad = SøknadUtils.defaultSøknad

        mockMvc.post("/pleiepenger-livets-sluttfase/innsending") {
            headers {
                set(NavHeaders.BRUKERDIALOG_YTELSE, Ytelse.PLEIEPENGER_LIVETS_SLUTTFASE.name)
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
        val defaultSøknad: PleiepengerILivetsSluttfaseSøknad =
            no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase.SøknadUtils.defaultSøknad

        val fødselsdatoIFremtiden = LocalDate.now().plusDays(1)
        val jsonPayload = objectMapper.writeValueAsString(
            defaultSøknad.copy(
                harForståttRettigheterOgPlikter = false,
                harBekreftetOpplysninger = false,
                pleietrengende = defaultSøknad.pleietrengende.copy(
                    norskIdentitetsnummer = "123ABC", // Feil format
                    navn = "", // Tomt navn
                    fødselsdato = fødselsdatoIFremtiden // Fødselsdato i fremtiden er ikke gyldig

                ),
                arbeidsgivere = listOf(
                    Arbeidsgiver(
                        organisasjonsnummer = "123ABC", // Feil format
                        navn = "", // Tomt navn
                        erAnsatt = false,
                        arbeidsforhold = Arbeidsforhold(
                            jobberNormaltTimer = 40.0,
                            arbeidIPeriode = ArbeidIPeriode(
                                jobberIPerioden = JobberIPeriodeSvar.REDUSERT,
                                enkeltdager = emptyList() // Kan ikke være tom liste
                            ),
                        )
                    )
                ),
                frilans = Frilans(
                    harHattInntektSomFrilanser = null, // Kan ikke være null,
                    startdato = LocalDate.parse("2024-07-22"),
                    sluttdato = null, // Må være satt om jobberFortsattSomFrilans er false
                    jobberFortsattSomFrilans = false,
                    arbeidsforhold = Arbeidsforhold(
                        jobberNormaltTimer = 40.0,
                        arbeidIPeriode = ArbeidIPeriode(
                            jobberIPerioden = JobberIPeriodeSvar.REDUSERT,
                            enkeltdager = emptyList() // Kan ikke være tom liste
                        )
                    )
                )
            )
        )
        mockMvc.post("/pleiepenger-livets-sluttfase/innsending") {
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
                          "instance": "http://localhost/pleiepenger-livets-sluttfase/innsending",
                          "title": "invalid-request-parameters",
                          "status": 400,
                          "detail": "Forespørselen inneholder valideringsfeil",
                          "violations": [
                            {
                              "invalidValue": "123ABC",
                              "parameterName": "pleiepengerILivetsSluttfaseSøknad.pleietrengende.norskIdentitetsnummer",
                              "parameterType": "ENTITY",
                              "reason": "size must be between 11 and 11"
                            },
                            {
                              "parameterName": "pleiepengerILivetsSluttfaseSøknad.pleietrengende.fødselsdato",
                              "parameterType": "ENTITY",
                              "reason": "Kan ikke være i fremtiden"
                            },
                            {
                              "invalidValue": "123ABC",
                              "parameterName": "pleiepengerILivetsSluttfaseSøknad.pleietrengende.norskIdentitetsnummer",
                              "parameterType": "ENTITY",
                              "reason": "'123ABC' matcher ikke tillatt pattern '^\\d+$'"
                            },
                            {
                              "invalidValue": "",
                              "parameterName": "pleiepengerILivetsSluttfaseSøknad.pleietrengende.navn",
                              "parameterType": "ENTITY",
                              "reason": "Kan ikke være tomt eller blankt"
                            },
                            {
                              "invalidValue": "123ABC",
                              "parameterName": "pleiepengerILivetsSluttfaseSøknad.arbeidsgivere[0].organisasjonsnummer",
                              "parameterType": "ENTITY",
                              "reason": "'123ABC' matcher ikke tillatt pattern '^\\d+$'"
                            },
                            {
                              "invalidValue": "",
                              "parameterName": "pleiepengerILivetsSluttfaseSøknad.arbeidsgivere[0].navn",
                              "parameterType": "ENTITY",
                              "reason": "Kan ikke være tomt eller blankt"
                            },
                            {
                              "invalidValue": [],
                              "parameterName": "pleiepengerILivetsSluttfaseSøknad.arbeidsgivere[0].arbeidsforhold.arbeidIPeriode.enkeltdager",
                              "parameterType": "ENTITY",
                              "reason": "Kan ikke være tom liste"
                            },
                            {
                              "invalidValue": null,
                              "parameterName": "pleiepengerILivetsSluttfaseSøknad.frilans.harHattInntektSomFrilanser",
                              "parameterType": "ENTITY",
                              "reason": "Kan ikke være null"
                            },
                            {
                              "invalidValue": false,
                              "parameterName": "pleiepengerILivetsSluttfaseSøknad.frilans.jobberFortsattSomFrilans",
                              "parameterType": "ENTITY",
                              "reason": "Dersom 'jobberFortsattSomFrilans' er false, må 'sluttdato' være satt"
                            },
                            {
                              "invalidValue": [],
                              "parameterName": "pleiepengerILivetsSluttfaseSøknad.frilans.arbeidsforhold.arbeidIPeriode.enkeltdager",
                              "parameterType": "ENTITY",
                              "reason": "Kan ikke være tom liste"
                            },
                            {
                              "invalidValue": false,
                              "parameterName": "pleiepengerILivetsSluttfaseSøknad.harForståttRettigheterOgPlikter",
                              "parameterType": "ENTITY",
                              "reason": "Må ha forstått rettigheter og plikter for å sende inn søknad"
                            },
                            {
                              "invalidValue": false,
                              "parameterName": "pleiepengerILivetsSluttfaseSøknad.harBekreftetOpplysninger",
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
