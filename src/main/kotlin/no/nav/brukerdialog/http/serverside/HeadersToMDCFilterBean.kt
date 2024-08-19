package no.nav.brukerdialog.http.serverside

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.brukerdialog.utils.CallIdGenerator
import no.nav.brukerdialog.utils.Constants
import no.nav.brukerdialog.utils.MDCUtil
import no.nav.brukerdialog.utils.NavHeaders
import no.nav.brukerdialog.ytelse.Ytelse.Companion.somYtelse
import no.nav.security.token.support.core.context.TokenValidationContextHolder
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
    private val tokenValidationContextHolder: TokenValidationContextHolder,
    @Value("\${spring.application.name:k9-brukerdialog-prosessering}") private val applicationName: String,
) : GenericFilterBean() {

    companion object {
        private val LOG = LoggerFactory.getLogger(HeadersToMDCFilterBean::class.java)
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpServletRequest = request as HttpServletRequest
        putHeadersToMDC(httpServletRequest)

        logRequest(httpServletRequest)

        chain.doFilter(request, response)

        val httpServletResponse = response as HttpServletResponse
        logResponse(request, httpServletResponse)
    }

    private fun logResponse(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val status = response.status
        val method = request.method
        logger.info("<-- Response $status $method ${request.requestURI}")
    }

    private fun logRequest(request: HttpServletRequest) {
        val jwtToken = tokenValidationContextHolder.getTokenValidationContext().firstValidToken

        val reqMethod = request.method
        val requestURI = request.requestURI
        val issuer = jwtToken?.let { "[${jwtToken.issuer}]" } ?: ""
        val requestMessage = "--> Request $reqMethod $requestURI $issuer"
        logger.info(requestMessage)
    }

    private fun putHeadersToMDC(req: HttpServletRequest) {
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
