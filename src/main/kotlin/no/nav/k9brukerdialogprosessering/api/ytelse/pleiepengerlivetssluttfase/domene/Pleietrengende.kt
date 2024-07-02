package no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase.domene

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9brukerdialogprosessering.utils.erFørEllerLik
import no.nav.k9brukerdialogprosessering.utils.krever
import java.time.LocalDate
import no.nav.k9.søknad.ytelse.pls.v1.Pleietrengende as K9Pleietrengende

class Pleietrengende(
    @Size(max = 11)
    @Pattern(regexp = "^\\d+$", message = "'\${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private val norskIdentitetsnummer: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd") private val fødselsdato: LocalDate? = null,
    private val navn: String,
    private val årsakManglerIdentitetsnummer: ÅrsakManglerIdentitetsnummer? = null,
) {
    internal fun somK9Pleietrengende(): K9Pleietrengende = when {
        norskIdentitetsnummer != null -> K9Pleietrengende().medNorskIdentitetsnummer(
            NorskIdentitetsnummer.of(
                norskIdentitetsnummer
            )
        )

        fødselsdato != null -> K9Pleietrengende().medFødselsdato(fødselsdato)
        else -> K9Pleietrengende()
    }

    fun valider(felt: String = "pleietrengende") = mutableListOf<String>().apply {
        krever(navn.isNotBlank(), "$felt.navn kan ikke være tomt eller blankt.")
        fødselsdato?.let { krever(it.erFørEllerLik(LocalDate.now()), "$felt.fødselsdato kan ikke være i fremtiden.") }
        if (norskIdentitetsnummer == null) {
            krever(fødselsdato != null, "$felt.fødselsdato må være satt dersom norskIdentitetsnummer er null.")
            krever(
                årsakManglerIdentitetsnummer != null,
                "$felt.årsakManglerIdentitetsnummer må være satt dersom norskIdentitetsnummer er null."
            )
        }
    }
}

enum class ÅrsakManglerIdentitetsnummer { BOR_I_UTLANDET, ANNET }
