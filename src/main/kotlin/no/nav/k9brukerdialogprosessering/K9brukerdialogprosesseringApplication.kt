package no.nav.k9brukerdialogprosessering

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan("no.nav.k9brukerdialogprosessering")
class K9brukerdialogprosesseringApplication

private val logger = LoggerFactory.getLogger(K9brukerdialogprosesseringApplication::class.java)

fun main(args: Array<String>) {
	Thread.setDefaultUncaughtExceptionHandler { t: Thread, e: Throwable ->
		logger.error("Uncaught exception in thread ${t.name}", e)
	}
	runApplication<K9brukerdialogprosesseringApplication>(*args)
}
