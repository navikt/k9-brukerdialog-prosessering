package no.nav.k9brukerdialogprosessering.oppslag.soker

import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9brukerdialogprosessering.mellomlagring.dokument.DokumentEier
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
    val aktør_id: String,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val fødselsdato: LocalDate
) {
    fun tilSøker(fødselsnummer: String) = Søker(
        aktørId = aktør_id,
        fødselsnummer = fødselsnummer,
        fødselsdato = fødselsdato,
        fornavn = fornavn,
        mellomnavn = mellomnavn,
        etternavn = etternavn
    )
}
