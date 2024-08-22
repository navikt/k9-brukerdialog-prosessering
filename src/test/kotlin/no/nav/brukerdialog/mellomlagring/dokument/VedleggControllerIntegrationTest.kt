package no.nav.brukerdialog.mellomlagring.dokument

import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import no.nav.brukerdialog.utils.TokenTestUtils.hentToken
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.context.ActiveProfiles
import org.springframework.util.ResourceUtils
import org.springframework.util.unit.DataSize


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableMockOAuth2Server
@ActiveProfiles("test")
class VedleggControllerIntegrationTest {

    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate

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

        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("vedlegg", FileSystemResource(file))

        val build = bodyBuilder.build()
        val requestEntity = RequestEntity.post("/vedlegg")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .header("Authorization", "Bearer ${mockOAuth2Server.hentToken().serialize()}")
            .body(build)

        val postForEntity = testRestTemplate.postForEntity("/vedlegg", requestEntity, String::class.java)
        assertThat(postForEntity.statusCode).isEqualTo(HttpStatus.CREATED)
    }
}

