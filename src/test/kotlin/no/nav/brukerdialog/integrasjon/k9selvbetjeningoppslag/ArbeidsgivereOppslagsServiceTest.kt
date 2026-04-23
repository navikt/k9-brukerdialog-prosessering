package no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import com.ninjasquad.springmockk.MockkBean
import no.nav.brukerdialog.GcsStorageTestConfiguration
import no.nav.brukerdialog.integrasjon.k9selvbetjeningoppslag.SøkerOppslagsServiceTest.Companion.k9SelvbetjeningOppslagObjectMapper
import no.nav.brukerdialog.oppslag.arbeidsgiver.*
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
class ArbeidsgivereOppslagsServiceTest {

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
    lateinit var arbeidsgivereOppslagsService: ArbeidsgivereOppslagsService

    @MockkBean
    private lateinit var springTokenValidationContextHolder: SpringTokenValidationContextHolder

    @BeforeEach
    fun setUp() {
        springTokenValidationContextHolder.mockContext()
        MDCUtil.toMDC(Constants.CALLER_CLIENT_ID, "pleiepengesoknad")
    }

    @Test
    fun `klient skal deserialisere ArbeidsgivereOppslagResponsDto serialisert med config fra k9-selvbetjening-oppslag`() {
        val arbeidsgivereRespons = ArbeidsgivereOppslagResponsDto(
            arbeidsgivere = ArbeidsgivereOppslagDto(
                organisasjoner = listOf(
                    OrganisasjonOppslagDto(
                        organisasjonsnummer = "999999999",
                        navn = "Test Bedrift AS",
                        ansattFom = LocalDate.parse("2020-01-01"),
                        ansattTom = null
                    )
                ),
                privateArbeidsgivere = listOf(
                    PrivatArbeidsgiverOppslagDto(
                        offentligIdent = "12345678901",
                        ansattFom = LocalDate.parse("2021-06-01"),
                        ansattTom = LocalDate.parse("2022-12-31")
                    )
                ),
                frilansoppdrag = listOf(
                    FrilansoppdragOppslagDto(
                        type = "frilans",
                        organisasjonsnummer = "888888888",
                        navn = "Frilans Oppdrag AS",
                        offentligIdent = null,
                        ansattFom = LocalDate.parse("2023-01-15"),
                        ansattTom = null
                    )
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
                        .withBody(k9SelvbetjeningOppslagObjectMapper.writeValueAsString(arbeidsgivereRespons))
                )
        )

        val result = arbeidsgivereOppslagsService.hentArbeidsgivere(
            fraOgMed = LocalDate.parse("2020-01-01"),
            tilOgMed = LocalDate.parse("2023-12-31"),
            inkluderAlleAnsettelsesperioder = true,
            skalHentePrivateArbeidsgivere = true,
            skalHenteFrilansoppdrag = true
        )

        assertThat(result.arbeidsgivere.organisasjoner).hasSize(1)
        assertThat(result.arbeidsgivere.organisasjoner[0].organisasjonsnummer).isEqualTo("999999999")
        assertThat(result.arbeidsgivere.organisasjoner[0].ansattFom).isEqualTo(LocalDate.parse("2020-01-01"))
        assertThat(result.arbeidsgivere.privateArbeidsgivere).hasSize(1)
        assertThat(result.arbeidsgivere.frilansoppdrag).hasSize(1)
    }
}
