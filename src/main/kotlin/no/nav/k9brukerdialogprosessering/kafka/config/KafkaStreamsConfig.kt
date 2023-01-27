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
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.KafkaStreamsConfiguration
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import org.springframework.kafka.streams.KafkaStreamsMicrometerListener
import org.springframework.kafka.support.serializer.JsonSerde

/**
 * Configuration for Kafka Streams.
 */
@Configuration
class KafkaStreamsConfig(
    private val meterRegistry: MeterRegistry,
    private val kafkaProperties: KafkaProperties,
) {
    @Bean(name = [PSB_STREAMS_BUILDER_BEAN_NAME])
    fun psbKafkaStreamsBuilder(): StreamsBuilderFactoryBean {
        val psbStreamProps =
            kafkaProperties.streams["psb"] ?: throw IllegalStateException("Mangler konfiguration for psb streams")
        val props = commonStreamsConfigProperties(kafkaProperties.security, kafkaProperties.schemaRegistry)
        props[StreamsConfig.APPLICATION_ID_CONFIG] = "${kafkaProperties.applicationId}-psb-stream"
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = psbStreamProps.autoOffsetReset

        val streamsBuilderFactoryBean = StreamsBuilderFactoryBean(KafkaStreamsConfiguration(props))
        streamsBuilderFactoryBean.configure()

        return streamsBuilderFactoryBean
    }

    /*@Bean(name = [OMP_STREAMS_BUILDER_BEAN_NAME])
    fun ompKafkaStreamsBuilder(): StreamsBuilderFactoryBean {
        val app2StreamsConfigProperties = commonStreamsConfigProperties()
        app2StreamsConfigProperties[StreamsConfig.APPLICATION_ID_CONFIG] = "$applicationId-omp-stream"
        return StreamsBuilderFactoryBean(KafkaStreamsConfiguration(app2StreamsConfigProperties))
    }*/


    private fun commonStreamsConfigProperties(
        security: KafkaSecurityProperties?,
        schemaRegistry: KafkaSchemaRegistryProperties,
    ): MutableMap<String, Any> {
        val props: MutableMap<String, Any> = HashMap()
        // StreamsConfig
        props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaProperties.bootstrapServers
        props[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.StringSerde::class.java
        props[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = JsonSerde<String>()::class.java
        props[StreamsConfig.REPLICATION_FACTOR_CONFIG] = kafkaProperties.replicationFactor
        props[CommonClientConfigs.RETRIES_CONFIG] = kafkaProperties.retries
        props[StreamsConfig.RETRY_BACKOFF_MS_CONFIG] = kafkaProperties.retryBackoffMs
        props[StreamsConfig.PROCESSING_GUARANTEE_CONFIG] = StreamsConfig.EXACTLY_ONCE_V2

        // Producer Config
        props[ProducerConfig.ACKS_CONFIG] = "all"
        props[ProducerConfig.RETRIES_CONFIG] = kafkaProperties.retries



        security?.let {
            props[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = it.protocol
            props[SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG] = ""
            props[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = it.ssl.trustStoreLocation.file.absolutePath
            props[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = it.ssl.trustStorePassword
            props[SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG] = it.ssl.trustStoreType
            props[SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG] = it.ssl.keyStoreLocation.file.absolutePath
            props[SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG] = it.ssl.keyStorePassword
            props[SslConfigs.SSL_KEYSTORE_TYPE_CONFIG] = it.ssl.keyStoreType
        }

        schemaRegistry.let {
            props[KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG] = it.url
            props[KafkaAvroDeserializerConfig.BASIC_AUTH_CREDENTIALS_SOURCE] = "USER_INFO"
            props[KafkaAvroDeserializerConfig.USER_INFO_CONFIG] = "${it.user}:${it.password}"
            props[KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG] = "true"
        }

        return props
    }

    private fun StreamsBuilderFactoryBean.configure() {
        setStreamsUncaughtExceptionHandler { throwable: Throwable ->
            logger.info("Setting StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD")
            when(throwable) {
                is org.apache.kafka.streams.errors.StreamsException -> {
                    logger.error("StreamsException: ${throwable.message}")
                }
                else -> {
                    logger.error("Exception: ${throwable.message}")
                }
            }
            StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD
        }

        setStateListener { newState: KafkaStreams.State, oldState: KafkaStreams.State ->
            logger.info("State transition from $oldState to $newState")
        }

        addListener(KafkaStreamsMicrometerListener(meterRegistry))
    }

    companion object {
        private val logger = LoggerFactory.getLogger(KafkaStreamsConfig::class.java)
        const val PSB_STREAMS_BUILDER_BEAN_NAME = "psbKafkaStreamsBuilder"
        const val OMP_STREAMS_BUILDER_BEAN_NAME = "ompKafkaStreamsBuilder"
    }
}
