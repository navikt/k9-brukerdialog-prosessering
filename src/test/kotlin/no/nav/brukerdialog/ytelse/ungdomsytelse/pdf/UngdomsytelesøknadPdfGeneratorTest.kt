package no.nav.brukerdialog.ytelse.ungdomsytelse.pdf

import no.nav.brukerdialog.ytelse.ungdomsytelse.utils.UngdomsytelsesøknadUtils
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.FlereSokereSvar
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.Frilans
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.Land
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.Næringstype
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.Pleietrengende
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.UtenlandskNæring
import no.nav.brukerdialog.meldinger.pleiepengerilivetsslutttfase.domene.ÅrsakManglerIdentitetsnummer
import no.nav.brukerdialog.pdf.PDFGenerator
import no.nav.brukerdialog.utils.PathUtils.pdfPath
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.soknad.HarKontonummer
import no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.soknad.KontonummerInfo
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDate

class UngdomsytelesøknadPdfGeneratorTest {

    @Test
    fun `generering av oppsummerings-PDF fungerer`() {
        genererOppsummeringsPdfer(false)
    }

    @Test
    fun `opprett lesbar oppsummerings-PDF`() {
        genererOppsummeringsPdfer(true)
    }

    private companion object {
        const val PDF_PREFIX = "ung"
        val generator = PDFGenerator()

        fun genererOppsummeringsPdfer(writeBytes: Boolean) {
            var id = "1-full-søknad"
            var pdf = generator.genererPDF(UngdomsytelsesøknadUtils.gyldigSøknad(søknadId = id).pdfData())
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "2-søknad-uten-barn"
            pdf = generator.genererPDF(UngdomsytelsesøknadUtils.gyldigSøknad(søknadId = id)
                .copy(barn = emptyList(), barnErRiktig = false)
                .pdfData())
            if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

            id = "3-søknad-uten-kontonummer"
            pdf = generator.genererPDF(
                UngdomsytelsesøknadUtils.gyldigSøknad(søknadId = id)
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

            id = "3-søknad-uvisst-kontonummer"
            pdf = generator.genererPDF(
                UngdomsytelsesøknadUtils.gyldigSøknad(søknadId = id)
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
