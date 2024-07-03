package no.nav.k9brukerdialogprosessering

import com.nimbusds.jwt.SignedJWT
import no.nav.k9brukerdialogprosessering.utils.hentToken
import no.nav.k9brukerdialogprosessering.utils.tokenTilHttpEntity
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EnableMockOAuth2Server
class K9brukerdialogAuthorizationTests {

    @Autowired
    lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate

    @Autowired
    lateinit var mockOAuth2Server: MockOAuth2Server

    private companion object {
        private val logger = LoggerFactory.getLogger(K9brukerdialogAuthorizationTests::class.java)
    }

    @Test
    fun contextLoads() {
        assertThat(endpointsProvider()).isNotEmpty
    }

    @ParameterizedTest
    @MethodSource("endpointsProvider")
    fun `Forventer at autorisasjon på endepunkter fungerer som forventet`(endpoint: Endpoint) {
        // Kall på endepunkt med riktig token ikke gir 401 feil
        testRestTemplate.assertNotEquals(
            endpoint = endpoint,
            expectedStatus = HttpStatus.UNAUTHORIZED,
            token = mockOAuth2Server.hentToken()
        )

        // Kall uten authorization header gir 401 feil
        testRestTemplate.assertEquals(
            endpoint = endpoint,
            token = null,
            expectedStatus = HttpStatus.UNAUTHORIZED
        )

        // Kall på endepunkt uten authorization header gir 401 feil
        testRestTemplate.assertEquals(
            endpoint = endpoint,
            expectedStatus = HttpStatus.UNAUTHORIZED
        )

        // Kall på endepunkt med acr level 3 gir 401 feil
        testRestTemplate.assertEquals(
            endpoint = endpoint,
            expectedStatus = HttpStatus.UNAUTHORIZED,
            token = mockOAuth2Server.hentToken(claims = mapOf("acr" to "Level3"))
        )

        // Kall på endepunkt med ukjent issuer gir 401 feil`(endpoint: Endpoint) {
        testRestTemplate.assertEquals(
            endpoint = endpoint,
            expectedStatus = HttpStatus.UNAUTHORIZED,
            token = mockOAuth2Server.hentToken(issuerId = "ukjent")
        )

        // Kall på endepunkt med utgått token gir 401 feil
        testRestTemplate.assertEquals(
            endpoint = endpoint,
            expectedStatus = HttpStatus.UNAUTHORIZED,
            token = mockOAuth2Server.hentToken(expiry = -1000)
        )

       // Kall på endepunkt med token for annen audience gir 401 feil`(endpoint: Endpoint) {
            testRestTemplate.assertEquals(
                endpoint = endpoint,
                expectedStatus = HttpStatus.UNAUTHORIZED,
                token = mockOAuth2Server.hentToken(audience = "annen-audience")
            )
    }


    private fun endpointsProvider(): List<Endpoint> {
        val requestMappingHandlerMapping =
            applicationContext.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping::class.java)
        val apiMappings: MutableMap<RequestMappingInfo, HandlerMethod> = requestMappingHandlerMapping.handlerMethods

        val endpointList = apiMappings.mapNotNull { entry ->
            val mappingInfo = entry.key
            logger.info("--> Endpoint: {}", mappingInfo)
            val requestMethod = mappingInfo.methodsCondition.methods.firstOrNull()
            if (requestMethod == null) {
                logger.warn("No request method found for mapping info: $mappingInfo")
                return@mapNotNull null
            }
            val path = mappingInfo.directPaths.first()

            Endpoint(
                method = requestMethod.asHttpMethod(),
                url = path,
            )
        }

        logger.info("Found endpoints: $endpointList")
        return endpointList
    }

    private fun TestRestTemplate.assertEquals(
        endpoint: Endpoint,
        expectedStatus: HttpStatus,
        token: SignedJWT? = null,
    ) {
        val url = endpoint.url
        val httpMethod = endpoint.method

        logger.info("Testing endpoint: $url with method: $httpMethod")
        val response = testRestTemplate.exchange(
            url,
            httpMethod,
            token?.tokenTilHttpEntity(),
            String::class.java
        )

        val statusCode = response.statusCode
        if (expectedStatus != statusCode) {
            logger.error("Forventet status $expectedStatus, men fikk $statusCode for $httpMethod $url")
        }
        assertThat(statusCode).isEqualTo(expectedStatus)
    }

    private fun TestRestTemplate.assertNotEquals(
        endpoint: Endpoint,
        expectedStatus: HttpStatus,
        token: SignedJWT? = null,
    ) {
        val url = endpoint.url
        val httpMethod = endpoint.method

        logger.info("Testing endpoint: $url with method: $httpMethod")
        val response = testRestTemplate.exchange(
            url,
            httpMethod,
            token?.tokenTilHttpEntity(),
            String::class.java
        )

        val statusCode = response.statusCode
        if (expectedStatus != statusCode) {
            logger.error("Forventet status $expectedStatus, men fikk $statusCode for $httpMethod $url")
        }
        assertThat(statusCode).isNotEqualTo(expectedStatus)
    }

    data class Endpoint(
        val method: HttpMethod,
        val url: String,
    )
}
