package no.nav.brukerdialog.ytelse.opplæringspenger.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.every
import no.nav.brukerdialog.config.JacksonConfiguration
import no.nav.brukerdialog.domenetjenester.innsending.InnsendingCache
import no.nav.brukerdialog.domenetjenester.innsending.InnsendingService
import no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag.BarnService
import no.nav.brukerdialog.metrikk.MetrikkService
import no.nav.brukerdialog.utils.CallIdGenerator
import no.nav.brukerdialog.utils.NavHeaders
import no.nav.brukerdialog.utils.TokenTestUtils.mockContext
import no.nav.brukerdialog.ytelse.Ytelse
import no.nav.brukerdialog.ytelse.opplæringspenger.utils.SøknadUtils
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.Arbeidsgiver
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.BarnRelasjon
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.NormalArbeidsdag
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid.*
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
    controllers = [OpplæringspengerSøknadController::class],
    properties = [
        "ENABLE_OPPLÆRINGSPENGER=true",
    ])
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
    private lateinit var innsendingCache: InnsendingCache

    @MockkBean
    private lateinit var barnService: BarnService

    @MockkBean
    private lateinit var springTokenValidationContextHolder: SpringTokenValidationContextHolder

    @MockkBean
    private lateinit var metrikkService: MetrikkService

    private val OLP_INNSEND_SØKNAD_URL = "/opplaeringspenger/soknad/innsending"

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

        val defaultSøknad = SøknadUtils.defaultSøknad()

        mockMvc.post(OLP_INNSEND_SØKNAD_URL) {
            headers {
                set(NavHeaders.BRUKERDIALOG_YTELSE, Ytelse.OPPLARINGSPENGER.dialog)
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
        every { innsendingCache.put(any()) } returns Unit

        val defaultSøknad = SøknadUtils.defaultSøknad()
        val fødselsdatoIFremtiden = LocalDate.now().plusDays(1)

        val ugyldigSøknad = defaultSøknad.copy(
            harForståttRettigheterOgPlikter = false,
            harBekreftetOpplysninger = false,
            barn = defaultSøknad.barn.copy(
                fødselsnummer = "123ABC", // Feil format
                navn = "", // Tomt navn
                fødselsdato = fødselsdatoIFremtiden // Fødselsdato i fremtiden er ikke gyldig

            ),
            barnRelasjon = BarnRelasjon.ANNET,
            barnRelasjonBeskrivelse = null, // Må være satt dersom barnRelasjon = ANNET
            arbeidsgivere = listOf(
                Arbeidsgiver(
                    organisasjonsnummer = "123ABC", // Feil format
                    navn = "", // Tomt navn
                    erAnsatt = false,
                    arbeidsforhold = Arbeidsforhold(
                        normalarbeidstid = NormalArbeidstid(timerPerUkeISnitt = NormalArbeidsdag),
                        arbeidIPeriode = ArbeidIPeriode(
                            type = ArbeidIPeriodeType.ARBEIDER_REDUSERT,
                            redusertArbeid = null // Kan ikke være null om type = ARBEIDER_REDUSERT
                        )
                    )
                ),
                Arbeidsgiver(
                    organisasjonsnummer = "12345678910987654321",
                    navn = "AG 1",
                    erAnsatt = false,
                    arbeidsforhold = Arbeidsforhold(
                        normalarbeidstid = NormalArbeidstid(timerPerUkeISnitt = NormalArbeidsdag),
                        arbeidIPeriode = ArbeidIPeriode(
                            type = ArbeidIPeriodeType.ARBEIDER_REDUSERT,
                            redusertArbeid = ArbeidsRedusert(
                                type = RedusertArbeidstidType.PROSENT_AV_NORMALT,
                                prosentAvNormalt = null // Kan ikke være null om type = PROSENT_AV_NORMALT
                            )
                        )
                    )
                ),
                Arbeidsgiver(
                    organisasjonsnummer = "12345678910987654321",
                    navn = "AG 2",
                    erAnsatt = false,
                    arbeidsforhold = Arbeidsforhold(
                        normalarbeidstid = NormalArbeidstid(timerPerUkeISnitt = NormalArbeidsdag),
                        arbeidIPeriode = ArbeidIPeriode(
                            type = ArbeidIPeriodeType.ARBEIDER_REDUSERT,
                            redusertArbeid = ArbeidsRedusert(
                                type = RedusertArbeidstidType.TIMER_I_SNITT_PER_UKE,
                                timerPerUke = null // Kan ikke være null om type = TIMER_I_SNITT_PER_UKE
                            )
                        )
                    )
                ),
                Arbeidsgiver(
                    organisasjonsnummer = "12345678910987654321",
                    navn = "AG 3",
                    erAnsatt = false,
                    arbeidsforhold = Arbeidsforhold(
                        normalarbeidstid = NormalArbeidstid(timerPerUkeISnitt = NormalArbeidsdag),
                        arbeidIPeriode = ArbeidIPeriode(
                            type = ArbeidIPeriodeType.ARBEIDER_REDUSERT,
                            redusertArbeid = ArbeidsRedusert(
                                type = RedusertArbeidstidType.ULIKE_UKER_TIMER,
                                arbeidsuker = null // Kan ikke være null om type = ULIKE_UKER_TIMER
                            )
                        )
                    )
                )
            )
        )

        val jsonPayload = objectMapper.writeValueAsString(ugyldigSøknad)
        mockMvc.post(OLP_INNSEND_SØKNAD_URL) {
            headers {
                set(NavHeaders.BRUKERDIALOG_YTELSE, Ytelse.OPPLARINGSPENGER.dialog)
                set(NavHeaders.BRUKERDIALOG_GIT_SHA, UUID.randomUUID().toString())
            }
            contentType = MediaType.APPLICATION_JSON
            content = jsonPayload.trimIndent()
        }
            .andExpect {
                status { isBadRequest() }
                header { exists(NavHeaders.X_CORRELATION_ID) }
                header { exists(NavHeaders.PROBLEM_DETAILS) }
                content {
                    json(
                        """
                        {
                          "type": "/problem-details/invalid-request-parameters",
                          "instance": "http://localhost/opplaeringspenger/soknad/innsending",
                          "title": "invalid-request-parameters",
                          "status": 400,
                          "detail": "Forespørselen inneholder valideringsfeil",
                          "violations": [
                            {
                              "invalidValue": "123ABC",
                              "parameterName": "barn.fødselsnummer",
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
                              "parameterName": "barnRelasjonBeskrivelse",
                              "parameterType": "ENTITY",
                              "reason": "Når 'barnRelasjon' er ANNET, kan ikke 'barnRelasjonBeskrivelse' være tom"
                            },
                            {
                              "invalidValue": false,
                              "parameterName": "harBekreftetOpplysninger",
                              "parameterType": "ENTITY",
                              "reason": "Opplysningene må bekreftes for å sende inn søknad"
                            },
                            {
                              
                              "parameterName": "arbeidsgivere[0].arbeidsforhold.arbeidIPeriode.arbeiderRedusert",
                              "parameterType": "ENTITY",
                              "reason": "Må være satt dersom type=ARBEIDER_REDUSERT"
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
                              "parameterName": "arbeidsgivere[1].arbeidsforhold.arbeidIPeriode.redusertArbeid.prosentAvNormalt",
                              "parameterType": "ENTITY",
                              "reason": "Må være satt dersom type=PROSENT_AV_NORMALT"
                            },
                            {
                              "parameterName": "arbeidsgivere[2].arbeidsforhold.arbeidIPeriode.redusertArbeid.timerPerUke",
                              "parameterType": "ENTITY",
                              "reason": "Må være satt dersom type=TIMER_I_SNITT_PER_UKE"
                            },
                            {
                              "parameterName": "arbeidsgivere[3].arbeidsforhold.arbeidIPeriode.redusertArbeid.arbeidsuker",
                              "parameterType": "ENTITY",
                              "reason": "Må være satt dersom type=ULIKE_UKER_TIMER"
                            },
                            {
                              "invalidValue": "$fødselsdatoIFremtiden",
                              "parameterName": "barn.fødselsdato",
                              "parameterType": "ENTITY",
                              "reason": "kan ikke være i fremtiden"
                            },
                            {
                              "invalidValue": "123ABC",
                              "parameterName": "barn.fødselsnummer",
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
                        """.trimIndent(),
                        false,
                    )
                }
            }
    }
}
