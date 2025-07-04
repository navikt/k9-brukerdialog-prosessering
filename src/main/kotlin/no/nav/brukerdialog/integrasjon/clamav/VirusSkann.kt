package no.nav.brukerdialog.integrasjon.clamav

interface VirusSkann {
    fun skann(fil: ByteArray)
}
