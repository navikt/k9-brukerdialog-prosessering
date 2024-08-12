package no.nav.brukerdialog.meldinger.felles.domene

import no.nav.brukerdialog.utils.StringUtils.storForbokstav
import java.time.LocalDate

data class Søker (
    val aktørId: String,
    val fødselsdato: LocalDate,
    val fødselsnummer: String,
    val fornavn: String,
    val mellomnavn: String? = null,
    val etternavn: String
) {
    override fun toString(): String {
        return "Soker(fornavn='$fornavn', mellomnavn=$mellomnavn, etternavn='$etternavn', fødselsdato=$fødselsdato, aktørId='******')"
    }

    fun fullnavn(): Navn = Navn(
        fornavn = fornavn,
        mellomnavn = mellomnavn,
        etternavn = etternavn
    )

    fun formatertNavn(): String = if (mellomnavn != null) "$fornavn $mellomnavn $etternavn" else "$fornavn $etternavn"

    fun somMap() = mapOf(
        "navn" to formatertNavn().storForbokstav(),
        "fødselsnummer" to fødselsnummer
    )


}
