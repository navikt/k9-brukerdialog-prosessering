package no.nav.k9brukerdialogprosessering.api.ytelse.omsorgspengerutbetalingsnf

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import no.nav.k9brukerdialogprosessering.api.innsending.InnsendingCache
import no.nav.k9brukerdialogprosessering.api.innsending.InnsendingService
import no.nav.k9brukerdialogprosessering.api.ytelse.MetrikkService
import no.nav.k9brukerdialogprosessering.api.ytelse.Ytelse
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Bekreftelser
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Næringstype
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Virksomhet
import no.nav.k9brukerdialogprosessering.api.ytelse.omsorgspengerutbetalingsnf.domene.Barn
import no.nav.k9brukerdialogprosessering.api.ytelse.omsorgspengerutbetalingsnf.domene.Frilans
import no.nav.k9brukerdialogprosessering.api.ytelse.omsorgspengerutbetalingsnf.domene.TypeBarn
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
    controllers = [OmsorgspengerUtbetalingSnfController::class]
)
@Import(
    JacksonConfiguration::class,
    CallIdGenerator::class
)
class OmsorgspengerUtbetalingSnfControllerTest {

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

        mockMvc.post("/omsorgspenger-utbetaling-snf/innsending") {
            headers {
                set(NavHeaders.BRUKERDIALOG_YTELSE, Ytelse.OMSORGSPENGER_UTBETALING_SNF.name)
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
        val fødselsdatoIFremtiden = LocalDate.now().plusDays(1)
        val jsonPayload = objectMapper.writeValueAsString(
            SøknadUtils.defaultSøknad.copy(
                barn = listOf(
                    Barn(
                        navn = "",
                        fødselsdato = fødselsdatoIFremtiden,
                        type = TypeBarn.FRA_OPPSLAG,
                        identitetsnummer = "123ABC"
                    )
                ),
                frilans = Frilans(
                    startdato = LocalDate.parse("2022-02-01"),
                    sluttdato = LocalDate.parse("2022-01-01"),
                    jobberFortsattSomFrilans = true
                ),
                selvstendigNæringsdrivende = Virksomhet(
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2022-10-01"),
                    næringstype = Næringstype.DAGMAMMA,
                    navnPåVirksomheten = "Kiwi ASA",
                    organisasjonsnummer = "123ABC",
                    erNyoppstartet = true,
                    registrertINorge = true,
                    harFlereAktiveVirksomheter = false
                ),
                bekreftelser = Bekreftelser(
                    harBekreftetOpplysninger = false,
                    harForståttRettigheterOgPlikter = false
                ),
            )
        )
        mockMvc.post("/omsorgspenger-utbetaling-snf/innsending") {
            headers {
                set(NavHeaders.BRUKERDIALOG_YTELSE, Ytelse.OMSORGSPENGER_UTBETALING_SNF.name)
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
                          "instance": "http://localhost/omsorgspenger-utbetaling-snf/innsending",
                          "title": "invalid-request-parameters",
                          "status": 400,
                          "detail": "Forespørselen inneholder valideringsfeil",
                          "violations": [
                            {
                              "parameterName": "omsorgspengerutbetalingSnfSøknad.barn[0].identitetsnummer",
                              "parameterType": "ENTITY",
                              "reason": "size must be between 11 and 11"
                            },
                            {
                              "parameterName": "omsorgspengerutbetalingSnfSøknad.barn[0].identitetsnummer",
                              "parameterType": "ENTITY",
                              "reason": "'123ABC' matcher ikke tillatt pattern '^\\d+$'"
                            },
                             {
                              "parameterName": "omsorgspengerutbetalingSnfSøknad.barn[0].fødselsdato",
                              "parameterType": "ENTITY",
                              "reason": "Kan ikke være i fremtiden"
                            },
                            {
                              "invalidValue": "",
                              "parameterName": "omsorgspengerutbetalingSnfSøknad.barn[0].navn",
                              "parameterType": "ENTITY",
                              "reason": "Kan ikke være tomt eller blankt"
                            },
                            {
                              "invalidValue": false,
                              "parameterName": "omsorgspengerutbetalingSnfSøknad.frilans.sluttdato",
                              "parameterType": "ENTITY",
                              "reason": "Dersom 'jobberFortsattSomFrilans' er true, kan ikke 'sluttdato' være satt"
                            },
                            {
                              "invalidValue": false,
                              "parameterName": "omsorgspengerutbetalingSnfSøknad.frilans.startdato",
                              "parameterType": "ENTITY",
                              "reason": "'Sluttdato' må være lik eller etter 'startdato'"
                            },
                            {
                              "invalidValue": "123ABC",
                              "parameterName": "omsorgspengerutbetalingSnfSøknad.selvstendigNæringsdrivende.organisasjonsnummer",
                              "parameterType": "ENTITY",
                              "reason": "'123ABC' matcher ikke tillatt pattern '^\\d+$'"
                            },
                            {
                              "invalidValue": false,
                              "parameterName": "omsorgspengerutbetalingSnfSøknad.bekreftelser.harBekreftetOpplysninger",
                              "parameterType": "ENTITY",
                              "reason": "Opplysningene må bekreftes for å sende inn søknad"
                            },
                            {
                              "invalidValue": false,
                              "parameterName": "omsorgspengerutbetalingSnfSøknad.bekreftelser.harForståttRettigheterOgPlikter",
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
