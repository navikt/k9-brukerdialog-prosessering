package no.nav.brukerdialog.integrasjon.gcpstorage

import com.google.cloud.NoCredentials
import com.google.cloud.storage.BucketInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile(value = ["vtp", "dev-gcp", "prod-gcp"]) // Aktiv i dev-gcp og prod-gcp profiler
class GoogleCloudStorageConfiguration {
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
            .setHost("http://localhost:4443") // Juster host om nødvendig for din lokale oppsett
            .setCredentials(NoCredentials.getInstance())
            .build()
            .service

        service.get("k9-mellomlagring") ?: run {
            // Hvis bøtten ikke finnes, oppretter vi den
            logger.info("Bucket k9-mellomlagring ikke funnet. Oppretter ny bucket.")
            service.create(BucketInfo.newBuilder("k9-mellomlagring").build())
        }
        return service
    }
}
