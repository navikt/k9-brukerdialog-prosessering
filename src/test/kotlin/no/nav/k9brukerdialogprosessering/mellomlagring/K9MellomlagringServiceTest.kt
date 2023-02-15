package no.nav.k9brukerdialogprosessering.mellomlagring

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import kotlinx.coroutines.runBlocking
import no.nav.k9brukerdialogprosessering.utils.WireMockServerUtils.stubLagreDokument
import no.nav.k9brukerdialogprosessering.utils.WireMockServerUtils.stubSlettDokument
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.RestClientException
import java.net.URI
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureWireMock
@EnableMockOAuth2Server
@ActiveProfiles("test")
class K9MellomlagringServiceTest {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var k9MellomlagringService: K9MellomlagringService

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Test
    fun `lagreDokument`(): Unit = runBlocking {
        val dokument = Dokument(
            eier = DokumentEier(eiersFødselsnummer = "123456"),
            content = "som etext as bytearray".encodeToByteArray(),
            contentType = MediaType.APPLICATION_PDF_VALUE,
            title = "some pdf-file"
        )

        val expectedLocationPath = "/v1/dokument/${UUID.randomUUID()}"

        wireMockServer.stubLagreDokument(
            urlPathMatching = "/v1/dokument",
            requestBodyJson = objectMapper.writeValueAsString(dokument),
            responseStatus = HttpStatus.OK,
            responseLocationHeaderPath = expectedLocationPath
        )

        val response: URI = k9MellomlagringService.lagreDokument(dokument)
        Assertions.assertThat(response.path).isEqualTo(expectedLocationPath)
    }

    @Test
    fun `slettDokumenter`() {
        val dokumentEier = DokumentEier(eiersFødselsnummer = "123456")
        val dokumentIder = listOf("123", "456")

        dokumentIder.forEach { dokumentId: String ->
            wireMockServer.stubSlettDokument(
                urlPathMatching = "/v1/dokument/$dokumentId",
                requestBodyJson = objectMapper.writeValueAsString(dokumentEier),
                responseStatus = HttpStatus.OK
            )
        }

        Assertions.assertThatNoException().isThrownBy {
            runBlocking {
                k9MellomlagringService.slettDokumenter(dokumentIder, dokumentEier)
            }
        }

        dokumentIder.forEach { dokumentId: String ->
            WireMock.verify(1, WireMock.deleteRequestedFor(WireMock.urlPathMatching(".*/v1/dokument/$dokumentId")))
        }
    }
}
