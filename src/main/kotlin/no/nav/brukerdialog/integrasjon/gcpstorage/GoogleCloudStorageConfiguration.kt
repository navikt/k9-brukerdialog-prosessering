package no.nav.brukerdialog.integrasjon.gcpstorage

import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile(value = ["dev-gcp", "prod-gcp"]) // Aktiv i dev-gcp og prod-gcp profiler
class GoogleCloudStorageConfiguration {

    @Bean
    fun storage(): Storage {
        return StorageOptions.getDefaultInstance().service
    }
}
