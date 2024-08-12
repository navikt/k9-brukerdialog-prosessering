package no.nav.brukerdialog.pleiepengersyktbarn.api.domene

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import no.nav.k9.søknad.felles.personopplysninger.Barn
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import no.nav.k9.søknad.felles.personopplysninger.Barn as K9Barn

private val logger: Logger =
    LoggerFactory.getLogger("no.nav.k9brukerdialogapi.ytelse.pleiepengersyktbarn.soknad.BarnDetaljer")

data class BarnDetaljer(
    @field:Size(min = 11, max = 11)
    @field:Pattern(regexp = "^\\d+$", message = "'\${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    var fødselsnummer: String?,

    @field:PastOrPresent(message = "kan ikke være i fremtiden")
    @JsonFormat(pattern = "yyyy-MM-dd") val fødselsdato: LocalDate?,

    val aktørId: String?,

    @field:NotBlank(message = "kan ikke være tomt eller blankt")
    val navn: String,

    val årsakManglerIdentitetsnummer: ÅrsakManglerIdentitetsnummer? = null,
) {
    override fun toString(): String {
        return "BarnDetaljer(aktørId=***, navn=***, fodselsdato=***"
    }

    fun manglerIdentitetsnummer(): Boolean = fødselsnummer.isNullOrEmpty()

    infix fun oppdaterFødselsnummer(fødselsnummer: String?) {
        logger.info("Forsøker å oppdaterer fnr på barn")
        this.fødselsnummer = fødselsnummer
    }

    fun tilK9Barn(): Barn = when {
        fødselsnummer != null -> K9Barn().medNorskIdentitetsnummer(NorskIdentitetsnummer.of(fødselsnummer))
        fødselsdato != null -> K9Barn().medFødselsdato(fødselsdato)
        else -> K9Barn()
    }

    @AssertTrue(message = "Må være satt dersom fødselsnummer er null.")
    fun isFødselsDato(): Boolean {
        if(fødselsnummer.isNullOrEmpty()) {
            return fødselsdato != null
        }
        return true
    }

    @AssertTrue(message = "Må være satt dersom fødselsnummer er null.")
    fun isÅrsakManglerIdentitetsnummer(): Boolean {
        if(fødselsnummer.isNullOrEmpty()) {
            return årsakManglerIdentitetsnummer != null
        }
        return true
    }
}

enum class ÅrsakManglerIdentitetsnummer {
    NYFØDT,
    BARNET_BOR_I_UTLANDET,
    ANNET
}
