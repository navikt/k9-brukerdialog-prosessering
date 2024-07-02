package no.nav.k9brukerdialogprosessering.api.ytelse.omsorgspengerutbetalingsnf.domene

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9brukerdialogprosessering.api.ytelse.omsorgspengerutbetalingsnf.domene.TypeBarn.FOSTERBARN
import no.nav.k9brukerdialogprosessering.oppslag.barn.BarnOppslag
import no.nav.k9brukerdialogprosessering.utils.krever
import java.time.LocalDate
import no.nav.k9.søknad.felles.personopplysninger.Barn as K9Barn

class Barn(
    @Size(max = 11)
    @Pattern(regexp = "^\\d+$", message = "'\${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private var identitetsnummer: String? = null,
    private val navn: String,
    private val fødselsdato: LocalDate,
    private val type: TypeBarn,
    private val aktørId: String? = null,
) {
    companion object {
        internal fun List<Barn>.somK9BarnListe() = kunFosterbarn().map { it.somK9Barn() }
        fun List<Barn>.kunFosterbarn() = this.filter { it.type == FOSTERBARN }
        internal fun List<Barn>.valider(felt: String) = this.flatMapIndexed { index, barn ->
            barn.valider("$felt[$index]")
        }
    }

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        krever(navn.isNotBlank(), "$felt.navn kan ikke være tomt eller blankt.")
    }

    internal fun leggTilIdentifikatorHvisMangler(barnFraOppslag: List<BarnOppslag>) {
        if (identitetsnummer == null) identitetsnummer =
            barnFraOppslag.find { it.aktørId == this.aktørId }?.identitetsnummer
    }

    internal fun somK9Barn() = K9Barn().medNorskIdentitetsnummer(NorskIdentitetsnummer.of(identitetsnummer))
}

enum class TypeBarn {
    FOSTERBARN,
    ANNET,
    FRA_OPPSLAG
}
