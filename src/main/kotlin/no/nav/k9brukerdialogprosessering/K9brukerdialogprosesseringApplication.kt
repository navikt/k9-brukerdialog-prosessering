package no.nav.k9brukerdialogprosessering

import no.nav.k9brukerdialogprosessering.exception.K9brukerdialogprosesseringUncaughtExceptionHandler
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan("no.nav.k9brukerdialogprosessering")
class K9brukerdialogprosesseringApplication

fun main(args: Array<String>) {
	Thread.setDefaultUncaughtExceptionHandler(K9brukerdialogprosesseringUncaughtExceptionHandler())
	runApplication<K9brukerdialogprosesseringApplication>(*args)
}
