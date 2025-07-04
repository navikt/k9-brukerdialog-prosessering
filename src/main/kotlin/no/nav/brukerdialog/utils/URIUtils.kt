package no.nav.brukerdialog.utils

import java.net.URI

object URIUtils {
    fun URI.dokumentId() = this.toString().substringAfterLast("/")
}
