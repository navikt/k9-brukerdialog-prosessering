package no.nav.brukerdialog

import no.nav.brukerdialog.exception.K9brukerdialogprosesseringUncaughtExceptionHandler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan("no.nav.brukerdialog")
class K9brukerdialogprosesseringApplication

fun main(args: Array<String>) {
	Thread.setDefaultUncaughtExceptionHandler(K9brukerdialogprosesseringUncaughtExceptionHandler())
	runApplication<K9brukerdialogprosesseringApplication>(*args)
}
