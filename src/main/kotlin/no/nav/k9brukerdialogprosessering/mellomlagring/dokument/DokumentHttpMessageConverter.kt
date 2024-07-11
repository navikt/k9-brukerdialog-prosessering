package no.nav.k9brukerdialogprosessering.mellomlagring.dokument

import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.converter.AbstractHttpMessageConverter

class DokumentHttpMessageConverter : AbstractHttpMessageConverter<Dokument>() {
    override fun supports(clazz: Class<*>): Boolean {
        return Dokument::class.java == clazz
    }

    override fun readInternal(clazz: Class<out Dokument>, inputMessage: HttpInputMessage): Dokument {
        val headers = inputMessage.headers
        val contentType = headers.contentType?.toString() ?: "application/octet-stream"
        val body = inputMessage.body.readBytes()

        return Dokument(
            content = body,
            contentType = contentType,
            title = headers.getFirst("Content-Disposition") ?: "Unknown"
        )
    }

    override fun writeInternal(dokument: Dokument, outputMessage: HttpOutputMessage) {
        outputMessage.headers.contentType = MediaType.parseMediaType(dokument.contentType)
        outputMessage.body.write(dokument.content)
    }
}
