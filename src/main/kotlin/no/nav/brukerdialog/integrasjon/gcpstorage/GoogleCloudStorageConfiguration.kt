package no.nav.brukerdialog.integrasjon.gcpstorage

import com.google.cloud.NoCredentials
import com.google.cloud.storage.BucketInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile(value = ["vtp", "dev-gcp", "prod-gcp"]) // Aktiv i dev-gcp og prod-gcp profiler
class GoogleCloudStorageConfiguration(
    @param:Value("\${GCP_STORAGE_BUCKET_NAVN}") val bucketName: String,
    @param:Value("\${VTP_GCP_STORAGE_HOST:http://gcp-storage:4443}") private val vtpGCPStorageHost: String,
) {
    private companion object {
        private val logger = LoggerFactory.getLogger(GoogleCloudStorageConfiguration::class.java)
    }

    @Bean("storage")
    @Profile(value = ["prod-gcp", "dev-gcp"])
    fun storage(): Storage {
        return StorageOptions.getDefaultInstance().service
    }

    @Bean("storage")
    @Profile(value = ["vtp"])
    fun storageNoCredentials(): Storage {
        val service = StorageOptions.newBuilder()
            .setProjectId("vtp")
            // localhost
            .setHost(vtpGCPStorageHost) // Juster host om nødvendig for din lokale oppsett
            .setCredentials(NoCredentials.getInstance())
            .build()
            .service

        logger.info("Kobler til GCP Storage med host: $vtpGCPStorageHost")

        service.get(bucketName) ?: run {
            // Hvis bøtten ikke finnes, oppretter vi den
            logger.info("Bucket $bucketName ikke funnet. Oppretter ny bucket.")
            service.create(BucketInfo.newBuilder(bucketName).build())
        }
        return service
    }
}
