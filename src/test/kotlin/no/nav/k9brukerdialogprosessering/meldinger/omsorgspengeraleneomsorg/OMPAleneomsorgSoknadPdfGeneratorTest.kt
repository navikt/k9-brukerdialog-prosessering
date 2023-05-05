package no.nav.k9brukerdialogprosessering.meldinger.omsorgspengeraleneomsorg

import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengeraleneomsorg.domene.Barn
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengeraleneomsorg.domene.TidspunktForAleneomsorg
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengeraleneomsorg.domene.TypeBarn
import no.nav.k9brukerdialogprosessering.meldinger.omsorgspengeraleneomsorg.utils.OMPAleneomsorgSoknadUtils
import no.nav.k9brukerdialogprosessering.pdf.PDFGenerator
import no.nav.k9brukerdialogprosessering.utils.PathUtils.pdfPath
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDate
import java.time.ZonedDateTime

class OMPAleneomsorgSoknadPdfGeneratorTest {

    @Test
    fun `generering av oppsummerings-PDF fungerer`() {
        genererOppsummeringsPdfer(false)
    }

    @Test
    fun `opprett lesbar oppsummerings-PDF`() {
        genererOppsummeringsPdfer(true)
    }

    private companion object {
        const val PDF_PREFIX = "omp_ao"
        val generator = PDFGenerator()
    }

    fun genererOppsummeringsPdfer(writeBytes: Boolean) {
        var id = "1-full-søknad"
        var pdf = generator.genererPDF(
            OMPAleneomsorgSoknadUtils.defaultSøknad(
                søknadId = id,
                mottatt = ZonedDateTime.now(),
            ).pdfData()
        )
        if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)

        id = "2-full-søknad-fosterbarn"
        pdf = generator.genererPDF(
            OMPAleneomsorgSoknadUtils.defaultSøknad(
                søknadId = id,
                mottatt = ZonedDateTime.now(),
            ).copy(
                barn = Barn(
                    navn = "Ole Dole",
                    identitetsnummer = "29076523302",
                    type = TypeBarn.FOSTERBARN,
                    fødselsdato = LocalDate.now().minusMonths(5),
                    tidspunktForAleneomsorg = TidspunktForAleneomsorg.SISTE_2_ÅRENE,
                    dato = LocalDate.now().minusMonths(4)
                )
            ).pdfData()
        )
        if (writeBytes) File(pdfPath(soknadId = id, prefix = PDF_PREFIX)).writeBytes(pdf)
    }
}

