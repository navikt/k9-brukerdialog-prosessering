package no.nav.brukerdialog.vtp

import no.nav.brukerdialog.K9brukerdialogprosesseringApplication
import no.nav.brukerdialog.exception.K9brukerdialogprosesseringUncaughtExceptionHandler
import no.nav.brukerdialog.utils.KAFKA_TOPICS
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
	Thread.setDefaultUncaughtExceptionHandler(K9brukerdialogprosesseringUncaughtExceptionHandler())

	runApplication<K9brukerdialogprosesseringApplication>(*args) {
		addInitializers(KafkaTopicInitializer())
	}
}

class KafkaTopicInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
	override fun initialize(applicationContext: ConfigurableApplicationContext) {
		val env = applicationContext.environment

		val adminClientConfig = mapOf(
			AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to env.getProperty("spring.kafka.bootstrap-servers", "kafka:9093"),
			AdminClientConfig.SECURITY_PROTOCOL_CONFIG to env.getProperty("no.nav.kafka.security.ssl.protocol", "SSL"),
			"ssl.truststore.location" to env.getProperty("no.nav.kafka.security.ssl.trust-store-location")?.removePrefix("file:"),
			"ssl.truststore.password" to env.getProperty("no.nav.kafka.security.ssl.trust-store-password"),
			"ssl.keystore.location" to env.getProperty("no.nav.kafka.security.ssl.key-store-location")?.removePrefix("file:"),
			"ssl.keystore.password" to env.getProperty("no.nav.kafka.security.ssl.key-store-password"),
			"ssl.keystore.type" to env.getProperty("no.nav.kafka.security.ssl.key-store-type")
		)

		AdminClient.create(adminClientConfig).use { adminClient ->
			val topics = KAFKA_TOPICS.map { NewTopic(it, 1, 1.toShort()) }
			try {
				adminClient.createTopics(topics).all().get(30, TimeUnit.SECONDS)
				println("Created ${topics.size} Kafka topics")
			} catch (e: Exception) {
				println("Topics may already exist: ${e.message}")
			}
		}
	}
}

