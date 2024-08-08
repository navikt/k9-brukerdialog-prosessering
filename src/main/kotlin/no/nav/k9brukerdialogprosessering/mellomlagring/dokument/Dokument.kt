package no.nav.k9brukerdialogprosessering.mellomlagring.dokument

import com.fasterxml.jackson.annotation.JsonProperty

data class Dokument(
    val eier: DokumentEier? = null,
    val content: ByteArray,
    @JsonProperty("content_type")
    val contentType: String,
    val title: String
)

data class DokumentEier(
    @JsonProperty("eiers_fødselsnummer")
    val eiersFødselsnummer: String
)
