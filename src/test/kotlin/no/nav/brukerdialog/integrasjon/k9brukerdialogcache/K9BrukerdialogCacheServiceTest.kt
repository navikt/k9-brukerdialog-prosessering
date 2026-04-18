package no.nav.brukerdialog.integrasjon.k9brukerdialogcache

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.brukerdialog.GcsStorageTestConfiguration
import no.nav.brukerdialog.K9brukerdialogprosesseringApplication
import no.nav.brukerdialog.mellomlagring.soknad.CacheRequest
import no.nav.brukerdialog.mellomlagring.soknad.CacheResponse
import no.nav.brukerdialog.utils.TokenTestUtils.hentToken
import no.nav.brukerdialog.utils.WireMockServerUtils.stubOpprettMellomlagringLenient
import no.nav.brukerdialog.ytelse.Ytelse
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
import java.time.ZonedDateTime

@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@EnableMockOAuth2Server
@SpringBootTest(
    classes = [K9brukerdialogprosesseringApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import(GcsStorageTestConfiguration::class)
class K9BrukerdialogCacheServiceTest {

    companion object {
        @JvmField
        @RegisterExtension
        val wireMock: WireMockExtension = WireMockExtension.newInstance()
            .build()

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("no.nav.integration.k9-brukerdialog-cache-base-url") { "${wireMock.baseUrl()}/k9-brukerdialog-cache-mock" }
        }

        /** ObjectMapper som matcher k9-brukerdialog-cache sin Jackson-konfigurasjon (Jackson2ObjectMapperBuilderCustomizer). */
        val k9BrukerdialogCacheObjectMapper: ObjectMapper = ObjectMapper().apply {
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
            propertyNamingStrategy = PropertyNamingStrategies.LOWER_CAMEL_CASE
            findAndRegisterModules()
        }
    }

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
    fun `Mellomlagring av tom json gir ikke feil og skal deserialisere`() {
        val cacheRequest = CacheRequest(
            nøkkelPrefiks = "psb_123",
            verdi = "{}",
            ytelse = Ytelse.ETTERSENDING_PLEIEPENGER_SYKT_BARN,
            utløpsdato = ZonedDateTime.now().plusDays(3),
            opprettet = ZonedDateTime.now()
        )

        wireMock.stubOpprettMellomlagringLenient(
            urlPathMatching = "/k9-brukerdialog-cache-mock/api/cache",
            responseStatus = HttpStatus.CREATED,
            responseBodyJson = k9BrukerdialogCacheObjectMapper.writeValueAsString(
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
