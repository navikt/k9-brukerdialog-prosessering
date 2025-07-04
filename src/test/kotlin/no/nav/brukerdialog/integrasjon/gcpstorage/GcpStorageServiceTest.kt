package no.nav.brukerdialog.integrasjon.gcpstorage

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.Storage
import no.nav.brukerdialog.GcsStorageTestConfiguration
import no.nav.brukerdialog.K9brukerdialogprosesseringApplication
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.junit.jupiter.Testcontainers

@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@EnableMockOAuth2Server
@SpringBootTest(
    classes = [K9brukerdialogprosesseringApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureWireMock
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(GcsStorageTestConfiguration::class)
class GcpStorageServiceTest {

    @Autowired
    lateinit var storage: Storage

    @Autowired
    lateinit var gcpStorageService: GcpStorageService

    companion object {
        private const val TEST_BUCKET = "test-bucket"
        private val TEST_KEY = StorageKey("test-nøkkel")
        private val TEST_VALUE = StorageValue("test-verdi")
    }

    @BeforeEach
    fun cleanBucket() {
        // Tømmer bøtten før hver test for å sikre at vi starter med en ren tilstand
        storage.list(TEST_BUCKET).iterateAll().forEach { storage.delete(it.blobId) }
    }

    @Test
    fun `init feiler ikke når bøtte finnes`() {
        assertDoesNotThrow { GcpStorageService(storage, TEST_BUCKET) }
    }

    @Test
    fun `init feiler når bøtte ikke finnes`() {
        assertThrows<IllegalStateException> { GcpStorageService(storage, "manglende-bøtte") }
    }

    @Test
    fun `lagre verdi i GCP Storage`() {
        gcpStorageService.lagre(TEST_KEY, TEST_VALUE, hold = false)
        val blob = storage.get(BlobId.of(TEST_BUCKET, TEST_KEY.value))
        assertNotNull(blob)
        assertThat(blob.getContent().toString(Charsets.UTF_8)).isEqualTo(TEST_VALUE.value)
    }


    @Test
    fun `hent returnerer verdi når blob finnes`() {
        gcpStorageService.lagre(TEST_KEY, TEST_VALUE, hold = false)
        val resultat = gcpStorageService.hent(TEST_KEY)
        assertNotNull(resultat)
        assertThat(TEST_VALUE.value).isEqualTo(resultat.value)
    }

    @Test
    fun `hent returnerer null når blob ikke finnes`() {
        assertThat(gcpStorageService.hent(TEST_KEY)).isNull()
    }

    @Test
    fun `hent returnerer null ved StorageException`() {
        assertThat(gcpStorageService.hent(TEST_KEY)).isNull()
    }

    @Test
    fun `slett returnerer false når hent returnerer null`() {
        assertThat(gcpStorageService.slett(TEST_KEY)).isFalse()
    }

    @Test
    fun `slett sletter eksisterende blob vellykket`() {
        gcpStorageService.lagre(TEST_KEY, TEST_VALUE, hold = false)
        assertThat(gcpStorageService.slett(TEST_KEY)).isTrue()
    }

    @Test
    fun `slett returnerer false når sletting kaster StorageException`() {
        assertThat(gcpStorageService.slett(TEST_KEY)).isFalse()
    }

    @Test
    fun `harHold oppdager midlertidig hold`() {
        assertThat(gcpStorageService.harHold(TEST_KEY)).isFalse()
    }

    @Test
    fun `persister returnerer false når hent er null`() {
        assertThat(gcpStorageService.persister(TEST_KEY)).isFalse()
    }

    @Test
    fun `persister setter hold og returnerer true`() {
        gcpStorageService.lagre(TEST_KEY, TEST_VALUE, hold = false)
        assertThat(gcpStorageService.persister(TEST_KEY)).isTrue()
    }

    @Test
    fun `persister returnerer false ved StorageException`() {
        assertThat(gcpStorageService.persister(TEST_KEY)).isFalse()
    }

    @Test
    fun `ready feiler ikke når bøtte finnes`() {
        assertDoesNotThrow { gcpStorageService.ready() }
    }
}
