package no.nav.brukerdialog.ytelse.aktivitetspenger.pdf

import no.nav.brukerdialog.pdf.PDFGenerator
import no.nav.brukerdialog.utils.PathUtils.pdfPath
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.soknad.HarKontonummer
import no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.soknad.KontonummerInfo
import no.nav.brukerdialog.ytelse.aktivitetspenger.utils.AktivitetspengersøknadUtils
import org.junit.jupiter.api.Test
import java.io.File

class AktivitetspengersøknadPdfGeneratorTest {

    @Test
    fun `generering av oppsummerings-PDF fungerer`() {
        genererOppsummeringsPdfer(false)
    }

    @Test
    fun `opprett lesbar oppsummerings-PDF`() {
        genererOppsummeringsPdfer(true)
    }

    private companion object {
        const val PDF_PREFIX = "aktivitetspenger"
        val generator = PDFGenerator()

        fun genererOppsummeringsPdfer(writeBytes: Boolean) {
            var id = "1-full-søknad"
            var pdf = generator.genererPDF(AktivitetspengersøknadUtils.gyldigSøknad(søknadId = id).pdfData())
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "2-søknad-uten-barn"
            pdf = generator.genererPDF(
                AktivitetspengersøknadUtils.gyldigSøknad(søknadId = id)
                    .copy(barn = emptyList(), barnErRiktig = false)
                    .pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "3-søknad-uten-kontonummer"
            pdf = generator.genererPDF(
                AktivitetspengersøknadUtils.gyldigSøknad(søknadId = id)
                    .copy(
                        kontonummerInfo = KontonummerInfo(
                            harKontonummer = HarKontonummer.NEI,
                            kontonummerFraRegister = null,
                            kontonummerErRiktig = false
                        )
                    )
                    .pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "4-søknad-uvisst-kontonummer"
            pdf = generator.genererPDF(
                AktivitetspengersøknadUtils.gyldigSøknad(søknadId = id)
                    .copy(
                        kontonummerInfo = KontonummerInfo(
                            harKontonummer = HarKontonummer.UVISST,
                            kontonummerFraRegister = null,
                            kontonummerErRiktig = false
                        )
                    )
                    .pdfData()
            )
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)
        }
    }
}
