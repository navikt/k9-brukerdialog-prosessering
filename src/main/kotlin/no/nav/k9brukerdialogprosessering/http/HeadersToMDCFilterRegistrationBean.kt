package no.nav.sifinnsynapi.filter

import no.nav.k9brukerdialogprosessering.http.HeadersToMDCFilterBean
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
