package no.nav.k9brukerdialogprosessering.utils

import no.nav.k9brukerdialogprosessering.RetryTemplateConfiguration.Companion.MAX_ATTEMPTS
import org.slf4j.Logger
import org.springframework.retry.RetryContext

object RetryContextUtils {
    fun RetryContext.logHttpRetries(logger: Logger, url: String) {
        if (retryCount > 0) {
            logger.warn(
                "Gjenforsøker HTTP forespørsel for URL {}. Forsøk {} av {}. Siste feil:",
                url, retryCount, MAX_ATTEMPTS, lastThrowable
            )
        }
    }
}
