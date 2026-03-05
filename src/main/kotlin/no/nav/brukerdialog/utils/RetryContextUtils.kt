package no.nav.brukerdialog.utils

import org.slf4j.Logger
import org.springframework.retry.RetryContext

object RetryContextUtils {
    fun RetryContext.logStreamingRetries(streamName: String, logger: Logger) {
        if (retryCount > 0) {
            logger.warn(
                "$streamName feilet første gang. Forsøker på nytt. Forsøk {}. Siste feil:",
                retryCount+1, lastThrowable
            )
        }
    }
}
