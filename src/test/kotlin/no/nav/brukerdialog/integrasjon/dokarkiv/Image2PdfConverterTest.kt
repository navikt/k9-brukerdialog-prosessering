package no.nav.brukerdialog.integrasjon.dokarkiv

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.util.ResourceUtils
import java.io.File
import java.lang.NullPointerException

class Image2PdfConverterTest {

    @Test
    fun `Generert skalerte bilder`() {
        scale(resourceName = "hoyopplost.jpg", name = "hoyopplost", format = "jpg")
        scale(resourceName = "widescreen.jpg", name = "widescreen", format = "jpg")
        scale(resourceName = "legeerklaering_iPhone_XS.jpg", name = "legeerklaering", format = "jpg")
        scale(resourceName = "grafana-board.png", name = "grafana-board", format = "png")

        // PDF feiler ikke
        scale(resourceName = "test.pdf", name = "test", format = "pdf")
    }

    @Test
    fun `skalering av gray-scale dokument skal ikke feile`() {
        assertDoesNotThrow {
            scale(resourceName = "imageMedFeil.jpeg", name = "feilende")
        }
    }

    private fun scale(
        resourceName: String,
        format: String = "jpeg",
        name: String,
    ) {
        val fileResource = ResourceUtils.getFile("classpath:filer/$resourceName")
        val image = Image2PDFConverter().convertToPDF(fileResource.readBytes(), format)
        val pathToWrite = "${System.getProperty("user.dir")}/scaled-image-$name.pdf"
        val file = File(pathToWrite)
        file.writeBytes(image)
    }
}
