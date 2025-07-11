package no.nav.brukerdialog.mellomlagring.dokument

import com.fasterxml.jackson.annotation.JsonProperty

data class Dokument(
    val eier: DokumentEier? = null,
    val content: ByteArray,
    @JsonProperty("content_type")
    val contentType: String,
    val title: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Dokument

        if (title != other.title) return false
        if (eier != other.eier) return false
        if (!content.contentEquals(other.content)) return false
        if (contentType != other.contentType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + content.contentHashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + eier.hashCode()
        return result
    }
}
