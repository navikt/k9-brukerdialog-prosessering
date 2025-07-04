package no.nav.brukerdialog

import com.google.cloud.NoCredentials
import com.google.cloud.storage.BucketInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import no.nav.brukerdialog.utils.TestContainers
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class GcsStorageTestConfiguration(
    @Value("\${no.nav.mellomlagring.gcp_storage_bucket_navn}") private val bucket: String,
) {

    @Bean
    fun storage(): Storage {
        val storage = StorageOptions.newBuilder()
            .setHost(TestContainers.FAKE_GCS_SERVER_CONTAINER.url())
            .setProjectId("test-project") // Dummy project ID
            .setCredentials(NoCredentials.getInstance())
            .build()
            .service

        // Oppretter en test-bøtte i emulatoren
        storage.get(bucket) ?: run {
            // Hvis bøtten ikke finnes, oppretter vi den
            storage.create(BucketInfo.of(bucket))
        }
        return storage
    }
}
