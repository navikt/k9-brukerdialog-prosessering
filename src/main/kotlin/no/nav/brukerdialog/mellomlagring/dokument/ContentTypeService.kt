package no.nav.brukerdialog.mellomlagring.dokument

import org.apache.tika.Tika
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType

class ContentTypeService {
    companion object {
        val SUPPORTED_CONTENT_TYPES = listOf(
            MediaType.APPLICATION_PDF,
            MediaType.IMAGE_JPEG,
            MediaType.IMAGE_PNG,
        )

        fun Tika.detectOrNull(content: ByteArray): String? {
            return try {
                detect(content)
            } catch (cause: Throwable) {
                logger.warn("Kunne ikke detektere filtype for dokument", cause)
                null
            }
        }

        private val logger: Logger = LoggerFactory.getLogger(ContentTypeService::class.java)
    }

    private val tika = Tika()

    fun isSupportedContentType(contentType: String): Boolean {
        return MediaType.parseMediaType(contentType).let { parsedContentType ->
            if (SUPPORTED_CONTENT_TYPES.contains(parsedContentType)) {
                true
            } else {
                logger.error(
                    "Ikke støttet contentType: {}. Støttet contentType: {}",
                    parsedContentType,
                    SUPPORTED_CONTENT_TYPES
                )
                false
            }
        }
    }

    fun isWhatItSeems(
        content: ByteArray,
        contentType: String,
    ): Boolean {
        val parsedContentType = MediaType.parseMediaType(contentType)
        return parsedContentType.toString().equals(tika.detectOrNull(content), ignoreCase = true)
    }
}
