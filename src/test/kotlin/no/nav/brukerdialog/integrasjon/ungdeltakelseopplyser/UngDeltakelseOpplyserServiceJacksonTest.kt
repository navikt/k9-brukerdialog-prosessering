package no.nav.brukerdialog.integrasjon.ungdeltakelseopplyser

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.brukerdialog.GcsStorageTestConfiguration
import no.nav.brukerdialog.utils.TokenTestUtils.hentToken
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.ung.deltakelseopplyser.kontrakt.deltaker.DeltakerDTO
import no.nav.ung.deltakelseopplyser.kontrakt.register.DeltakelseDTO
import no.nav.ung.deltakelseopplyser.kontrakt.register.DeltakelseKomposittDTO
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
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@EnableMockOAuth2Server
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(GcsStorageTestConfiguration::class)
class UngDeltakelseOpplyserServiceJacksonTest {

    companion object {
        @JvmField
        @RegisterExtension
        val wireMock: WireMockExtension = WireMockExtension.newInstance().build()

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("no.nav.integration.ung-deltakelse-opplyser-base-url") { wireMock.baseUrl() }
        }

        private val deltakelseId = UUID.randomUUID()

        /** ObjectMapper som matcher ung-deltakelse-opplyser sin Jackson-konfigurasjon (custom ISO_INSTANT JavaTimeModule). */
        val ungDeltakelseOpplyserObjectMapper: ObjectMapper = jacksonObjectMapper().apply {
            val zonedDateTimeFormatter = DateTimeFormatter.ISO_INSTANT.withZone(UTC)
            registerModule(JavaTimeModule().also {
                it.addSerializer(ZonedDateTime::class.java, object : JsonSerializer<ZonedDateTime?>() {
                    override fun serialize(zdt: ZonedDateTime?, gen: JsonGenerator?, serializers: SerializerProvider?) {
                        gen?.writeString(zdt?.format(zonedDateTimeFormatter))
                    }
                })
                it.addDeserializer(ZonedDateTime::class.java, object : JsonDeserializer<ZonedDateTime?>() {
                    override fun deserialize(p0: JsonParser, p1: DeserializationContext?): ZonedDateTime {
                        return ZonedDateTime.parse(p0.valueAsString, zonedDateTimeFormatter)
                    }
                })
            })
            findAndRegisterModules()
        }
    }

    @Autowired
    private lateinit var jsonMapper: JsonMapper

    @Autowired
    private lateinit var ungDeltakelseOpplyserService: UngDeltakelseOpplyserService

    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

    @MockkBean
    lateinit var oAuth2AccessTokenService: OAuth2AccessTokenService

    @BeforeEach
    fun setUp() {
        val token = mockOAuth2Server.hentToken(audience = "ung-deltakelse-opplyser").serialize()
        every { oAuth2AccessTokenService.getAccessToken(any()) } returns OAuth2AccessTokenResponse(token)
    }

    @Test
    fun `klient skal deserialisere DeltakelseKomposittDTO serialisert med config fra ung-deltakelse-opplyser`() {
        stubMarkerDeltakelse(ungDeltakelseOpplyserObjectMapper.writeValueAsString(defaultDeltakelse()))

        val result = ungDeltakelseOpplyserService.markerDeltakelseSomSøkt(deltakelseId)
        assertThat(result.deltakelse).isNotNull
        assertThat(result.deltakelse.deltaker.deltakerIdent).isEqualTo("12345678910")
        assertThat(result.deltakelse.søktTidspunkt).isNotNull
    }

    private fun defaultDeltakelse() = DeltakelseKomposittDTO(
        deltakelse = DeltakelseDTO(
            id = UUID.randomUUID(),
            deltaker = DeltakerDTO(
                id = UUID.randomUUID(),
                deltakerIdent = "12345678910",
            ),
            fraOgMed = LocalDate.parse("2025-01-01"),
            tilOgMed = LocalDate.parse("2025-06-30"),
            søktTidspunkt = ZonedDateTime.parse("2025-01-15T10:30:00Z"),
        ),
        oppgaver = emptyList(),
    )

    private fun stubMarkerDeltakelse(body: String) {
        wireMock.stubFor(
            WireMock.put(WireMock.urlPathMatching("/deltakelse/register/.*/marker-har-sokt"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)
                )
        )
    }
}
