package no.nav.brukerdialog.integrasjon.k9sakinnsynapi

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.brukerdialog.GcsStorageTestConfiguration
import no.nav.brukerdialog.K9brukerdialogprosesseringApplication
import no.nav.brukerdialog.utils.TokenTestUtils.hentToken
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.utils.SøknadUtils
import no.nav.k9.søknad.ytelse.psb.v1.PleiepengerSyktBarn
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import tools.jackson.databind.json.JsonMapper
import java.time.LocalDate

@ActiveProfiles("test")
@EnableMockOAuth2Server
@SpringBootTest(
    classes = [K9brukerdialogprosesseringApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import(GcsStorageTestConfiguration::class)
class K9SakInnsynApiServiceTest {

    val k9SakInnsynApiObjectMapper = ObjectMapper().apply {
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
        propertyNamingStrategy = PropertyNamingStrategies.LOWER_CAMEL_CASE
        findAndRegisterModules()
    }

    @Autowired
    private lateinit var jsonMapper: JsonMapper

    @Autowired
    private lateinit var k9SakInnsynApiService: K9SakInnsynApiService

    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

    @MockkBean
    lateinit var oAuth2AccessTokenService: OAuth2AccessTokenService

    companion object {
        @JvmField
        @RegisterExtension
        val wireMock: WireMockExtension = WireMockExtension.newInstance().build()

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("no.nav.integration.k9-sak-innsyn-api-base-url") { wireMock.baseUrl() }
        }
    }

    @BeforeEach
    fun setUp() {
        val token = mockOAuth2Server.hentToken(audience = "k9-sak-innsyn-api").serialize()
        every { oAuth2AccessTokenService.getAccessToken(any()) } returns OAuth2AccessTokenResponse(token)
    }

    @Test
    fun `klient skal deserialisere K9SakInnsynSøknad serialisert med config fra k9-sak-innsyn-api`() {
        val innsynSøknad = defaultSøknad()
        val responseBody = k9SakInnsynApiObjectMapper.writeValueAsString(listOf(innsynSøknad))

        wireMock.stubFor(
            WireMock.get(WireMock.urlEqualTo("/soknad"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)
                )
        )

        val result = k9SakInnsynApiService.hentSøknadsopplysninger()

        assertThat(result).hasSize(1)
        assertThat(result[0].barn.identitetsnummer).isEqualTo("02119970079")
        assertThat(result[0].barn.fornavn).isEqualTo("Test")

        val psbYtelse = result[0].søknad.getYtelse<PleiepengerSyktBarn>()
        assertThat(psbYtelse.søknadsperiodeList).hasSize(1)
        assertThat(psbYtelse.barn.personIdent.verdi).isEqualTo("02119970079")
    }

    private fun defaultSøknad(): K9SakInnsynSøknad {
        val søknad = SøknadUtils.defaultK9FormatPSB()
        val barn = Barn(
            fødselsdato = LocalDate.parse("2019-07-11"),
            fornavn = "Test",
            mellomnavn = null,
            etternavn = "Barnesen",
            aktørId = "1000000000001",
            identitetsnummer = "02119970079"
        )

        val innsynSøknad = K9SakInnsynSøknad(
            barn,
            søknad
        )
        return innsynSøknad
    }
}
