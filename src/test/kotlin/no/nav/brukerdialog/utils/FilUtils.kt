package no.nav.brukerdialog.utils

import org.springframework.util.ResourceUtils

object FilUtils {
    fun hentFil(filnavn: String) = ResourceUtils.getFile("classpath:filer/$filnavn")
}
