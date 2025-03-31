package no.nav.brukerdialog.ytelse.ungdomsytelse.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.every
import no.nav.brukerdialog.config.JacksonConfiguration
import no.nav.brukerdialog.domenetjenester.innsending.DuplikatInnsendingSjekker
import no.nav.brukerdialog.domenetjenester.innsending.InnsendingService
import no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag.BarnService
import no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser.EndretStartdatoOppgavetypeDataDTO
import no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser.OppgaveDTO
import no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser.OppgaveStatus
import no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser.Oppgavetype
import no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser.UngDeltakelseOpplyserService
import no.nav.brukerdialog.metrikk.MetrikkService
import no.nav.brukerdialog.utils.CallIdGenerator
import no.nav.brukerdialog.utils.NavHeaders
import no.nav.brukerdialog.utils.TokenTestUtils.mockContext
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.BekreftelseSvar
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse.EndretStartdatoUngdomsytelseOppgaveDTO
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.soknad.Ungdomsytelsesøknad
import no.nav.brukerdialog.ytelse.ungdomsytelse.utils.InntektrapporteringUtils
import no.nav.brukerdialog.ytelse.ungdomsytelse.utils.SøknadUtils
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
import java.time.ZonedDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension::class)
@WebMvcTest(
    controllers = [UngdomsytelseController::class],
    properties = [
        "ENABLE_UNDOMSYTELSE=true",
    ])
@Import(
    JacksonConfiguration::class,
    CallIdGenerator::class,
    UngdomsytelseService::class,
)
class UngdomsytelseControllerTest {

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

    @MockkBean
    private lateinit var ungDeltakelseOpplyserService: UngDeltakelseOpplyserService

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

        mockMvc.post("/ungdomsytelse/soknad/innsending") {
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
    fun `Innsending av inntekt er OK`() {
        coEvery { barnService.hentBarn() } returns emptyList()
        every { duplikatInnsendingSjekker.forsikreIkkeDuplikatInnsending(any()) } returns Unit
        coEvery { innsendingService.registrer(any(), any()) } returns Unit
        every { metrikkService.registrerMottattInnsending(any()) } returns Unit

        val defaultInntektsrapportering = InntektrapporteringUtils.defaultInntektsrapportering

        mockMvc.post("/ungdomsytelse/inntektsrapportering/innsending") {
            headers {
                set(NavHeaders.BRUKERDIALOG_GIT_SHA, UUID.randomUUID().toString())
            }
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(defaultInntektsrapportering)
        }
            .andExpect {
                status { isAccepted() }
                header { exists(NavHeaders.X_CORRELATION_ID) }
            }
    }

    @Test
    fun `Innsending av søknad med feile verdier responderer med bad request`() {
        val defaultSøknad: Ungdomsytelsesøknad = SøknadUtils.defaultSøknad

        val jsonPayload = objectMapper.writeValueAsString(
            defaultSøknad.copy(
                harForståttRettigheterOgPlikter = false,
                harBekreftetOpplysninger = false,
            )
        )
        mockMvc.post("/ungdomsytelse/soknad/innsending") {
            headers {
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
                          "instance": "http://localhost/ungdomsytelse/soknad/innsending",
                          "title": "invalid-request-parameters",
                          "status": 400,
                          "detail": "Forespørselen inneholder valideringsfeil",
                          "violations": [
                            {
                              "invalidValue": false,
                              "parameterName": "ungdomsytelsesøknad.harForståttRettigheterOgPlikter",
                              "parameterType": "ENTITY",
                              "reason": "Må ha forstått rettigheter og plikter for å sende inn søknad"
                            },
                            {
                              "invalidValue": false,
                              "parameterName": "ungdomsytelsesøknad.harBekreftetOpplysninger",
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

    @Test
    fun `Innsending av oppgavebekreftelse er OK`() {
        coEvery { barnService.hentBarn() } returns emptyList()
        every { duplikatInnsendingSjekker.forsikreIkkeDuplikatInnsending(any()) } returns Unit
        coEvery { innsendingService.registrer(any(), any()) } returns Unit
        every { metrikkService.registrerMottattInnsending(any()) } returns Unit
        every { ungDeltakelseOpplyserService.hentOppgaveForDeltakelse(any(), any()) } returns OppgaveDTO(
            oppgaveReferanse = UUID.randomUUID(),
            oppgavetype = Oppgavetype.BEKREFT_ENDRET_STARTDATO,
            oppgavetypeData = EndretStartdatoOppgavetypeDataDTO(
                nyStartdato = LocalDate.now(),
                veilederRef = "veileder-123",
                meldingFraVeileder = "Hei, jeg har endret startdatoen som vi avtalte i møtet. Fra: Pål Hønesen."
            ),
            status = OppgaveStatus.ULØST,
            opprettetDato = ZonedDateTime.now(),
            løstDato = null
        )

        val defaultOppgavebekreftelse = SøknadUtils.defaultOppgavebekreftelse

        mockMvc.post("/ungdomsytelse/oppgavebekreftelse/innsending") {
            headers {
                set(NavHeaders.BRUKERDIALOG_GIT_SHA, UUID.randomUUID().toString())
            }
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(defaultOppgavebekreftelse)
        }
            .andExpect {
                status { isAccepted() }
                header { exists(NavHeaders.X_CORRELATION_ID) }
            }
    }

    @Test
    fun `Innsending av oppgavebekreftelse med feile verdier responderer med bad request`() {
        every { duplikatInnsendingSjekker.forsikreIkkeDuplikatInnsending(any()) } returns Unit

        val defaultOppgavebekreftelse = SøknadUtils.defaultOppgavebekreftelse

        val jsonPayload = objectMapper.writeValueAsString(
            defaultOppgavebekreftelse.copy(
                oppgave = EndretStartdatoUngdomsytelseOppgaveDTO(
                    oppgaveReferanse = "123",
                    bekreftelseSvar = BekreftelseSvar.AVSLÅR,
                    ikkeGodkjentResponse = null,
                )
            )
        )
        mockMvc.post("/ungdomsytelse/oppgavebekreftelse/innsending") {
            headers {
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
                          "detail": "Forespørselen inneholder valideringsfeil",
                          "instance": "http://localhost/ungdomsytelse/oppgavebekreftelse/innsending",
                          "properties": null,
                          "status": 400,
                          "title": "invalid-request-parameters",
                          "type": "/problem-details/invalid-request-parameters",
                          "violations": [
                            {
                              "invalidValue": "123",
                              "parameterName": "ungdomsytelseOppgavebekreftelse.oppgave.oppgaveReferanse",
                              "parameterType": "ENTITY",
                              "reason": "Forventet gyldig UUID, men var '123'"
                            },
                            {
                              "invalidValue": false,
                              "parameterName": "ungdomsytelseOppgavebekreftelse.oppgave.ikkeGodkjentResponseValid",
                              "parameterType": "ENTITY",
                              "reason": "Ikke godkjent respons må være satt hvis bekreftelseSvar er AVSLÅR"
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
