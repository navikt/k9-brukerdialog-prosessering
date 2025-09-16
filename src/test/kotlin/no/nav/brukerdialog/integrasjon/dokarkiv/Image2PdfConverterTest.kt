package no.nav.brukerdialog.integrasjon.dokarkiv

import no.nav.brukerdialog.pdf.PDFGenerator
import no.nav.brukerdialog.utils.PathUtils.pdfPath
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.util.ResourceUtils
import java.io.File

class Image2PdfConverterTest {

    @Test
    fun `Generert skalerte bilder`() {
        scale(resourceName = "hoyopplost.jpg", name = "hoyopplost", format = "jpg")
        scale(resourceName = "widescreen.jpg", name = "widescreen", format = "jpg")
        scale(resourceName = "legeerklaering_iPhone_XS.jpg", name = "legeerklaering", format = "jpg")
        scale(resourceName = "grafana-board.png", name = "grafana-board", format = "png")
    }

    @Test
    fun `skalering av gray-scale dokument skal ikke feile`() {
        assertDoesNotThrow {
            scale(resourceName = "imageMedFeil.jpeg", name = "feilende")
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["bad_crc.png", "ihdr_corrupt.png", "sig_corrupt.png", "truncated.png"])
    fun `skal generere pdf for korrupt fil ved konvertering`(filnavn: String) {
        val fileResource = ResourceUtils.getFile("classpath:filer/uleselig/$filnavn")
        val pdfData = Image2PDFConverter(generator).convertToPDF(fileResource.readBytes(), "png", true)

        File(pdfPath(soknadId = filnavn, prefix = "uleselig")).writeBytes(pdfData)
    }

    private fun scale(
        resourceName: String,
        format: String = "jpeg",
        name: String,
    ) {
        val fileResource = ResourceUtils.getFile("classpath:filer/$resourceName")
        val image = Image2PDFConverter(generator).convertToPDF(fileResource.readBytes(), format, true)
        val pathToWrite = "${System.getProperty("user.dir")}/scaled-image-$name.pdf"
        val file = File(pathToWrite)
        file.writeBytes(image)
    }

    private companion object {
        val generator = PDFGenerator()
    }
}
