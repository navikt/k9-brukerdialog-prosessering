package no.nav.k9brukerdialogprosessering.mellomlagring.dokument

import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import no.nav.k9brukerdialogprosessering.http.HeadersToMDCFilterBean
import no.nav.k9brukerdialogprosessering.utils.TokenTestUtils.mockContext
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.multipart

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
        // Mock the behavior of vedleggService
        coEvery { vedleggService.lagreVedlegg(any(), any()) } returns "12345"

        // Create headers
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        // Create a mock multipart file
        val mockFile = MockMultipartFile(
            "vedlegg",
            "test-file.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "test-content".toByteArray()
        )

        // Perform the multipart request and verify the response
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

        // Create headers
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        // Create a mock multipart file
        val mockFile = MockMultipartFile(
            "vedlegg",
            "test-file.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "test-content".toByteArray()
        )

        // Perform the multipart request and verify the response
        mockMvc.multipart("/vedlegg") {
            file(mockFile)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        }.andExpect {
            status { isBadRequest() }
            content {
                json(
                    """
                {
                  "type": "about:blank",
                  "title": "invalid-request-parameters",
                  "status": 400,
                  "detail": "Requesten inneholder ugyldige parametere",
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
}

