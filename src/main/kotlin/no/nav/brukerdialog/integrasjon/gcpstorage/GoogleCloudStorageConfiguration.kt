package no.nav.brukerdialog.integrasjon.gcpstorage

import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GoogleCloudStorageConfiguration {

    @Bean
    fun gcpStorage(): Storage {
        return StorageOptions.getDefaultInstance().service
    }
}
