package no.nav.brukerdialog.ytelse.aktivitetspenger.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.verify
import no.nav.brukerdialog.config.JacksonConfiguration
import no.nav.brukerdialog.domenetjenester.innsending.DuplikatInnsendingSjekker
import no.nav.brukerdialog.domenetjenester.innsending.InnsendingService
import no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag.BarnService
import no.nav.brukerdialog.integrasjon.ungbrukerdialogapi.UngBrukerdialogApiService
import no.nav.brukerdialog.metrikk.MetrikkService
import no.nav.brukerdialog.utils.CallIdGenerator
import no.nav.brukerdialog.utils.NavHeaders
import no.nav.brukerdialog.utils.TokenTestUtils.mockContext
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.soknad.*
import no.nav.brukerdialog.ytelse.aktivitetspenger.utils.SøknadUtils
import no.nav.brukerdialog.ytelse.ungdomsytelse.utils.InntektrapporteringUtils
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import no.nav.ung.brukerdialog.kontrakt.oppgaver.BrukerdialogOppgaveDto
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgavetypeDataDto
import no.nav.ung.brukerdialog.kontrakt.oppgaver.typer.inntektsrapportering.InntektsrapporteringOppgavetypeDataDto
import no.nav.ung.brukerdialog.kontrakt.soknad.TilgjengeligSøknadResponse
import no.nav.ung.brukerdialog.kontrakt.soknad.TilgjengeligSøknadType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.web.client.HttpClientErrorException
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension::class)
@WebMvcTest(
    controllers = [AktivitetspengerController::class],
)
@Import(
    JacksonConfiguration::class,
    CallIdGenerator::class,
    AktivitetspengerService::class,
)
class AktivitetspengerControllerTest {

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
    private lateinit var ungBrukerdialogApiService: UngBrukerdialogApiService

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
        mockTilgjengeligSøknad(TilgjengeligSøknadType.FØRSTEGANGSSØKNAD)
        mockRegistrerSøknadHendelse()

        sendInnSøknad(SøknadUtils.defaultSøknad)
            .andExpect {
                status { isAccepted() }
                header { exists(NavHeaders.X_CORRELATION_ID) }
            }

