package no.nav.brukerdialog.integrasjon.ungbrukerdialogapi

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.brukerdialog.GcsStorageTestConfiguration
import no.nav.brukerdialog.utils.TokenTestUtils.hentToken
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.ung.brukerdialog.kontrakt.oppgaver.BrukerdialogOppgaveDto
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveStatus
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import tools.jackson.databind.json.JsonMapper
import java.time.ZonedDateTime
import java.util.*

@EnableMockOAuth2Server
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(GcsStorageTestConfiguration::class)
class UngBrukerdialogApiServiceJacksonTest {

    companion object {
        @JvmField
        @RegisterExtension
        val wireMock: WireMockExtension = WireMockExtension.newInstance().build()

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("no.nav.integration.ung-brukerdialog-api-base-url") { wireMock.baseUrl() }
        }

        private val oppgaveReferanse = UUID.randomUUID()

        /** ObjectMapper som matcher ung-brukerdialog-api sin Jackson-konfigurasjon (ObjectMapperFactory). */
        val ungBrukerdialogApiObjectMapper: ObjectMapper = ObjectMapper().apply {
            registerModule(Jdk8Module())
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }

    @Autowired
    private lateinit var jsonMapper: JsonMapper

    @Autowired
    private lateinit var ungBrukerdialogApiService: UngBrukerdialogApiService

    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

    @MockkBean
    lateinit var oAuth2AccessTokenService: OAuth2AccessTokenService

    @BeforeEach
    fun setUp() {
        val token = mockOAuth2Server.hentToken(audience = "ung-brukerdialog-api").serialize()
        every { oAuth2AccessTokenService.getAccessToken(any()) } returns OAuth2AccessTokenResponse(token)
    }


    @Test
    fun `klient skal deserialisere BrukerdialogOppgaveDto serialisert med config fra ung-brukerdialog-api`() {
        stubHentOppgave(ungBrukerdialogApiObjectMapper.writeValueAsString(defaultOppgave()))

        val result = ungBrukerdialogApiService.hentOppgave(oppgaveReferanse)
        assertThat(result).isNotNull
        assertThat(result.oppgaveReferanse()).isEqualTo(oppgaveReferanse)
        assertThat(result.opprettetDato()).isNotNull
    }

    private fun defaultOppgave() = BrukerdialogOppgaveDto(
        oppgaveReferanse,
        OppgaveType.BEKREFT_ENDRET_STARTDATO,
        null,
        null,
        OppgaveStatus.ULØST,
        ZonedDateTime.parse("2025-01-15T10:30:00Z"),
        null,
        ZonedDateTime.parse("2025-02-15T10:30:00Z")
    )

    private fun stubHentOppgave(body: String) {
        wireMock.stubFor(
            WireMock.get(WireMock.urlPathMatching("/ung/brukerdialog/ekstern/api/oppgave/.*"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)
                )
        )
    }
}
