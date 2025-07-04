package no.nav.brukerdialog

import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@SpringBootTest/*(classes = [K9brukerdialogprosesseringApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)*/
@ActiveProfiles("test")
@EnableMockOAuth2Server
@Import(GcsStorageTestConfiguration::class)
class K9brukerdialogprosesseringApplicationTests {

	@Test
	fun contextLoads() {
	}
}
