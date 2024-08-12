package no.nav.brukerdialog.utils

object PathUtils {
    fun pdfPath(soknadId: String, prefix: String) = "${System.getProperty("user.dir")}/$prefix-pdf-$soknadId.pdf"
}
