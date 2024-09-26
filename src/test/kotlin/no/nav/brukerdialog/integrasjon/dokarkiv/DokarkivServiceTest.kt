package no.nav.brukerdialog.integrasjon.dokarkiv

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import kotlinx.coroutines.runBlocking
import no.nav.brukerdialog.K9brukerdialogprosesseringApplication
import no.nav.brukerdialog.integrasjon.dokarkiv.DokarkivResponseTransformer.Companion.BREVKODE_MED_FORVENTET_JOURNALPOST_ID
import no.nav.brukerdialog.integrasjon.dokarkiv.dto.AvsenderMottakerIdType
import no.nav.brukerdialog.integrasjon.dokarkiv.dto.JournalPostRequestV1Factory
import no.nav.brukerdialog.integrasjon.dokarkiv.dto.JournalPostType
import no.nav.brukerdialog.integrasjon.dokarkiv.dto.Kanal
import no.nav.brukerdialog.integrasjon.dokarkiv.dto.YtelseType
import no.nav.brukerdialog.mellomlagring.dokument.Dokument
import no.nav.brukerdialog.utils.TokenTestUtils.hentToken
import no.nav.brukerdialog.utils.WireMockServerUtils.stubJournalføring
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.cloud.contract.wiremock.WireMockConfigurationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
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
@Import(DokarkivServiceTest.DokarkivServiceTestConfig::class)
@AutoConfigureWireMock
class DokarkivServiceTest {
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var dokarkivService: DokarkivService

    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

    @MockkBean
    lateinit var oAuth2AccessTokenService: OAuth2AccessTokenService

    @TestConfiguration
    class DokarkivServiceTestConfig {
        @Bean
        fun optionsCustomizer(): WireMockConfigurationCustomizer {
            return WireMockConfigurationCustomizer { options -> options.extensions(DokarkivResponseTransformer()) }
        }
    }

    companion object {
        @JvmStatic
        fun søknaderForJournalføring(): List<Journalføring> {
            return YtelseType.entries.map {
                val journalpostId = BREVKODE_MED_FORVENTET_JOURNALPOST_ID.getValue(it)
                Journalføring(it, journalpostId)
            }
        }

        data class Journalføring(
            val ytelseType: YtelseType,
            val forventetJournalpostId: String,
        )
    }

    @BeforeEach
    fun setUp() {
        wireMockServer.stubJournalføring()

        val token =
            mockOAuth2Server.hentToken("123456789", audience = "dev-gcp:dusseldorf:k9-brukerdialog-cache").serialize()
        every { oAuth2AccessTokenService.getAccessToken(any()) } returns OAuth2AccessTokenResponse(token)
    }

    @ParameterizedTest
    @MethodSource("søknaderForJournalføring")
    fun `Journalføring fungerer som forventet`(journalføring: Journalføring): Unit = runBlocking {
        val journalpostResponse = dokarkivService.journalfør(journalpostRequest(journalføring))
        assertThat(journalpostResponse.journalpostId).isNotNull()
        assertThat(journalpostResponse.journalpostId).isEqualTo(journalføring.forventetJournalpostId)

    }

    private fun journalpostRequest(journalføring: Journalføring) =
        JournalPostRequestV1Factory.instance(
            kanal = Kanal.NAV_NO,
            journalposttype = JournalPostType.INNGAAENDENDE,
            mottaker = "123456789",
            dokumenter = listOf(
                listOf(
                    Dokument(
                        eier = null,
                        content = "content".toByteArray(),
                        contentType = MediaType.APPLICATION_PDF_VALUE,
                        title = "laoret"
                    )
                )
            ),
            datoMottatt = ZonedDateTime.now(),
            ytelseType = journalføring.ytelseType,
            avsenderMottakerIdType = AvsenderMottakerIdType.FNR,
            avsenderMottakerNavn = "Ola Nordmann",
            eksternReferanseId = "123156",
        )
}
