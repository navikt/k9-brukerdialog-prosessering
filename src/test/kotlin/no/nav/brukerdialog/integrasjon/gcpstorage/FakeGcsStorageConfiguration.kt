package no.nav.brukerdialog.integrasjon.gcpstorage

import com.google.cloud.NoCredentials
import com.google.cloud.storage.BucketInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import io.aiven.testcontainers.fakegcsserver.FakeGcsServerContainer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("vtp")
class FakeGcsStorageConfiguration(
    @Value("\${GCP_STORAGE_BUCKET_NAVN}") private val bucketName: String,
) {
    private companion object {
        private val logger = LoggerFactory.getLogger(FakeGcsStorageConfiguration::class.java)
    }

    @Bean
    fun fakeGcsServerContainer(): FakeGcsServerContainer {
        val container = FakeGcsServerContainer()
        container.start()
        logger.info("Started Fake GCS Server at: ${container.url()}")
        return container
    }

    @Bean("storage")
    fun storage(fakeGcsServerContainer: FakeGcsServerContainer): Storage {
        val storage = StorageOptions.newBuilder()
            .setHost(fakeGcsServerContainer.url())
            .setProjectId("vtp")
            .setCredentials(NoCredentials.getInstance())
            .build()
            .service

        logger.info("Kobler til Fake GCS Storage med host: ${fakeGcsServerContainer.url()}")

        storage.get(bucketName) ?: run {
            logger.info("Bucket $bucketName ikke funnet. Oppretter ny bucket.")
            storage.create(BucketInfo.newBuilder(bucketName).build())
        }

        return storage
    }
}