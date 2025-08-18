package no.nav.brukerdialog.integrasjon.gcpstorage

import com.google.cloud.NoCredentials
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile(value = ["vtp", "dev-gcp", "prod-gcp"]) // Aktiv i dev-gcp og prod-gcp profiler
class GoogleCloudStorageConfiguration {

    @Bean("storage")
    @Profile(value = ["prod-gcp", "dev-gcp"])
    fun storage(): Storage {
        return StorageOptions.getDefaultInstance().service
    }

    @Bean("storage")
    @Profile(value = ["vtp"])
    fun storageNoCredentials(): Storage {
        return StorageOptions.newBuilder()
            .setProjectId("vtp")
            // localhost
            .setHost("https://localhost:4443") // Juster host om nødvendig for din lokale oppsett
            .setCredentials(NoCredentials.getInstance())
            .build()
            .service
    }
}
