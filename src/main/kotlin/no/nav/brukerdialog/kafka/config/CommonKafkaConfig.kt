package no.nav.brukerdialog.kafka.config

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SslConfigs
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.transaction.KafkaTransactionManager

class CommonKafkaConfig {
    companion object {
        fun commonConfig(kafkaConfigProps: KafkaProperties) = mutableMapOf<String, Any>().apply {
            put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigProps.bootstrapServers)
        } + securityConfig(kafkaConfigProps.security)

        fun securityConfig(securityProps: KafkaSecurityProperties?) = mutableMapOf<String, Any>().apply {
            put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "") // Disable server host name verification
            securityProps?.let { props ->
                val sslProps = props.ssl

                put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, props.protocol)
                put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, sslProps.trustStoreLocation.file.absolutePath)
                put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, sslProps.trustStorePassword)
                put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, sslProps.trustStoreType)

                put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, sslProps.keyStoreLocation.file.absolutePath)
                put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, sslProps.keyStorePassword)
                put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, sslProps.keyStoreType)
            }
        }

        fun producerFactory(kafkaConfigProps: KafkaProperties): ProducerFactory<String, String> {
            val producerProps = kafkaConfigProps.producer
            val factory = DefaultKafkaProducerFactory<String, String>(
                mutableMapOf<String, Any>(
                    ProducerConfig.CLIENT_ID_CONFIG to producerProps.clientId,
                    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to producerProps.keySerializer,
                    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to producerProps.valueSerializer,
                    ProducerConfig.RETRIES_CONFIG to producerProps.retries,
                ) + commonConfig(kafkaConfigProps)
            )

            factory.setTransactionIdPrefix(producerProps.transactionIdPrefix)
            return factory
        }

        fun kafkaTemplate(producerFactory: ProducerFactory<String, String>, kafkaProperties: KafkaProperties) =
            KafkaTemplate(producerFactory).apply {
                setTransactionIdPrefix(kafkaProperties.producer.transactionIdPrefix)
            }

        fun kafkaTransactionManager(
            producerFactory: ProducerFactory<String, String>,
            kafkaConfigProps: KafkaProperties,
        ) =
            KafkaTransactionManager(producerFactory).apply {
                setTransactionIdPrefix(kafkaConfigProps.producer.transactionIdPrefix)
            }
    }
}
