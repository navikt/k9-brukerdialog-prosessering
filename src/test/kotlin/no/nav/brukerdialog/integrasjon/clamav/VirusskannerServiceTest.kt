package no.nav.brukerdialog.integrasjon.clamav

import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import no.nav.brukerdialog.GcsStorageTestConfiguration
import no.nav.brukerdialog.K9brukerdialogprosesseringApplication
import no.nav.brukerdialog.utils.WireMockServerUtils.stubVirusScan
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
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
import org.springframework.web.ErrorResponseException

@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@EnableMockOAuth2Server
@SpringBootTest(
    classes = [K9brukerdialogprosesseringApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import(GcsStorageTestConfiguration::class)
class VirusskannerServiceTest {

    companion object {
        @JvmField
        @RegisterExtension
        val wireMock: WireMockExtension = WireMockExtension.newInstance()
            .build()

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("no.nav.integration.clam-av-base-url") { "${wireMock.baseUrl()}/clamav-mock" }
        }
    }

    @Autowired
    lateinit var virusskannerService: VirusskannerService

    @Test
    fun `Gitt fil uten virus, forvent ingen exception`() {
        wireMock.stubVirusScan(HttpStatus.OK, ScanResultat.OK)
        assertDoesNotThrow { virusskannerService.skann("fil med innhold".toByteArray()) }
    }

    @Test
    fun `Gitt infisert fil, forvent exception`() {
        wireMock.stubVirusScan(HttpStatus.OK, ScanResultat.FOUND)
        assertThrows<ErrorResponseException> { virusskannerService.skann("fil med innhold".toByteArray()) }
    }

    @Test
    fun `Gitt scanning av fil feiler, forvent exception`() {
        wireMock.stubVirusScan(HttpStatus.INTERNAL_SERVER_ERROR, ScanResultat.FOUND)
        assertThrows<ErrorResponseException> { virusskannerService.skann("fil med innhold".toByteArray()) }
    }
}
