package no.nav.k9brukerdialogprosessering.kafka.config

import no.nav.k9brukerdialogprosessering.kafka.config.CommonKafkaConfig.Companion.kafkaTemplate
import no.nav.k9brukerdialogprosessering.kafka.config.CommonKafkaConfig.Companion.kafkaTransactionManager
import no.nav.k9brukerdialogprosessering.kafka.config.CommonKafkaConfig.Companion.producerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@Configuration
class KafkaConfig(
    private val kafkaProperties: KafkaProperties,
) {

    @Bean
    fun aivenProducerFactory(): ProducerFactory<String, String> = producerFactory(kafkaProperties)

    @Bean
    fun aivenKafkaTemplate(aivenProducerFactory: ProducerFactory<String, String>): KafkaTemplate<String, String> =
        kafkaTemplate(aivenProducerFactory, kafkaProperties)

    @Bean
    fun kafkaTransactionManager(aivenProducerFactory: ProducerFactory<String, String>) =
        kafkaTransactionManager(aivenProducerFactory, kafkaProperties)
}
