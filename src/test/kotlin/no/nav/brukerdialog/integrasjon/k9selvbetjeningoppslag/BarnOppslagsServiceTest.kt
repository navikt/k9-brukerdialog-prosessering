package no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import com.ninjasquad.springmockk.MockkBean
import no.nav.brukerdialog.GcsStorageTestConfiguration
import no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag.SøkerOppslagsServiceTest.Companion.k9SelvbetjeningOppslagObjectMapper
import no.nav.brukerdialog.oppslag.barn.BarnOppslagRespons
import no.nav.brukerdialog.oppslag.barn.BarnOppslagResponsListe
import no.nav.brukerdialog.utils.Constants
import no.nav.brukerdialog.utils.MDCUtil
import no.nav.brukerdialog.utils.TokenTestUtils.mockContext
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDate

@EnableMockOAuth2Server
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(GcsStorageTestConfiguration::class)
class BarnOppslagsServiceTest {

    companion object {
        @JvmField
        @RegisterExtension
        val wireMock: WireMockExtension = WireMockExtension.newInstance()
            .build()

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("no.nav.integration.k9-selvbetjening-oppslag-base-url") { wireMock.baseUrl() }
        }
    }

    @Autowired
    lateinit var barnOppslagsService: BarnOppslagsService

    @MockkBean
    private lateinit var springTokenValidationContextHolder: SpringTokenValidationContextHolder

    @BeforeEach
    fun setUp() {
        springTokenValidationContextHolder.mockContext()
        MDCUtil.toMDC(Constants.CALLER_CLIENT_ID, "pleiepengesoknad")
    }

    @Test
    fun `klient skal deserialisere BarnOppslagResponsListe serialisert med config fra k9-selvbetjening-oppslag`() {
        val barnRespons = BarnOppslagResponsListe(
            barn = listOf(
                BarnOppslagRespons(
                    identitetsnummer = "02119970079",
                    fødselsdato = LocalDate.parse("2019-11-02"),
                    fornavn = "Test",
                    mellomnavn = null,
                    etternavn = "Barnesen",
                    aktørId = "1000000000001"
                )
            )
        )

        wireMock.stubFor(
            WireMock
                .get(WireMock.urlPathEqualTo("/meg"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(k9SelvbetjeningOppslagObjectMapper.writeValueAsString(barnRespons))
                )
        )

        val barn = barnOppslagsService.hentBarn()
        assertThat(barn).hasSize(1)
        assertThat(barn[0].identitetsnummer).isEqualTo("02119970079")
        assertThat(barn[0].fødselsdato).isEqualTo(LocalDate.parse("2019-11-02"))
        assertThat(barn[0].fornavn).isEqualTo("Test")
        assertThat(barn[0].aktørId).isEqualTo("1000000000001")
    }
}
