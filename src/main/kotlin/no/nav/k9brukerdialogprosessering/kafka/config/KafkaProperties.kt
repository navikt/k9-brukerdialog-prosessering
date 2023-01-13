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
)

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
