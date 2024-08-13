package no.nav.brukerdialog.mellomlagring.soknad

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.brukerdialog.K9brukerdialogprosesseringApplication
import no.nav.brukerdialog.ytelse.Ytelse
import no.nav.brukerdialog.utils.TokenTestUtils.hentToken
import no.nav.brukerdialog.utils.WireMockServerUtils.stubOpprettMellomlagring
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.ZonedDateTime

@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@EnableMockOAuth2Server
@SpringBootTest(
    classes = [K9brukerdialogprosesseringApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureWireMock
class K9BrukerdialogCacheServiceTest {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var wireMockServer: WireMockServer

    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

    @Autowired
    private lateinit var k9BrukerdialogCacheService: K9BrukerdialogCacheService

    @MockkBean
    lateinit var oAuth2AccessTokenService: OAuth2AccessTokenService

    @BeforeEach
    fun setUp() {
        val token =
            mockOAuth2Server.hentToken("123456789", audience = "dev-gcp:dusseldorf:k9-brukerdialog-cache").serialize()
        every { oAuth2AccessTokenService.getAccessToken(any()) } returns OAuth2AccessTokenResponse(token)
    }

    @Test
    fun `Mellomlagring av tom json gir ikke feil`() {
        val cacheRequest = CacheRequest(
            nøkkelPrefiks = "psb_123",
            verdi = "{}",
            ytelse = Ytelse.ETTERSENDING_PLEIEPENGER_SYKT_BARN,
            utløpsdato = ZonedDateTime.now().plusDays(3),
            opprettet = ZonedDateTime.now()
        )

        wireMockServer.stubOpprettMellomlagring(
            urlPathMatching = "/api/cache",
            requestBodyJson = objectMapper.writeValueAsString(cacheRequest),
            responseStatus = HttpStatus.CREATED,
            responseBodyJson = objectMapper.writeValueAsString(
                CacheResponse(
                    nøkkel = "psb_123",
                    verdi = "{}",
                    utløpsdato = cacheRequest.utløpsdato,
                    opprettet = cacheRequest.opprettet,
                    endret = null
                )
            )
        )

        k9BrukerdialogCacheService.mellomlagreSøknad(cacheRequest)
    }
}
