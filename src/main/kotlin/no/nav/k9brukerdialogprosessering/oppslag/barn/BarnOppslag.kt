package no.nav.k9brukerdialogprosessering.oppslag.barn

import com.fasterxml.jackson.annotation.JsonIgnore
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
    val aktør_id: String,
) {
    fun tilBarnOppslag() = BarnOppslag(
        identitetsnummer = identitetsnummer,
        fødselsdato = fødselsdato,
        fornavn = fornavn,
        mellomnavn = mellomnavn,
        etternavn = etternavn,
        aktørId = aktør_id
    )
}

data class BarnOppslagResponsListe(val barn: List<BarnOppslagRespons>)

