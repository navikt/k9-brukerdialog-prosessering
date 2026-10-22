package no.nav.brukerdialog.mellomlagring.dokument

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

const val MAKS_ANTALL_VEDLEGG_I_EKSISTENSSJEKK = 20

data class VedleggEksistensRequest(
    @field:Size(
        min = 1,
        max = MAKS_ANTALL_VEDLEGG_I_EKSISTENSSJEKK,
        message = "Må inneholde mellom 1 og $MAKS_ANTALL_VEDLEGG_I_EKSISTENSSJEKK vedlegg-id-er"
    )
    val vedleggIder: List<@NotBlank String>,
)

data class VedleggEksistens(
    val vedleggId: String,
    val finnes: Boolean,
)

