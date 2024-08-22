package no.nav.brukerdialog.oppslag.soker

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.brukerdialog.mellomlagring.dokument.DokumentEier
import java.time.LocalDate
import no.nav.k9.søknad.felles.personopplysninger.Søker as K9Søker

data class Søker (
    val aktørId: String,
    val fødselsdato: LocalDate,
    val fødselsnummer: String,
    val fornavn: String? = null,
    val mellomnavn: String? = null,
    val etternavn: String? = null
) {

    fun somK9Søker() = K9Søker(NorskIdentitetsnummer.of(fødselsnummer))

    fun somDokumentEier() = DokumentEier(fødselsnummer)

}

data class SøkerOppslagRespons(
    @JsonProperty("aktør_id") val aktørId: String,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val fødselsdato: LocalDate
) {
    fun tilSøker(fødselsnummer: String) = Søker(
        aktørId = aktørId,
        fødselsnummer = fødselsnummer,
        fødselsdato = fødselsdato,
        fornavn = fornavn,
        mellomnavn = mellomnavn,
        etternavn = etternavn
    )
}
