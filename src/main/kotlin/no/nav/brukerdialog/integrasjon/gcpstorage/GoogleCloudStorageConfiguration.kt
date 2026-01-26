package no.nav.brukerdialog.integrasjon.gcpstorage

import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile(value = ["dev-gcp", "prod-gcp"])
class GoogleCloudStorageConfiguration(
    @param:Value("\${GCP_STORAGE_BUCKET_NAVN}") val bucketName: String,
) {
    private companion object {
        private val logger = LoggerFactory.getLogger(GoogleCloudStorageConfiguration::class.java)
    }

    @Bean("storage")
    fun storage(): Storage {
        return StorageOptions.getDefaultInstance().service
    }
}
