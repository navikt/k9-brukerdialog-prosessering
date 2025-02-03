package no.nav.brukerdialog.integrasjon.familiepdf.dto

data class FamiliePdfPostRequest(
    val feltmap: FeltMap,
)

data class FeltMap(
    val label: String,
    val verdiliste: List<VerdilisteElement>,
    val pdfConfig: PdfConfig = PdfConfig(harInnholdsfortegnelse = true, "nb"),
    val skjemanummer: String? = null,
)

data class VerdilisteElement(
    val label: String,
    val visningsVariant: String? = null,
    val verdi: String? = null,
    val verdiliste: List<VerdilisteElement>? = null,
    val alternativer: String? = null,
)

data class PdfConfig(
    val harInnholdsfortegnelse: Boolean,
    val språk: String,
)
