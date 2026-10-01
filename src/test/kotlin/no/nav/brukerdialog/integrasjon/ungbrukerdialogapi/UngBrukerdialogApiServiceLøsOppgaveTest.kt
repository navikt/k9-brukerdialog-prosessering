package no.nav.brukerdialog.integrasjon.ungbrukerdialogapi

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
import no.nav.ung.brukerdialog.kontrakt.oppgaver.LøsOppgaveRequest
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveStatus
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveType
import no.nav.ung.brukerdialog.kontrakt.oppgaver.OppgaveYtelsetype
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
class UngBrukerdialogApiServiceLøsOppgaveTest {

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
        private val løsUrl = "/ung/brukerdialog/ekstern/api/oppgave/$oppgaveReferanse/l%C3%B8s"
        private val hentUrl = "/ung/brukerdialog/ekstern/api/oppgave/$oppgaveReferanse"
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
    fun `409 på løs når oppgaven allerede er LØST regnes som vellykket`() {
        stubLøs(409)
        stubHent(OppgaveStatus.LØST)

        val resultat = ungBrukerdialogApiService.markerOppgaveSomLøst(oppgaveReferanse, LøsOppgaveRequest(null))

        assertThat(resultat.status()).isEqualTo(OppgaveStatus.LØST)
        wireMock.verify(1, WireMock.postRequestedFor(WireMock.urlEqualTo(løsUrl)))
    }

    @Test
    fun `409 på løs når oppgaven ikke er LØST feiler`() {
        stubLøs(409)
        stubHent(OppgaveStatus.UTLØPT)

        assertThatThrownBy { ungBrukerdialogApiService.markerOppgaveSomLøst(oppgaveReferanse, LøsOppgaveRequest(null)) }
            .isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `200 på løs returnerer oppgaven uten å hente den på nytt`() {
        stubLøs(200, OppgaveStatus.LØST)

        val resultat = ungBrukerdialogApiService.markerOppgaveSomLøst(oppgaveReferanse, LøsOppgaveRequest(null))

        assertThat(resultat.status()).isEqualTo(OppgaveStatus.LØST)
        wireMock.verify(0, WireMock.getRequestedFor(WireMock.urlEqualTo(hentUrl)))
    }

    private fun stubLøs(status: Int, oppgaveStatus: OppgaveStatus? = null) {
        val respons = WireMock.aResponse().withStatus(status).withHeader("Content-Type", "application/json")
        if (oppgaveStatus != null) respons.withBody(jsonMapper.writeValueAsString(oppgave(oppgaveStatus)))
        else respons.withBody("""{"feilmelding":"Ugyldig statusendring","feilkode":null,"type":"GENERELL_FEIL"}""")
        wireMock.stubFor(WireMock.post(WireMock.urlEqualTo(løsUrl)).willReturn(respons))
    }

    private fun stubHent(status: OppgaveStatus) {
        wireMock.stubFor(
            WireMock.get(WireMock.urlEqualTo(hentUrl)).willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(jsonMapper.writeValueAsString(oppgave(status)))
            )
        )
    }

    private fun oppgave(status: OppgaveStatus) = BrukerdialogOppgaveDto(
        oppgaveReferanse,
        OppgaveType.BEKREFT_ENDRET_STARTDATO,
        null,
        OppgaveYtelsetype.UNGDOMSYTELSE,
        null,
        status,
        ZonedDateTime.parse("2025-01-15T10:30:00Z"),
        null,
        ZonedDateTime.parse("2025-02-15T10:30:00Z")
    )
}
