package no.nav.brukerdialog.integrasjon.gcpstorage

import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Bucket
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageException
import com.ninjasquad.springmockk.MockkBean
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.brukerdialog.K9brukerdialogprosesseringApplication
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.ByteArrayOutputStream

@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@EnableMockOAuth2Server
@SpringBootTest(
    classes = [K9brukerdialogprosesseringApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureWireMock
class GcpStorageServiceTest {

    @MockkBean
    lateinit var gcpStorage: Storage

    lateinit var gcpStorageService: GcpStorageService

    val bucket = "test-bucket"
    private val key = StorageKey("test-nøkkel")
    private val value = StorageValue("test-verdi")
    private lateinit var blobId: BlobId
    private lateinit var blob: Blob
    private lateinit var bucketStub: Bucket

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        blobId = BlobId.of(bucket, key.value)
        blob = mockk()
        bucketStub = mockk()
        every { bucketStub.location } returns "US"

        every { gcpStorage.get(bucket) } returns bucketStub
        gcpStorageService = assertDoesNotThrow {
            GcpStorageService(gcpStorage, bucket)
        }

        // Stubber både single-arg BlobId, String, and vararg get() kall for å unngå manglende svar
        every { gcpStorage.get(any<String>()) } returns null
        every { gcpStorage.get(any<BlobId>()) } returns null
        every { gcpStorage.get(any<BlobId>(), *anyVararg<Storage.BlobGetOption>()) } returns null
        every { gcpStorage.get(any<String>()) } returns null
        every { gcpStorage.get(any<BlobId>(), *anyVararg<Storage.BlobGetOption>()) } returns null
    }

    @Test
    fun `init feiler ikke når bøtte finnes`() {
        every { gcpStorage.get(bucket) } returns bucketStub
        assertDoesNotThrow {
            GcpStorageService(gcpStorage, bucket)
        }
    }

    @Test
    fun `init feiler når bøtte ikke finnes`() {
        every { gcpStorage.get("manglende-bøtte") } returns null
        assertThrows<IllegalStateException> { GcpStorageService(gcpStorage, "manglende-bøtte") }
    }


    @Test
    fun `hent returnerer verdi når blob finnes`() {
        every { gcpStorage.get(blobId) } returns blob
        every { blob.downloadTo(any<ByteArrayOutputStream>()) } answers {
            val os = firstArg<ByteArrayOutputStream>()
            os.write(this@GcpStorageServiceTest.value.value.toByteArray())
        }

        val resultat = gcpStorageService.hent(key)
        assertNotNull(resultat)
        assertThat(value.value).isEqualTo(resultat.value)
    }

    @Test
    fun `hent returnerer null når blob ikke finnes`() {
        every { gcpStorage.get(blobId) } returns null
        assertThat(gcpStorageService.hent(key)).isNull()
    }

    @Test
    fun `hent returnerer null ved StorageException`() {
        every { gcpStorage.get(blobId) } throws StorageException(404, "Blob not found")
        assertThat(gcpStorageService.hent(key)).isNull()
    }

    @Test
    fun `slett returnerer false når hent returnerer null`() {
        assertThat(gcpStorageService.slett(key)).isFalse()
    }

    @Test
    fun `slett sletter eksisterende blob vellykket`() {
        every { gcpStorage.get(blobId) } returns blob
        every { blob.downloadTo(any<ByteArrayOutputStream>()) } answers {
            val os = firstArg<ByteArrayOutputStream>()
            os.write(this@GcpStorageServiceTest.value.value.toByteArray())
        }
        every { gcpStorage.delete(bucket, key.value) } returns true

        assertThat(gcpStorageService.slett(key)).isTrue()
        verify { gcpStorage.delete(bucket, key.value) }
    }

    @Test
    fun `slett returnerer false når sletting kaster StorageException`() {
        every { gcpStorage.get(blobId) } returns blob
        every { blob.downloadTo(any<ByteArrayOutputStream>()) } answers {
            val os = firstArg<ByteArrayOutputStream>()
            os.write(this@GcpStorageServiceTest.value.value.toByteArray())
        }
        every { gcpStorage.delete(bucket, key.value) } throws StorageException(404, "Blob not found")

        assertThat(gcpStorageService.slett(key)).isFalse()
    }

    @Test
    fun `harHold oppdager midlertidig hold`() {
        every { gcpStorage.get(blobId, Storage.BlobGetOption.fields(Storage.BlobField.TEMPORARY_HOLD)) } returns blob
        every { blob.temporaryHold } returns true
        assertThat(gcpStorageService.harHold(key)).isTrue()

        every { blob.temporaryHold } returns false
        assertThat(gcpStorageService.harHold(key)).isFalse()
    }

    @Test
    fun `persister returnerer false når hent er null`() {
        every { gcpStorage.get(blobId) } returns null
        assertThat(gcpStorageService.persister(key)).isFalse()
    }

    @Test
    fun `persister setter hold og returnerer true`() {
        every { gcpStorage.get(blobId) } returns blob
        every { blob.downloadTo(any<ByteArrayOutputStream>()) } answers {
            val os = firstArg<ByteArrayOutputStream>()
            os.write(this@GcpStorageServiceTest.value.value.toByteArray())
        }
        every { gcpStorage.update(any<BlobInfo>()) } returns blob

        assertThat(gcpStorageService.persister(key)).isTrue()
        verify { gcpStorage.update(any<BlobInfo>()) }
    }

    @Test
    fun `persister returnerer false ved StorageException`() {
        every { gcpStorage.get(blobId) } returns blob
        every { blob.downloadTo(any<ByteArrayOutputStream>()) } answers {
            val os = firstArg<ByteArrayOutputStream>()
            os.write(this@GcpStorageServiceTest.value.value.toByteArray())
        }
        every { gcpStorage.update(any<BlobInfo>()) } throws StorageException(500, "Internal Server Error")

        assertThat(gcpStorageService.persister(key)).isFalse()
    }

    @Test
    fun `ready feiler ikke når bøtte finnes`() {
        every { gcpStorage.get(bucket) } returns bucketStub
        assertDoesNotThrow { gcpStorageService.ready() }
    }
}
