package no.nav.brukerdialog.mellomlagring.dokument

import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import no.nav.brukerdialog.utils.CallIdGenerator
import no.nav.brukerdialog.utils.NavHeaders
import no.nav.brukerdialog.utils.TokenTestUtils.mockContext
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart
import org.springframework.util.ResourceUtils
import org.springframework.web.client.HttpClientErrorException
import java.nio.file.Files

@WebMvcTest(controllers = [VedleggController::class])
@Import(CallIdGenerator::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VedleggControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var vedleggService: VedleggService

    @MockkBean
    private lateinit var springTokenValidationContextHolder: SpringTokenValidationContextHolder

    @BeforeEach
    fun setUp() {
        springTokenValidationContextHolder.mockContext()
    }

    @Test
    fun `Opplasting av vedlegg med støttet content-type returnerer location header`() {
        coEvery { vedleggService.lagreVedlegg(any(), any()) } returns "12345"

        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        val pdfFil = ResourceUtils.getFile("classpath:filer/test.pdf")

        val mockFile = MockMultipartFile(
            "vedlegg",
            "test.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            pdfFil.readBytes()
        )

        mockMvc.multipart("/vedlegg") {
            file(mockFile)
            contentType = MediaType.MULTIPART_FORM_DATA
        }.andExpect {
            status { isCreated() }
            header { string(HttpHeaders.LOCATION, "http://localhost/vedlegg/12345") }
            header { string(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.LOCATION) }
            header { exists(NavHeaders.X_CORRELATION_ID) }
        }
    }

    @Test
    fun `Opplasting av vedlegg med som er av annen type enn content-type sier den er gir valideringsfeil`() {
        coEvery { vedleggService.lagreVedlegg(any(), any()) } returns "12345"

        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        val bildeFil = ResourceUtils.getFile("classpath:filer/nav-logo.png")

        val mockFile = MockMultipartFile(
            "vedlegg",
            "nav-logo.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            bildeFil.readBytes()
        )

        mockMvc.multipart("/vedlegg") {
            file(mockFile)
            contentType = MediaType.MULTIPART_FORM_DATA
        }.andExpect {
            status { isBadRequest() }
            header { exists(NavHeaders.X_CORRELATION_ID) }
            content {
                json(
                    """
                {
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "status": 400,
                  "detail": "Forespørselen inneholder valideringsfeil",
                  "instance": "/vedlegg",
                  "violations": [
                    {
                      "parameterName": "vedlegg",
                      "parameterType": "FORM",
                      "reason": "Vedlegg av typen image/png påstås ikke for å være application/pdf",
                      "invalidValue": "application/pdf"
                    }
                  ]                  
                }
            """.trimIndent(), true
                )
            }
        }
    }

    @Test
    fun `Opplasting av vedlegg med ikke støttet content-type feiler`() {
        // Mock the behavior of vedleggService
        coEvery { vedleggService.lagreVedlegg(any(), any()) } returns "12345"

        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        val mockFile = MockMultipartFile(
            "vedlegg",
            "test-file.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "test-content".toByteArray()
        )

        mockMvc.multipart("/vedlegg") {
            file(mockFile)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        }.andExpect {
            status { isBadRequest() }
            header { exists(NavHeaders.X_CORRELATION_ID) }
            content {
                json(
                    """
                {
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "status": 400,
                  "detail": "Forespørselen inneholder valideringsfeil",
                  "instance": "/vedlegg",
                  "violations": [
                    {
                      "parameterName": "vedlegg",
                      "parameterType": "FORM",
                      "reason": "Kun [application/pdf, image/jpeg, image/png] er tillatt",
                      "invalidValue": "text/plain"
                    }
                  ]                  
                }
            """.trimIndent(), true
                )
            }
        }
    }

    @Test
    fun `Sletting av vedlegg som feiler returnerer 500 problem details`() {
        coEvery {
            vedleggService.slettVedlegg(
                any(),
                any()
            )
        } throws RuntimeException("Feil ved sletting av dokument med id: 123")

        mockMvc.delete("/vedlegg/12345")
            .andExpect {
                status { isInternalServerError() }
                header { exists(NavHeaders.X_CORRELATION_ID) }
                content {
                    json(
                        """
                        {
                          "type": "about:blank",
                          "title": "Internal Server Error",
                          "status": 500,
                          "detail": "Feil ved sletting av vedlegg",
                          "instance": "/vedlegg/12345"
                        }
                        """.trimIndent(), true
                    )
                }
            }
    }

    @Test
    fun `Sletting av vedlegg som lykkes returnerer 204`() {
        coEvery { vedleggService.slettVedlegg(any(), any()) } returns Unit

        mockMvc.delete("/vedlegg/12345")
            .andExpect {
                status { isNoContent() }
                header { exists(NavHeaders.X_CORRELATION_ID) }
            }
    }

    @Test
    fun `Henting av vedlegg som ikke blir funnet returnerer 404`() {
        coEvery { vedleggService.hentVedlegg(any(), any()) } throws HttpClientErrorException.create(
            HttpStatus.NOT_FOUND,
            "Vedlegg ikke funnet",
            HttpHeaders(),
            "Vedlegg med id 12345 ble ikke funnet".toByteArray(),
            null
        )

        mockMvc.get("/vedlegg/12345")
            .andExpect {
                status { isNotFound() }
                header { exists(NavHeaders.X_CORRELATION_ID) }
                content {
                    json(
                        """
                        {
                          "type": "about:blank",
                          "title": "Not Found",
                          "status": 404,
                          "detail": "Vedlegg med id 12345 ble ikke funnet",
                          "instance": "/vedlegg/12345"
                        }
                        """.trimIndent(), true
                    )
                }
            }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "Bilde_3_MB.jpg",
            "test.pdf",
            "nav-logo.png"
        ]
    )
    fun `Henting av vedlegg som lykkes returnerer 200 og vedlegget`(filnavn: String) {
        val file = ResourceUtils.getFile("classpath:filer/$filnavn")
        val fileContent = file.readBytes()
        val fileContentType = Files.probeContentType(file.toPath())

        coEvery { vedleggService.hentVedlegg(any(), any()) } returns Vedlegg(
            content = fileContent,
            contentType = fileContentType,
            title = file.name
        )

        mockMvc.get("/vedlegg/12345")
            .andExpect {
                status { isOk() }
                header { exists(NavHeaders.X_CORRELATION_ID) }
                content {
                    contentType(fileContentType)
                    bytes(fileContent)
                }
            }
    }
}

