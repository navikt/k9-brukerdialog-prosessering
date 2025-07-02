package no.nav.brukerdialog.integrasjon.clamav

import com.github.tomakehurst.wiremock.WireMockServer
import no.nav.brukerdialog.GcsStorageTestConfiguration
import no.nav.brukerdialog.K9brukerdialogprosesseringApplication
import no.nav.brukerdialog.utils.WireMockServerUtils.stubVirusScan
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.ErrorResponseException

@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@EnableMockOAuth2Server
@SpringBootTest(
    classes = [K9brukerdialogprosesseringApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureWireMock
@Import(GcsStorageTestConfiguration::class)
class VirusskannerServiceTest {

    @Autowired
    lateinit var virusskannerService: VirusskannerService

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Test
    fun `Gitt fil uten virus, forvent ingen exception`() {
        wireMockServer.stubVirusScan(HttpStatus.OK, ScanResultat.OK)
        assertDoesNotThrow { virusskannerService.skann("fil med innhold".toByteArray()) }
    }

    @Test
    fun `Gitt infisert fil, forvent exception`() {
        wireMockServer.stubVirusScan(HttpStatus.OK, ScanResultat.FOUND)
        assertThrows<ErrorResponseException> { virusskannerService.skann("fil med innhold".toByteArray()) }
    }

    @Test
    fun `Gitt scanning av fil feiler, forvent exception`() {
        wireMockServer.stubVirusScan(HttpStatus.INTERNAL_SERVER_ERROR, ScanResultat.FOUND)
        assertThrows<ErrorResponseException> { virusskannerService.skann("fil med innhold".toByteArray()) }
    }
}
