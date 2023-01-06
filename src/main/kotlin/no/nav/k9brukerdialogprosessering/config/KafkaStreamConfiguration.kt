package no.nav.k9brukerdialogprosessering.config

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import org.apache.kafka.streams.KafkaStreams
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafkaStreams
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import org.springframework.kafka.config.StreamsBuilderFactoryBeanConfigurer
import org.springframework.kafka.listener.KafkaListenerErrorHandler
import org.springframework.kafka.listener.ListenerExecutionFailedException
import org.springframework.messaging.Message


@Configuration
@EnableKafkaStreams
class KafkaStreamConfiguration {
    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(KafkaStreamConfiguration::class.java)
    }

    @Bean
    fun streamsBuilderFactoryBeanConfigurer(): StreamsBuilderFactoryBeanConfigurer {
        return StreamsBuilderFactoryBeanConfigurer { fb: StreamsBuilderFactoryBean ->
            fb.setStateListener { newState: KafkaStreams.State, oldState: KafkaStreams.State ->
                logger.info("State transition from $oldState to $newState")
            }
        }
    }

    @Bean
    fun errorHandler(): KafkaListenerErrorHandler {
        return KafkaListenerErrorHandler { message: Message<*>, exception: ListenerExecutionFailedException ->
            val offset = message.headers.getValue("kafka_offset")
            val topic = message.headers.getValue("kafka_receivedTopic")
            when (exception.cause) {
                is MissingKotlinParameterException -> {
                    logger.error("Deserialisering av melding fra topic: $topic med offset: $offset feilet. Se securelogs for payload.")
                    // TODO: Logg payload til securelogs
                }

                else -> {
                    logger.error(
                        "Ukjent feil ved behandling av melding fra topic: $topic med offset: $offset feilet. Årsak: ",
                        exception
                    )
                }
            }
            throw KafkaListenerConsumingException("Feil ved konsumering av melding")
        }
    }
}

class KafkaListenerConsumingException(override val message: String) : RuntimeException(message)
