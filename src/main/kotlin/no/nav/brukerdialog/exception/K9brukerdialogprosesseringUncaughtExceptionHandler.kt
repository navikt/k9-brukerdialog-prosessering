package no.nav.brukerdialog.exception

import org.slf4j.LoggerFactory

class K9brukerdialogprosesseringUncaughtExceptionHandler : Thread.UncaughtExceptionHandler {
    private companion object {
        private val logger = LoggerFactory.getLogger(K9brukerdialogprosesseringUncaughtExceptionHandler::class.java)
    }
    override fun uncaughtException(t: Thread, e: Throwable) {
        // Handle the uncaught exception here
        logger.error("Uncaught exception in thread ${t.name}", e)
    }
}
