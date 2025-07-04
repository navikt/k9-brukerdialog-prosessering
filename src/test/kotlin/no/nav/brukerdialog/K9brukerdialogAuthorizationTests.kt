package no.nav.brukerdialog

import com.nimbusds.jwt.SignedJWT
import no.nav.brukerdialog.utils.TokenTestUtils.hentToken
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
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EnableMockOAuth2Server
@Import(GcsStorageTestConfiguration::class)
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

        val endpointList = apiMappings.keys.mapNotNull { mappingInfo: RequestMappingInfo ->
            logger.info("--> Endpoint: {}", mappingInfo.toString())
            val requestMethod = mappingInfo.methodsCondition.methods.firstOrNull()
            if (requestMethod == null) {
                logger.warn("No request method found for mapping info: $mappingInfo")
                return@mapNotNull null
            }
            val pathPattern = mappingInfo.pathPatternsCondition!!.patterns.first()

            var urlVariables: String? = null
            if (pathPattern.hasPatternSyntax()) {
                println("Pattern syntax error: ${pathPattern.patternString}")
                urlVariables = UUID.randomUUID().toString()
            }
            val path = pathPattern.patternString

            val contentType = mappingInfo.consumesCondition.consumableMediaTypes.firstOrNull()

            Endpoint(
                method = requestMethod.asHttpMethod(),
                url = path,
                urlVariables = urlVariables,
                contentType = contentType
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
        val response = request(endpoint, url, httpMethod, token)

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
        val response = request(endpoint, url, httpMethod, token)

        val statusCode = response.statusCode
        if (expectedStatus != statusCode) {
            logger.error("Forventet status $expectedStatus, men fikk $statusCode for $httpMethod $url")
        }
        assertThat(statusCode).isNotEqualTo(expectedStatus)
    }

    private fun TestRestTemplate.request(
        endpoint: Endpoint,
        url: String,
        httpMethod: HttpMethod,
        token: SignedJWT?,
    ): ResponseEntity<String> {
        val httpEntity = HttpHeaders().let {

            if (token != null) {
                it.setBearerAuth(token.serialize())
            }

            it.contentType = endpoint.contentType

            var body: Any? = null

            if (it.contentType == MediaType.MULTIPART_FORM_DATA) {
                body = håndterMultipartUpload(it)
            }

            HttpEntity(body, it)
        }

        return if (endpoint.urlVariables != null) {
            exchange(
                url,
                httpMethod,
                httpEntity,
                String::class.java,
                endpoint.urlVariables
            )
        } else {
            exchange(
                url,
                httpMethod,
                httpEntity,
                String::class.java
            )
        }
    }

    private fun håndterMultipartUpload(httpHeaders: HttpHeaders): LinkedMultiValueMap<String, Any> {
        httpHeaders.setContentDispositionFormData("vedlegg", "test-file.pdf")
        val file = MockMultipartFile(
            "vedlegg",
            "test-file.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "Test content".toByteArray()
        )
        return LinkedMultiValueMap<String, Any>().apply {
            add("vedlegg", file.resource)
        }
    }

    data class Endpoint(
        val method: HttpMethod,
        val url: String,
        val urlVariables: String? = null,
        val contentType: MediaType? = MediaType.APPLICATION_JSON,
    ) {
        override fun toString(): String {
            return "$method $url"
        }
    }
}
