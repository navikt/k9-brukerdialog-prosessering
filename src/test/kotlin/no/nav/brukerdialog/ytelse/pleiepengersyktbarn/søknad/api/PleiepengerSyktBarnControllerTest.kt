package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api

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
import no.nav.brukerdialog.utils.SøknadUtils.Companion.metadata
import no.nav.brukerdialog.utils.TokenTestUtils.mockContext
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.Arbeidsgiver
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.BarnRelasjon
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.Nattevåk
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.Nattevåk.Companion.MAX_FRITEKST_TEGN
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.NormalArbeidsdag
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.arbeid.ArbeidIPeriode
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.arbeid.ArbeidIPeriodeType
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.arbeid.ArbeidsRedusert
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.arbeid.Arbeidsforhold
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.arbeid.NormalArbeidstid
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.api.domene.arbeid.RedusertArbeidstidType
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.utils.SøknadUtils
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
@WebMvcTest(controllers = [PleiepengerSyktBarnController::class])
@Import(
    JacksonConfiguration::class,
    CallIdGenerator::class
)
class PleiepengerSyktBarnControllerTest {

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

        val defaultSøknad = SøknadUtils.defaultSøknad()

        mockMvc.post("/pleiepenger-sykt-barn/innsending") {
            headers {
                set(NavHeaders.BRUKERDIALOG_YTELSE, Ytelse.PLEIEPENGER_SYKT_BARN.dialog)
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
            ),
            nattevåk = Nattevåk(
                harNattevåk = true,
                tilleggsinformasjon = """
                        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean consectetur nisi at velit tempor, 
                        sed sagittis ex aliquet. Duis nec ante a magna vehicula egestas. Integer malesuada augue nec purus condimentum, 
                        ut dictum magna vestibulum. Nulla vel dolor ut metus blandit lacinia. Maecenas bibendum feugiat turpis, 
                        in congue erat. Etiam facilisis, eros nec egestas condimentum, lacus lacus convallis nulla, ut 
                        varius velit elit non libero. Nulla facilisi. Fusce dapibus magna vitae magna tempor, at aliquet
                        massa eleifend. Morbi fermentum nibh nisi, non cursus felis convallis non. Vivamus sit amet massa 
                        at erat dictum suscipit a at urna. Cras mollis metus sed erat fermentum, sed efficitur orci tincidunt. 
                        Sed non varius est, sit amet porttitor lorem. Nulla malesuada justo id justo ultrices, ut vehicula lorem interdum. 
                        Phasellus sed risus vel turpis cursus volutpat. Sed dapibus sapien in nisl venenatis, sit amet ultrices libero convallis. 
                        Donec fringilla felis et risus finibus, vitae vehicula lorem consectetur. Ut at velit in purus malesuada porttitor. 
                        Duis non dui purus. Integer placerat nec sapien in volutpat. Morbi vitae eros nec eros scelerisque aliquet vel ut nulla. 
                        Nullam vulputate sapien sit amet mi porttitor lacinia. Etiam laoreet augue nec vestibulum accumsan.
                        Nullam vehicula elit id tortor ornare, sit amet aliquam sapien tempus. Nam commodo, risus eget vulputate suscipit, 
                        lectus libero tempor purus, quis sollicitudin sapien nunc eget ligula. Curabitur nec scelerisque nisl. 
                        In fermentum ligula non est auctor tincidunt. Morbi aliquam ex non volutpat fermentum.
                    """.trimIndent() // Over maks tegn
            )
        )

        val jsonPayload = objectMapper.writeValueAsString(ugyldigSøknad)
        mockMvc.post("/pleiepenger-sykt-barn/innsending") {
            headers {
                set(NavHeaders.BRUKERDIALOG_YTELSE, Ytelse.PLEIEPENGER_SYKT_BARN.dialog)
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
                          "instance": "http://localhost/pleiepenger-sykt-barn/innsending",
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
                              "parameterName": "nattevåk.tilleggsinformasjon_lengde",
                              "parameterType": "ENTITY",
                              "reason": "Kan ikke være over $MAX_FRITEKST_TEGN tegn"
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
