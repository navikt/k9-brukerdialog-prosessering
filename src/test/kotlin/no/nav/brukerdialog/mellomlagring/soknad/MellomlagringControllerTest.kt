package no.nav.brukerdialog.mellomlagring.soknad

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import no.nav.brukerdialog.utils.CallIdGenerator
import no.nav.brukerdialog.utils.NavHeaders
import no.nav.brukerdialog.utils.TokenTestUtils.mockContext
import no.nav.brukerdialog.ytelse.Ytelse
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.json.JSONObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.time.ZonedDateTime
import java.util.*

@ExtendWith(SpringExtension::class)
@WebMvcTest(controllers = [MellomlagringController::class])
@Import(CallIdGenerator::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MellomlagringControllerTest {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var mellomlagringService: MellomlagringService

    @MockkBean
    private lateinit var springTokenValidationContextHolder: SpringTokenValidationContextHolder

    private companion object {
        private val logger = LoggerFactory.getLogger(MellomlagringControllerTest::class.java)
    }

    @BeforeEach
    fun setUp() {
        springTokenValidationContextHolder.mockContext()
    }


    @Test
    fun `Innsending av tom mellomlagring`() {
        val mellomlagringSøknad = "{}"

        coEvery {
            mellomlagringService.settMellomlagring(
                any(),
                any()
            )
        } returns mockCacheResponse("mellomlagring_OMSORGSDAGER_ALENEOMSORG", mellomlagringSøknad)

        mockMvc.post("/mellomlagring/OMSORGSDAGER_ALENEOMSORG") {
            contentType = MediaType.APPLICATION_JSON
            content = mellomlagringSøknad
        }.andExpect {
            status { isCreated() }
            header { exists(NavHeaders.X_CORRELATION_ID) }
        }

        coEvery { mellomlagringService.hentMellomlagring(any()) } returns mellomlagringSøknad

        mockMvc.get("/mellomlagring/OMSORGSDAGER_ALENEOMSORG") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            header { exists(NavHeaders.X_CORRELATION_ID) }
            content { json(mellomlagringSøknad) }
        }
    }

    @ParameterizedTest
    @EnumSource(Ytelse::class)
    fun `Sende inn, hente, oppdatere og slette mellomlagring`(ytelse: Ytelse) {
        val mellomlagring = """{"formData":{"noe":"no"},"metadata":{"noeAnnet":"ABC"}}"""
        val oppdatertMellomlagringSøknad = """{"mellomlagring": "oppdatert soknad"}"""

        val url = "/mellomlagring/$ytelse"
        val nøkkelPrefix = "mellomlagring_${ytelse}_${UUID.randomUUID()}"

        coEvery { mellomlagringService.settMellomlagring(any(), any()) } returns mockCacheResponse(
            nøkkelPrefix,
            mellomlagring
        )

        // Post request
        mockMvc.post(url) {
            contentType = MediaType.APPLICATION_JSON
            content = mellomlagring
        }.andExpect {
            status { isCreated() }
            header { exists(NavHeaders.X_CORRELATION_ID) }
        }

        coEvery { mellomlagringService.hentMellomlagring(any()) } returns mellomlagring

        // Get request
        mockMvc.get(url) {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { json(mellomlagring) }
            header { exists(NavHeaders.X_CORRELATION_ID) }
        }

        coEvery { mellomlagringService.oppdaterMellomlagring(any(), any()) } returns mockCacheResponse(
            nøkkelPrefix,
            oppdatertMellomlagringSøknad
        )

        // Put request
        mockMvc.put(url) {
            contentType = MediaType.APPLICATION_JSON
            content = oppdatertMellomlagringSøknad
        }.andExpect {
            status { isNoContent() }
            header { exists(NavHeaders.X_CORRELATION_ID) }
        }

        coEvery { mellomlagringService.hentMellomlagring(any()) } returns oppdatertMellomlagringSøknad

        // Get updated request
        mockMvc.get(url) {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            header { exists(NavHeaders.X_CORRELATION_ID) }
            content { json(oppdatertMellomlagringSøknad) }
        }

        coEvery { mellomlagringService.slettMellomlagring(any()) } returns true

        // Delete request
        mockMvc.delete(url) {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isAccepted() }
            header { exists(NavHeaders.X_CORRELATION_ID) }
        }

        coEvery { mellomlagringService.hentMellomlagring(any()) } returns null

        // Get after delete request
        mockMvc.get(url) {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            header { exists(NavHeaders.X_CORRELATION_ID) }
            content { json("{}") }
        }
    }

    private fun mockCacheResponse(nøkkelPrefix: String, mellomlagring: String) = CacheResponse(
        nøkkel = nøkkelPrefix,
        verdi = mellomlagring,
        utløpsdato = ZonedDateTime.now().plusDays(3),
        opprettet = ZonedDateTime.now(),
        endret = null
    )

    @Test
    fun `gitt mellomlagring ikke eksisterer, forvent tomt objekt`() {
        coEvery { mellomlagringService.hentMellomlagring(any()) } returns null
        mockMvc.get("/mellomlagring/OMSORGSDAGER_ALENEOMSORG") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            header { exists(NavHeaders.X_CORRELATION_ID) }
            content { json("{}") }
        }
    }

    @Test
    fun `gitt det mellomlagres på en eksisterende nøkkel, forvent konfliktfeil`() {
        coEvery { mellomlagringService.settMellomlagring(any(), any()) } throws CacheConflictException("psb_123")

        val mellomlagringSøknad = """{"mellomlagring": "soknad"}"""
        mockMvc.post("/mellomlagring/OMSORGSDAGER_ALENEOMSORG") {
            contentType = MediaType.APPLICATION_JSON
            content = mellomlagringSøknad
        }.andExpect {
            status { isConflict() }
            header { exists(NavHeaders.X_CORRELATION_ID) }
            content {
                json(
                    """
                    {
                      "type": "about:blank",
                      "title": "Conflict",
                      "status": 409,
                      "detail": "Cache med nøkkelPrefiks = psb_123 for person finnes allerede.",
                      "instance": "/mellomlagring/OMSORGSDAGER_ALENEOMSORG"
                    }
                    """.trimIndent()
                )
            }
        }
    }

    @Test
    fun `gitt sletting av en ikke-eksisterende nøkkel, forvent ingen feil`() {
        coEvery { mellomlagringService.slettMellomlagring(any()) } returns false

        val mellomlagringSøknad = """{"mellomlagring": "soknad"}"""
        mockMvc.delete("/mellomlagring/OMSORGSDAGER_ALENEOMSORG") {
            contentType = MediaType.APPLICATION_JSON
            content = mellomlagringSøknad
        }.andExpect {
            status { isAccepted() }
            header { exists(NavHeaders.X_CORRELATION_ID) }
        }
    }

    @Test
    fun name() {
        val deserialized = objectMapper.readValue<Map<String, Any>>("{}")
        val serialized = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
            CacheRequest(
                nøkkelPrefiks = "psb_123",
                verdi = JSONObject(deserialized).toString(),
                ytelse = Ytelse.ETTERSENDING,
                utløpsdato = ZonedDateTime.now(),
                opprettet = ZonedDateTime.now(),
                endret = null
            )
        )
        logger.info("Deserialized: {}. Serialized: {}", deserialized, serialized)
    }
}

