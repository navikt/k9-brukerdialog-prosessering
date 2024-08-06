package no.nav.k9brukerdialogprosessering.http

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import no.nav.k9brukerdialogprosessering.api.ytelse.Ytelse.Companion.somYtelse
import no.nav.k9brukerdialogprosessering.utils.CallIdGenerator
import no.nav.k9brukerdialogprosessering.utils.Constants
import no.nav.k9brukerdialogprosessering.utils.MDCUtil
import no.nav.k9brukerdialogprosessering.utils.NavHeaders
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean
import java.io.IOException

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class HeadersToMDCFilterBean(
    private val generator: CallIdGenerator,
    @Value("\${spring.application.name:k9-brukerdialog-prosessering}") private val applicationName: String) : GenericFilterBean() {

    companion object {
        private val LOG = LoggerFactory.getLogger(HeadersToMDCFilterBean::class.java)
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        putValues(HttpServletRequest::class.java.cast(request))
        chain.doFilter(request, response)
    }

    private fun putValues(req: HttpServletRequest) {
        try {
            MDCUtil.toMDC(Constants.NAV_CONSUMER_ID, req.getHeader(Constants.NAV_CONSUMER_ID), applicationName)
            MDCUtil.toMDC(Constants.CORRELATION_ID, req.getHeader(NavHeaders.X_CORRELATION_ID), generator.create())
            MDCUtil.toMDC(Constants.YTELSE, req.getHeader(NavHeaders.BRUKERDIALOG_YTELSE)?.somYtelse())
        } catch (e: Exception) {
            LOG.warn("Feil ved setting av MDC-verdier for {}, MDC-verdier er inkomplette", req.requestURI, e)
        }
    }

    override fun toString(): String {
        return javaClass.simpleName + " [generator=" + generator + ", applicationName=" + applicationName + "]"
    }
}
