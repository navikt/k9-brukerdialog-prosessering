package no.nav.brukerdialog.mellomlagring.dokument

import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import no.nav.brukerdialog.http.serverside.HeadersToMDCFilterBean
import no.nav.brukerdialog.utils.TokenTestUtils.mockContext
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart
import org.springframework.web.client.HttpClientErrorException

@WebMvcTest(
    controllers = [VedleggController::class],
    excludeFilters = [
        ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [HeadersToMDCFilterBean::class])
    ]
)
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

        val mockFile = MockMultipartFile(
            "vedlegg",
            "test-file.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "test-content".toByteArray()
        )

        mockMvc.multipart("/vedlegg") {
            file(mockFile)
            contentType = MediaType.MULTIPART_FORM_DATA
        }.andExpect {
            status { isCreated() }
            header { string(HttpHeaders.LOCATION, "http://localhost/vedlegg/12345") }
            header { string(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.LOCATION) }
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
                      "reason": "Kun application/pdf, image/jpeg, og image/png er tillatt",
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

    @Test
    fun `Henting av vedlegg som lykkes returnerer 200 og vedlegget`() {
        coEvery { vedleggService.hentVedlegg(any(), any()) } returns Vedlegg(
            content = "test-content".toByteArray(),
            contentType = MediaType.APPLICATION_PDF_VALUE,
            title = "test-file.pdf"
        )

        mockMvc.get("/vedlegg/12345")
            .andExpect {
                status { isOk() }
                content {
                    contentType(MediaType.APPLICATION_PDF_VALUE)
                    bytes("test-content".toByteArray())
                }
            }
    }
}

