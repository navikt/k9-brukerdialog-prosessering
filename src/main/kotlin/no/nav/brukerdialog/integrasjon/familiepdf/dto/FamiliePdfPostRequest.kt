package no.nav.brukerdialog.integrasjon.familiepdf.dto

import no.nav.brukerdialog.common.PdfConfig
import no.nav.brukerdialog.common.VerdilisteElement

data class FamiliePdfPostRequest(
    val label: String,
    val verdiliste: List<VerdilisteElement>,
    val pdfConfig: PdfConfig = PdfConfig(harInnholdsfortegnelse = true, "nb"),
    val skjemanummer: String? = null,
)
