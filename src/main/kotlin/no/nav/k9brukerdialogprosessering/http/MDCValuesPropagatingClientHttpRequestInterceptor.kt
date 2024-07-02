package no.nav.k9brukerdialogprosessering.http

import no.nav.k9brukerdialogprosessering.utils.Constants.CALL_ID
import no.nav.k9brukerdialogprosessering.utils.Constants.CORRELATION_ID
import no.nav.k9brukerdialogprosessering.utils.Constants.NAV_CONSUMER_ID
import no.nav.k9brukerdialogprosessering.utils.MDCUtil.callIdOrNew
import no.nav.k9brukerdialogprosessering.utils.NavHeaders.X_CORRELATION_ID
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import java.io.IOException

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class MDCValuesPropagatingClientHttpRequestInterceptor : ClientHttpRequestInterceptor {
    @Throws(IOException::class)
    override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
        propagerFraMDC(request, CORRELATION_ID, NAV_CONSUMER_ID)
        return execution.execute(request, body)
    }

    companion object {
        private fun propagerFraMDC(request: HttpRequest, vararg keys: String) {
            for (key in keys) {
                val value = MDC.get(key)
                if (value != null) {
                    request.headers.add(key, value)
                }
            }
            request.headers.add(CALL_ID, callIdOrNew())
            request.headers.add(X_CORRELATION_ID, callIdOrNew())
        }
    }
}