        verify(exactly = 1) { ungBrukerdialogApiService.registrerSøknadHendelse(any()) }
    }

    @Test
    fun `Søknad som ikke er tilgjengelig avvises uten at den publiseres`() {
        coEvery { barnService.hentBarn() } returns emptyList()
        every { duplikatInnsendingSjekker.forsikreIkkeDuplikatInnsending(any()) } returns Unit
        mockTilgjengeligSøknad(TilgjengeligSøknadType.INGEN)

        sendInnSøknad(SøknadUtils.defaultSøknad)
            .andExpect { status { isConflict() } }

        // Deltakeren skal få avslaget før noe er sendt, ikke en feilmelding for en søknad som faktisk gikk gjennom.
        coVerify(exactly = 0) { innsendingService.registrer(any(), any()) }
        verify(exactly = 0) { ungBrukerdialogApiService.registrerSøknadHendelse(any()) }
    }

    @Test
    fun `Søknadshendelse registreres først etter at søknaden er publisert`() {
        coEvery { barnService.hentBarn() } returns emptyList()
        every { duplikatInnsendingSjekker.forsikreIkkeDuplikatInnsending(any()) } returns Unit
        every { metrikkService.registrerMottattInnsending(any()) } returns Unit
        mockTilgjengeligSøknad(TilgjengeligSøknadType.FØRSTEGANGSSØKNAD)
        mockRegistrerSøknadHendelse()
        coEvery { innsendingService.registrer(any(), any()) } throws IllegalStateException("Kafka nede")

        sendInnSøknad(SøknadUtils.defaultSøknad)

        // Uten dette ville deltakeren vært permanent sperret ute fra en søknad som aldri ble sendt.
        verify(exactly = 0) { ungBrukerdialogApiService.registrerSøknadHendelse(any()) }
    }

    @Test
    fun `Innsendingen går gjennom selv om registrering av søknadshendelse gir konflikt`() {
        coEvery { barnService.hentBarn() } returns emptyList()
        every { duplikatInnsendingSjekker.forsikreIkkeDuplikatInnsending(any()) } returns Unit
        coEvery { innsendingService.registrer(any(), any()) } returns Unit
        every { metrikkService.registrerMottattInnsending(any()) } returns Unit
        mockTilgjengeligSøknad(TilgjengeligSøknadType.FØRSTEGANGSSØKNAD)
        every { ungBrukerdialogApiService.registrerSøknadHendelse(any()) } throws
            HttpClientErrorException.create(
                HttpStatus.CONFLICT, "Conflict", HttpHeaders.EMPTY, ByteArray(0), null
            )

        // Søknaden er allerede publisert, så deltakeren skal ikke få feilmelding.
        sendInnSøknad(SøknadUtils.defaultSøknad)
            .andExpect { status { isAccepted() } }
    }

    @Test
    fun `Innsending av inntekt er OK`() {
        coEvery { barnService.hentBarn() } returns emptyList()
        every { duplikatInnsendingSjekker.forsikreIkkeDuplikatInnsending(any()) } returns Unit
        coEvery { innsendingService.registrer(any(), any()) } returns Unit
        every { metrikkService.registrerMottattInnsending(any()) } returns Unit
        mockHentingAvOppgave(
            oppgavetype = OppgaveType.RAPPORTER_INNTEKT,
            oppgavetypeData = InntektsrapporteringOppgavetypeDataDto(
                LocalDate.now(),
                LocalDate.now(),
                true
            )
        )

        mockMarkerOppgaveSomLøst()

        val defaultInntektsrapportering = InntektrapporteringUtils.defaultInntektsrapportering

        mockMvc.post("/aktivitetspenger/inntektsrapportering/innsending") {
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
        val defaultSøknad: Aktivitetspengersøknad = SøknadUtils.defaultSøknad

        val jsonPayload = objectMapper.writeValueAsString(
            defaultSøknad.copy(
                kontonummerInfo = KontonummerInfo(
                    harKontonummer = HarKontonummer.JA,
                    kontonummerErRiktig = null,
                    kontonummerFraRegister = null
                ),
                harForståttRettigheterOgPlikter = false,
                harBekreftetOpplysninger = false,
                forutgåendeBosteder = ForutgåendeBosteder(
                    true,
                    listOf(Bosted(
                        LocalDate.now(),
                        LocalDate.now(),
                        landkode = "ÆÆÆ",
                        landnavn = "Finnes ikke")
                    )
                )
            )
        )
        mockMvc.post("/aktivitetspenger/soknad/innsending") {
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
                          "instance": "http://localhost/aktivitetspenger/soknad/innsending",
                          "title": "invalid-request-parameters",
                          "status": 400,
                          "detail": "Forespørselen inneholder valideringsfeil",
                          "violations": [
                            {
                              "invalidValue": "ÆÆÆ",
                              "parameterName": "aktivitetspengersøknad.forutgåendeBosteder.utenlandsoppholdSiste5År[0].landkode",
                              "parameterType": "ENTITY",
                              "reason": "ÆÆÆ er ikke en gyldig ISO 3166-1 alpha-3 kode"
                            },
                            {
                              "invalidValue": false,
                              "parameterName": "aktivitetspengersøknad.harForståttRettigheterOgPlikter",
                              "parameterType": "ENTITY",
                              "reason": "Må ha forstått rettigheter og plikter for å sende inn søknad"
                            },
                            {
                              "invalidValue": false,
                              "parameterName": "aktivitetspengersøknad.harBekreftetOpplysninger",
                              "parameterType": "ENTITY",
                              "reason": "Opplysningene må bekreftes for å sende inn søknad"
                            },
                            {
                              "invalidValue": false,
                              "parameterName": "aktivitetspengersøknad.kontonummerInfo.kontonummerErRiktigSattNårHarJa",
                              "parameterType": "ENTITY",
                              "reason": "Dersom harKontonummer=JA må kontonummerErRiktig være satt"
                            },
                            {
                              "invalidValue": false,
                              "parameterName": "aktivitetspengersøknad.kontonummerInfo.kontonummerFraRegisterSattNårHarJa",
                              "parameterType": "ENTITY",
                              "reason": "Dersom harKontonummer=JA må kontonummerFraRegister være satt"
                            }
                          ]
                        }
                        """.trimIndent(),
                        JsonCompareMode.LENIENT,
                    )
                }
            }
    }

    private fun sendInnSøknad(søknad: Aktivitetspengersøknad) =
        mockMvc.post("/aktivitetspenger/soknad/innsending") {
            headers { set(NavHeaders.BRUKERDIALOG_GIT_SHA, UUID.randomUUID().toString()) }
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(søknad)
        }

    private fun mockRegistrerSøknadHendelse() {
        every { ungBrukerdialogApiService.registrerSøknadHendelse(any()) } returns Unit
    }

    private fun mockTilgjengeligSøknad(type: TilgjengeligSøknadType) {
        every { ungBrukerdialogApiService.hentTilgjengeligSøknad() } returns
                TilgjengeligSøknadResponse(type, false, false)
    }

    private fun mockHentingAvOppgave(oppgavetype: OppgaveType, oppgavetypeData: OppgavetypeDataDto) {
        every { ungBrukerdialogApiService.hentOppgave(any()) } returns BrukerdialogOppgaveDto(
            UUID.randomUUID(),
            oppgavetype,
            oppgavetypeData,
            OppgaveYtelsetype.AKTIVITETSPENGER,
            null,
            no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveStatus.ULØST,
            ZonedDateTime.now(),
            ZonedDateTime.now(),
            null
        )
    }

    private fun mockMarkerOppgaveSomLøst() {
        every { ungBrukerdialogApiService.markerOppgaveSomLøst(any(), any()) } returns BrukerdialogOppgaveDto(
            UUID.randomUUID(),
            OppgaveType.RAPPORTER_INNTEKT,
            InntektsrapporteringOppgavetypeDataDto(
                LocalDate.now(),
                LocalDate.now(),
                true
            ),
            OppgaveYtelsetype.AKTIVITETSPENGER,
            null,
            no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveStatus.ULØST,
            ZonedDateTime.now(),
            ZonedDateTime.now(),
            null
        )
    }
}
