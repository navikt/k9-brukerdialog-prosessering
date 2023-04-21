package no.nav.k9brukerdialogprosessering.kafka.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.io.Resource
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "no.nav.kafka")
@Validated
data class KafkaProperties(
    val bootstrapServers: String,
    val applicationId: String,
    val retries: Int,
    val retryBackoffMs: Int,
    val replicationFactor: Int,
    val streams: Map<String, KafkaStreamsProperties>,
    val schemaRegistry: KafkaSchemaRegistryProperties,
    val security: KafkaSecurityProperties? = null,
) {
    fun streamProperties(streamPropertyKey: KafkaStreamName): KafkaStreamsProperties {
        return streams[streamPropertyKey.value] ?: throw IllegalStateException("Mangler konfiguration for ${streamPropertyKey.value} streams")
    }
}

data class KafkaStreamsProperties(
    val applicationIdSuffix: String,
    val autoOffsetReset: String
)

data class KafkaSecurityProperties(
    val protocol: String,
    val ssl: KafkaSSlProperties,
)

data class KafkaSSlProperties(
    val keyPassword: String,
    val trustStoreType: String,
    val trustStoreLocation: Resource,
    val trustStorePassword: String,
    val keyStoreLocation: Resource,
    val keyStoreType: String,
    val keyStorePassword: String
)

data class KafkaSchemaRegistryProperties(
    val url: String,
    val user: String,
    val password: String
)

enum class KafkaStreamName(val value: String) {
    // PSB Søknad
    PSB_SØKNAD_PREPROSESSERING("psb-soknad-preprosessering"),
    PSB_SØKNAD_JOURNALFØRING("psb-soknad-journalforing"),
    PSB_SØKNAD_CLEANUP("psb-soknad-cleanup"),

    // PSB Endringsmelding
    PSB_ENDRINGSMELDING_PREPROSESSERING("psb-endringsmelding-preprosessering"),
    PSB_ENDRINGSMELDING_JOURNALFØRING("psb-endringsmelding-journalforing"),
    PSB_ENDRINGSMELDING_CLEANUP("psb-endringsmelding-cleanup"),


    // PILS Søknad
    PILS_SØKNAD_PREPROSESSERING("pils-soknad-preprosessering"),
    PILS_SØKNAD_JOURNALFØRING("pils-soknad-journalforing"),
    PILS_SØKNAD_CLEANUP("pils-soknad-cleanup"),

    // Ettersendelse
    ETTERSENDELSE_PREPROSESSERING("ettersendelse-preprosessering"),
    ETTERSENDELSE_JOURNALFØRING("ettersendelse-journalforing"),
    ETTERSENDELSE_CLEANUP("ettersendelse-cleanup"),

    // Omsorgspenger utvidet rett - kronisk sykt barn
    OMP_UTV_KS_PREPROSESSERING("omp-utv-ks-preprosessering"),
    OMP_UTV_KS_JOURNALFØRING("omp-utv-ks-journalforing"),
    OMP_UTV_KS_CLEANUP("omp-utv-ks-cleanup"),
}
