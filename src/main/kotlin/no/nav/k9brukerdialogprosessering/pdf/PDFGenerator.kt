package no.nav.k9brukerdialogprosessering.pdf

import com.github.jknack.handlebars.Context
import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Helper
import com.github.jknack.handlebars.Template
import com.github.jknack.handlebars.context.MapValueResolver
import com.github.jknack.handlebars.io.ClassPathTemplateLoader
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import com.openhtmltopdf.slf4j.Slf4jLogger
import com.openhtmltopdf.util.XRLog
import no.nav.k9brukerdialogprosessering.common.Ytelse
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

@Component
class PDFGenerator {

    init {
        XRLog.setLoggerImpl(Slf4jLogger())
    }

    private companion object {
        private val ROOT = "handlebars"
        private val REGULAR_FONT = ClassPathResource("${ROOT}/fonts/SourceSansPro-Regular.ttf").inputStream.readAllBytes()
        private val BOLD_FONT = ClassPathResource("${ROOT}/fonts/SourceSansPro-Bold.ttf").inputStream.readAllBytes()
        private val ITALIC_FONT = ClassPathResource("${ROOT}/fonts/SourceSansPro-Italic.ttf").inputStream.readAllBytes()
        private val bilder: Map<String, String> = emptyMap()
        private val handlebars = configureHandlebars()

        private fun configureHandlebars() = Handlebars(ClassPathTemplateLoader("/${ROOT}")).apply {
            imageHelper()
            equalsHelper()
            equalsNumberHelper()
            fritekstHelper()
            jaNeiSvarHelper()
            infiniteLoops(true)
        }

        protected fun loadPng(name: String): String {
            val bytes = ClassPathResource("${ROOT}/images/$name.png").inputStream.readAllBytes()
            val base64string = Base64.getEncoder().encodeToString(bytes)
            return "data:image/png;base64,$base64string"
        }

        private fun Handlebars.jaNeiSvarHelper() {
            registerHelper("jaNeiSvar", Helper<Boolean> { context, _ ->
                if (context == true) "Ja" else "Nei"
            })
        }

        private fun Handlebars.fritekstHelper() {
            registerHelper("fritekst", Helper<String> { context, _ ->
                if (context == null) "" else {
                    val text = Handlebars.Utils.escapeExpression(context)
                        .toString()
                        .replace(Regex("\\u0002"), " ")
                        .replace(Regex("\\r\\n|[\\n\\r]"), "<br/>")
                    Handlebars.SafeString(text)
                }
            })
        }

        private fun Handlebars.equalsNumberHelper() {
            registerHelper("eqTall", Helper<Int> { context, options ->
                if (context == options.param(0)) options.fn() else options.inverse()
            })
        }

        private fun Handlebars.equalsHelper() {
            registerHelper("eq", Helper<String> { context, options ->
                if (context == options.param(0)) options.fn() else options.inverse()
            })
        }

        private fun Handlebars.imageHelper() {
            registerHelper("image", Helper<String> { context, _ ->
                if (context == null) "" else bilder[context]
            })
        }
    }


    fun genererPDF(pdfData: PdfData): ByteArray = template(pdfData)
        .apply(
            Context
                .newBuilder(pdfData.pdfData())
                .resolver(MapValueResolver.INSTANCE)
                .build()
        ).let { html ->
            val outputStream = ByteArrayOutputStream()
            PdfRendererBuilder()
                .useFastMode()
                .usePdfUaAccessbility(true)
                .withHtmlContent(html, "")
                .medFonter()
                .toStream(outputStream)
                .buildPdfRenderer()
                .createPDF()

            outputStream.use {
                it.toByteArray()
            }
        }

    private fun template(pdfData: PdfData): Template = handlebars.compile(pdfData.resolveTemplate())

    private fun PdfRendererBuilder.medFonter(): PdfRendererBuilder {
        val sourceSansPro = "Source Sans Pro"
        return useFont(
            { ByteArrayInputStream(REGULAR_FONT) },
            sourceSansPro,
            400,
            BaseRendererBuilder.FontStyle.NORMAL,
            false
        )
            .useFont(
                { ByteArrayInputStream(BOLD_FONT) },
                sourceSansPro,
                700,
                BaseRendererBuilder.FontStyle.NORMAL,
                false
            )
            .useFont(
                { ByteArrayInputStream(ITALIC_FONT) },
                sourceSansPro,
                400,
                BaseRendererBuilder.FontStyle.ITALIC,
                false
            )
    }
}

abstract class PdfData {
    abstract fun ytelse(): Ytelse
    abstract fun pdfData(): Map<String, Any?>
    fun resolveTemplate(): String = when (ytelse()) {
        Ytelse.PLEIEPENGER_SYKT_BARN -> "pleiepenger-sykt-barn-soknad"
        Ytelse.PLEIEPENGER_SYKT_BARN_ENDRINGSMELDING -> "pleiepenger-sykt-barn-endringsmelding"
        Ytelse.OMSORGSPENGER_UTVIDET_RETT -> "omsorgspenger-utvidet-rett-soknad"
        Ytelse.OMSORGSPENGER_MIDLERTIDIG_ALENE -> "omsorgspenger-midlertidig-alene-soknad"
        Ytelse.OMSORGSDAGER_ALENEOMSORG -> "omsorgspenger-aleneomsorg-soknad"
        Ytelse.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER -> "omsorgspengerutbetaling-arbeidstaker-soknad"
        Ytelse.OMSORGSPENGER_UTBETALING_SNF -> "omsorgspengerutbetaling-snf-soknad"
        Ytelse.PLEIEPENGER_LIVETS_SLUTTFASE -> "pleiepenger-livets-sluttfase-soknad"
        Ytelse.ETTERSENDING, Ytelse.ETTERSENDING_PLEIEPENGER_SYKT_BARN, Ytelse.ETTERSENDING_PLEIEPENGER_LIVETS_SLUTTFASE, Ytelse.ETTERSENDING_OMP -> "/ettersending-soknad"
    }
}
