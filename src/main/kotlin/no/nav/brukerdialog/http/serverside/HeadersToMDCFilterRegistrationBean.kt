package no.nav.brukerdialog.http.serverside

import org.slf4j.LoggerFactory
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.stereotype.Component

@Component
class HeadersToMDCFilterRegistrationBean(headersFilter: HeadersToMDCFilterBean?) : FilterRegistrationBean<HeadersToMDCFilterBean?>() {
    companion object {
        private val logger = LoggerFactory.getLogger(HeadersToMDCFilterRegistrationBean::class.java)
        private const val ALWAYS = "/*"
    }

    init {
        filter = headersFilter
        urlPatterns = listOf(ALWAYS)
        logger.info("Registrert filter {}", this.javaClass.simpleName)
    }
}
