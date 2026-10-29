package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.tilgang

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.brukerdialog.config.JacksonConfiguration
import no.nav.brukerdialog.utils.CallIdGenerator
import no.nav.brukerdialog.utils.TokenTestUtils.mockContext
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.tilgang.IngenTilgangÅrsak.EN_ARBEIDSGIVER_TO_ANSETTELSER_SAMME_UKE_MED_OPPHOLD
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.tilgang.IngenTilgangÅrsak.HAR_ARBEIDSTID_SOM_SELVSTENDIG_NÆRINGSDRIVENDE
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.endringsmelding.utils.TilgangTestUtils.periode
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@ExtendWith(SpringExtension::class)
@WebMvcTest(controllers = [EndringsmeldingTilgangController::class])
@Import(
    JacksonConfiguration::class,
    CallIdGenerator::class
)
class EndringsmeldingTilgangControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var endringsmeldingTilgangService: EndringsmeldingTilgangService

    @MockkBean
    private lateinit var springTokenValidationContextHolder: SpringTokenValidationContextHolder

    private val endringsperiode = periode("2024-01-01/2024-12-31")

    @BeforeEach
    fun setUp() {
        springTokenValidationContextHolder.mockContext()
    }

    @Test
    fun `tilgang gir kanBruke true, tom liste og no-store`() {
        every { endringsmeldingTilgangService.vurderTilgang() } returns TilgangResultat.tilgang(endringsperiode)

        mockMvc.get("/pleiepenger-sykt-barn/endringsmelding/tilgang")
            .andExpect {
                status { isOk() }
                header { string("Cache-Control", "no-store") }
                content {
                    //language=json
                    json("""{ "kanBruke": true, "årsaker": [], "tillattEndringsperiode": { "fom": "2024-01-01", "tom": "2024-12-31" } }""", JsonCompareMode.STRICT)
                }
            }
    }

    @Test
    fun `avslag gir 200 med årsakskodene i samme rekkefølge`() {
        every { endringsmeldingTilgangService.vurderTilgang() } returns TilgangResultat.ingenTilgang(
            listOf(HAR_ARBEIDSTID_SOM_SELVSTENDIG_NÆRINGSDRIVENDE, EN_ARBEIDSGIVER_TO_ANSETTELSER_SAMME_UKE_MED_OPPHOLD),
            endringsperiode
        )

        mockMvc.get("/pleiepenger-sykt-barn/endringsmelding/tilgang")
            .andExpect {
                status { isOk() }
                content {
                    //language=json
                    json(
                        """
                        {
                          "kanBruke": false,
                          "årsaker": [
                            "harArbeidstidSomSelvstendigNæringsdrivende",
                            "enArbeidsgiverToAnsettelserSammeUkeMedOpphold"
                          ],
                          "tillattEndringsperiode": {
                            "fom": "2024-01-01",
                            "tom": "2024-12-31"
                          }
                        }
                        """.trimIndent(),
                        JsonCompareMode.STRICT
                    )
                }
            }
    }
}
