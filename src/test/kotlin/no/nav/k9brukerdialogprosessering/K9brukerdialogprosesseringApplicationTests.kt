package no.nav.k9brukerdialogprosessering

import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest/*(classes = [K9brukerdialogprosesseringApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)*/
@ActiveProfiles("test")
@EnableMockOAuth2Server
class K9brukerdialogprosesseringApplicationTests {

	@Test
	fun contextLoads() {
	}
}
