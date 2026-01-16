package no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import com.ninjasquad.springmockk.MockkBean
import no.nav.brukerdialog.GcsStorageTestConfiguration
import no.nav.brukerdialog.utils.Constants
import no.nav.brukerdialog.utils.MDCUtil
import no.nav.brukerdialog.utils.TokenTestUtils.mockContext
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension

@EnableMockOAuth2Server
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(GcsStorageTestConfiguration::class)
class SøkerOppslagsServiceTest {

    companion object {
        @JvmField
        @RegisterExtension
        val wireMock: WireMockExtension = WireMockExtension.newInstance()
            .build()

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("no.nav.integration.k9-selvbetjening-oppslag-base-url") { wireMock.baseUrl() }
        }
    }

    @Autowired
    lateinit var søkerenOppslagsService: SøkerOppslagsService

    @MockkBean
    private lateinit var springTokenValidationContextHolder: SpringTokenValidationContextHolder

    @BeforeEach
    fun setUp() {
        springTokenValidationContextHolder.mockContext()
        MDCUtil.toMDC(Constants.CALLER_CLIENT_ID, "pleiepengesoknad")
    }

    @Test
    fun `Forventer søker gitt OK respons`() {
        mockHentSøker(
            statusKode = HttpStatus.OK.value(),
            // language=json
            body = """
              {
                "fornavn": "Ola",
                "mellomnavn": "Nordmann",
                "etternavn": "Nordmann",
                "fødselsdato": "1990-01-01",
                "fødselsnummer": "12345678901",
                "aktør_id": "1234567890123"
              }""".trimMargin()
        )

        val søker = søkerenOppslagsService.hentSøker()
        assertThat(søker).isNotNull
    }

    private fun mockHentSøker(statusKode: Int, body: String?) {
        val responseDefinitionBuilder = WireMock.aResponse()
            .withStatus(statusKode)
            .withHeader("Content-Type", "application/json")

        body?.let { responseDefinitionBuilder.withBody(it) }

        wireMock.stubFor(
            WireMock
                .get(WireMock.urlPathEqualTo("/meg"))
                .withQueryParam("a", WireMock.equalTo("aktør_id"))
                .withQueryParam("a", WireMock.equalTo("fornavn"))
                .withQueryParam("a", WireMock.equalTo("mellomnavn"))
                .withQueryParam("a", WireMock.equalTo("etternavn"))
                .withQueryParam("a", WireMock.equalTo("fødselsdato"))
                .willReturn(responseDefinitionBuilder)
        )
    }
}
