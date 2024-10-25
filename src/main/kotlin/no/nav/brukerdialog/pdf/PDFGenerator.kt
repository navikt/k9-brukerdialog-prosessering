package no.nav.brukerdialog.pdf

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
import no.nav.brukerdialog.common.Constants.DATE_FORMATTER
import no.nav.brukerdialog.common.Constants.DATE_TIME_FORMATTER
import no.nav.brukerdialog.common.Ytelse
import no.nav.brukerdialog.meldinger.omsorgpengerutbetalingat.domene.FraværÅrsak
import no.nav.brukerdialog.meldinger.omsorgpengerutbetalingsnf.domene.AktivitetFravær
import no.nav.brukerdialog.utils.DurationUtils.tilString
import no.nav.k9.søknad.felles.type.Språk
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

@Component
class PDFGenerator {

    init {
        XRLog.setLoggerImpl(Slf4jLogger())
    }

    private companion object {
        private val ROOT = "handlebars"
        private val REGULAR_FONT =
            ClassPathResource("${ROOT}/fonts/SourceSansPro-Regular.ttf").inputStream.readAllBytes()
        private val BOLD_FONT = ClassPathResource("${ROOT}/fonts/SourceSansPro-Bold.ttf").inputStream.readAllBytes()
        private val ITALIC_FONT = ClassPathResource("${ROOT}/fonts/SourceSansPro-Italic.ttf").inputStream.readAllBytes()
        private val bilder: Map<String, String> = emptyMap()
        private val handlebars = configureHandlebars()

        private fun configureHandlebars() = Handlebars(ClassPathTemplateLoader("/${ROOT}")).apply {
            imageHelper()
            equalsHelper()
            equalsNumberHelper()
            equalsJaNeiHelper()
            fritekstHelper()
            jaNeiSvarHelper()
            isNotNullHelper()
            capitalizeHelper()
            datoHelper()
            tidspunktHelper()
            varighetHelper()
            årsakHelper()
            fraværSomFormatHelper()
            aktivitetFraværHelper()
            infiniteLoops(true)
        }

        protected fun loadPng(name: String): String {
            val bytes = ClassPathResource("${ROOT}/images/$name.png").inputStream.readAllBytes()
            val base64string = Base64.getEncoder().encodeToString(bytes)
            return "data:image/png;base64,$base64string"
        }

        private fun Handlebars.isNotNullHelper() {
            registerHelper("isNotNull", Helper<Any> { context, options ->
                return@Helper if (context != null) {
                    options.fn()
                } else {
                    options.inverse()
                }
            })
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

        private fun Handlebars.capitalizeHelper() {
            registerHelper("capitalize", Helper<String> { context, _ ->
                context.capitalize()
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

        private fun Handlebars.equalsJaNeiHelper() {
            registerHelper("eqJaNei") { context: Boolean, options ->
                val con = when (context) {
                    true -> "Ja"
                    false -> "Nei"
                }
                if (con == options.param(0)) options.fn() else options.inverse()
            }
        }

        private fun Handlebars.datoHelper() {
            registerHelper("dato", Helper<String> { context, _ ->
                DATE_FORMATTER.format(LocalDate.parse(context))
            })
        }


        private fun Handlebars.tidspunktHelper() {
            registerHelper("tidspunkt", Helper<String> { context, _ ->
                DATE_TIME_FORMATTER.format(ZonedDateTime.parse(context))
            })
        }

        private fun Handlebars.varighetHelper() {
            registerHelper("varighet", Helper<String> { context, _ ->
                Duration.parse(context).tilString()
            })
        }

        private fun Handlebars.årsakHelper() {
            registerHelper("årsak", Helper<String> { context, _ ->
                when (FraværÅrsak.valueOf(context)) {
                    FraværÅrsak.ORDINÆRT_FRAVÆR -> "Ordinært fravær"
                    FraværÅrsak.STENGT_SKOLE_ELLER_BARNEHAGE -> "Stengt skole eller barnehage"
                    FraværÅrsak.SMITTEVERNHENSYN -> "Smittevernhensyn"
                }
            })
        }

        private fun Handlebars.fraværSomFormatHelper() {
            registerHelper("fraværSomFormat", Helper<List<String>> { context, _ ->
                when (context.size) {
                    1 -> "Fravær som"
                    2 -> "Fravær både som"
                    else -> ""
                }
            })
        }

        private fun Handlebars.aktivitetFraværHelper() {
            registerHelper("aktivitetFravær", Helper<String> { context, _ ->
                when (AktivitetFravær.valueOf(context)) {
                    AktivitetFravær.FRILANSER -> "frilanser"
                    AktivitetFravær.SELVSTENDIG_VIRKSOMHET -> "selvstendig næringsdrivende"
                }
            })
        }
    }

    fun genererPDF(pdfData: PdfData): ByteArray = genererHTML(pdfData).let { html ->
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

    fun genererHTML(pdfData: PdfData): String = template(pdfData)
        .apply(
            Context
                .newBuilder(pdfData.pdfData())
                .resolver(MapValueResolver.INSTANCE)
                .build()
        )

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
    abstract fun språk(): Språk
    abstract fun pdfData(): Map<String, Any?>
    fun resolveTemplate(): String {
        val språkSuffix = when (språk()) {
            Språk.NORSK_NYNORSK -> ".nn"
            else -> "" // Default bokmål
        }
        return when (ytelse()) {
            Ytelse.PLEIEPENGER_SYKT_BARN -> "pleiepenger-sykt-barn-soknad$språkSuffix".trimEnd()
            Ytelse.PLEIEPENGER_SYKT_BARN_ENDRINGSMELDING -> "pleiepenger-sykt-barn-endringsmelding$språkSuffix".trimEnd()
            Ytelse.PLEIEPENGER_LIVETS_SLUTTFASE -> "pleiepenger-i-livets-sluttfase-soknad$språkSuffix".trimEnd()
            Ytelse.OMSORGSPENGER_UTVIDET_RETT -> "omsorgspenger-utvidet-rett-kronisk-sykt-barn-soknad$språkSuffix".trimEnd()
            Ytelse.OMSORGSPENGER_MIDLERTIDIG_ALENE -> "omsorgspenger-midlertidig-alene-soknad$språkSuffix".trimEnd()
            Ytelse.OMSORGSDAGER_ALENEOMSORG -> "omsorgspenger-aleneomsorg-soknad$språkSuffix".trimEnd()
            Ytelse.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER -> "omsorgspenger-utbetaling-arbeidstaker-soknad$språkSuffix".trimEnd()
            Ytelse.OMSORGSPENGER_UTBETALING_SNF -> "omsorgspenger-utbetaling-snf-soknad$språkSuffix".trimEnd()
            Ytelse.ETTERSENDELSE -> "ettersendelse$språkSuffix".trimEnd()
            Ytelse.UNGDOMSYTELSE -> "ungdomsytelse-soknad$språkSuffix".trimEnd()
        }
    }
}
