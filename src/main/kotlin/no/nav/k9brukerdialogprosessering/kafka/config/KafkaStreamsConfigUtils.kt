package no.nav.k9brukerdialogprosessering.kafka.config

import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import io.micrometer.core.instrument.MeterRegistry
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler
import org.slf4j.LoggerFactory
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import org.springframework.kafka.streams.KafkaStreamsMicrometerListener
import org.springframework.kafka.support.serializer.JsonSerde

/**
 * Configuration for Kafka Streams.
 */
object KafkaStreamsConfigUtils {

    private val logger = LoggerFactory.getLogger(KafkaStreamsConfigUtils::class.java)
    fun commonStreamsConfigProperties(
        kafkaProperties: KafkaProperties,
        streamPropertyKey: KafkaStreamName,
    ): MutableMap<String, Any> {
        val props: MutableMap<String, Any> = HashMap()
        val streamProps = kafkaProperties.streamProperties(streamPropertyKey)

        // StreamsConfig
        props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaProperties.bootstrapServers
        props[StreamsConfig.APPLICATION_ID_CONFIG] = "${kafkaProperties.applicationId}${streamProps.applicationIdSuffix}"
        props[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.StringSerde::class.java
        props[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = JsonSerde<String>()::class.java
        props[StreamsConfig.REPLICATION_FACTOR_CONFIG] = kafkaProperties.replicationFactor
        props[CommonClientConfigs.RETRIES_CONFIG] = kafkaProperties.retries
        props[StreamsConfig.RETRY_BACKOFF_MS_CONFIG] = kafkaProperties.retryBackoffMs
        props[StreamsConfig.PROCESSING_GUARANTEE_CONFIG] = StreamsConfig.EXACTLY_ONCE_V2

        // Consumer Config
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = streamProps.autoOffsetReset

        // Producer Config
        props[ProducerConfig.ACKS_CONFIG] = "all"
        props[ProducerConfig.RETRIES_CONFIG] = kafkaProperties.retries



        kafkaProperties.security?.let {
            props[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = it.protocol
            props[SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG] = ""
            props[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = it.ssl.trustStoreLocation.file.absolutePath
            props[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = it.ssl.trustStorePassword
            props[SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG] = it.ssl.trustStoreType
            props[SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG] = it.ssl.keyStoreLocation.file.absolutePath
            props[SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG] = it.ssl.keyStorePassword
            props[SslConfigs.SSL_KEYSTORE_TYPE_CONFIG] = it.ssl.keyStoreType
        }

        kafkaProperties.schemaRegistry.let {
            props[KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG] = it.url
            props[KafkaAvroDeserializerConfig.BASIC_AUTH_CREDENTIALS_SOURCE] = "USER_INFO"
            props[KafkaAvroDeserializerConfig.USER_INFO_CONFIG] = "${it.user}:${it.password}"
            props[KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG] = "true"
        }

        return props
    }

    fun StreamsBuilderFactoryBean.configure(streamPropertyKey: KafkaStreamName, meterRegistry: MeterRegistry) {
        setStreamsUncaughtExceptionHandler { throwable: Throwable ->
            logger.info("Setting StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD")
            logger.error("Failed to stream message:", throwable)
            StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD
        }

        setStateListener { newState: KafkaStreams.State, oldState: KafkaStreams.State ->
            logger.info("${streamPropertyKey.value} State transition from $oldState to $newState")
        }

        addListener(KafkaStreamsMicrometerListener(meterRegistry))
    }
}
