package no.nav.brukerdialog.mellomlagring.dokument

import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import no.nav.brukerdialog.GcsStorageTestConfiguration
import no.nav.brukerdialog.utils.TokenTestUtils.hentToken
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.multipart
import org.springframework.util.ResourceUtils
import org.springframework.util.unit.DataSize


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@EnableMockOAuth2Server
@ActiveProfiles("test")
@Import(GcsStorageTestConfiguration::class)
class VedleggControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

    @MockkBean
    private lateinit var vedleggService: VedleggService

    @Test
    fun `Opplasting av vedlegg større enn 1MB feiler ikke`() {
        coEvery { vedleggService.lagreVedlegg(any(), any()) } returns "12345"

        val file = ResourceUtils.getFile("classpath:filer/Bilde_3_MB.jpg")
        val fileContent = file.readBytes()
        println("---> File size: ${DataSize.ofBytes(fileContent.size.toLong()).toMegabytes()} MB")

        val mockFile = MockMultipartFile(
            "vedlegg",
            file.name,
            MediaType.IMAGE_JPEG_VALUE,
            fileContent
        )

        mockMvc.multipart("/vedlegg") {
            file(mockFile)
            contentType = MediaType.MULTIPART_FORM_DATA
            header("Authorization", "Bearer ${mockOAuth2Server.hentToken().serialize()}")
        }.andExpect {
            status { isCreated() }
        }
    }
}

