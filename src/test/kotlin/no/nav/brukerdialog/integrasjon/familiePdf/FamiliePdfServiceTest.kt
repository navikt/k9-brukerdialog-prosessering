package no.nav.brukerdialog.integrasjon.familiePdf

import com.github.tomakehurst.wiremock.WireMockServer
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import kotlinx.coroutines.runBlocking
import no.nav.brukerdialog.K9brukerdialogprosesseringApplication
import no.nav.brukerdialog.integrasjon.familiepdf.FamiliePdfService
import no.nav.brukerdialog.integrasjon.familiepdf.dto.FamiliePdfPostRequest
import no.nav.brukerdialog.integrasjon.familiepdf.dto.PdfConfig
import no.nav.brukerdialog.integrasjon.familiepdf.dto.VerdilisteElement
import no.nav.brukerdialog.utils.TokenTestUtils.hentToken
import no.nav.brukerdialog.utils.WireMockServerUtils.stubFamiliePdf
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@EnableMockOAuth2Server
@SpringBootTest(
    classes = [K9brukerdialogprosesseringApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureWireMock
class FamiliePdfServiceTest {
    @Autowired
    private lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var familiePdfService: FamiliePdfService

    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

    @MockkBean
    lateinit var oAuth2AccessTokenService: OAuth2AccessTokenService

    @BeforeEach
    fun setUp() {
        wireMockServer.stubFamiliePdf()
        val token =
            mockOAuth2Server.hentToken("123456789", audience = "dev-gcp:dusseldorf:k9-brukerdialog-cache").serialize()
        every { oAuth2AccessTokenService.getAccessToken(any()) } returns OAuth2AccessTokenResponse(token)
    }

    @Test
    fun `Lag pdf fungerer som forventet`(): Unit = runBlocking {
        val feltmap = FamiliePdfPostRequest(
            label = "Test",
            verdiliste = listOf(
                VerdilisteElement(
                    label = "key",
                    verdi = "value"
                )
            ),
            pdfConfig = PdfConfig(språk = "nb", harInnholdsfortegnelse = true),
            skjemanummer = "skjemanummer"
        )

        val familiePdfResponse = familiePdfService.lagPdfKvittering(feltmap)
        assertThat(familiePdfResponse).isNotNull()
        assertThat(familiePdfResponse).isEqualTo("mocked-pdf-innhold".toByteArray())
    }
}
