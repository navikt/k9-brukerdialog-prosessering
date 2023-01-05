package no.nav.k9brukerdialogprosessering

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
class K9brukerdialogprosesseringApplication

fun main(args: Array<String>) {
	runApplication<K9brukerdialogprosesseringApplication>(*args)
}
