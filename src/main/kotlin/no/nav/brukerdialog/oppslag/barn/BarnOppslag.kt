package no.nav.brukerdialog.oppslag.barn

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class BarnOppslag(
    val fødselsdato: LocalDate,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val aktørId: String,
    @JsonIgnore var identitetsnummer: String,
) {
    fun navn(): String = when (mellomnavn) {
        null -> "$fornavn $etternavn"
        else -> "$fornavn $mellomnavn $etternavn"
    }
}

data class BarnOppslagRespons(
    val identitetsnummer: String,
    val fødselsdato: LocalDate,
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String,
    @JsonProperty("aktør_id") val aktørId: String,
) {
    fun tilBarnOppslag() = BarnOppslag(
        identitetsnummer = identitetsnummer,
        fødselsdato = fødselsdato,
        fornavn = fornavn,
        mellomnavn = mellomnavn,
        etternavn = etternavn,
        aktørId = aktørId
    )
}

data class BarnOppslagResponsListe(val barn: List<BarnOppslagRespons>)

