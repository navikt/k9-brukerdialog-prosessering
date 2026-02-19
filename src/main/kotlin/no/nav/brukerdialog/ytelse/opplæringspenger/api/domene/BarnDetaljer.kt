package no.nav.brukerdialog.ytelse.opplæringspenger.api.domene

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.constraints.*
import no.nav.k9.søknad.felles.personopplysninger.Barn
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.time.LocalDate
import no.nav.k9.søknad.felles.personopplysninger.Barn as K9Barn

private val logger: Logger =
    LoggerFactory.getLogger("no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.BarnDetaljer")

data class BarnDetaljer(
    @field:Size(min = 11, max = 11)
    @field:Pattern(regexp = "^\\d+$", message = "'\${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    var norskIdentifikator: String?,

    @field:PastOrPresent(message = "kan ikke være i fremtiden")
    @JsonFormat(pattern = "yyyy-MM-dd")
    val fødselsdato: LocalDate?,

    val aktørId: String?,

    @field:NotBlank(message = "kan ikke være tomt eller blankt")
    val navn: String,

    @get:JsonProperty("årsakManglerIdentitetsnummer")
    val årsakManglerIdentitetsnummer: ÅrsakManglerIdentitetsnummer? = null,

    val relasjonTilBarnet: BarnRelasjon? = null,
    val relasjonTilBarnetBeskrivelse: String? = null,

    val fødselsattestVedleggUrls: List<URL>? = listOf(),
) {
    override fun toString(): String {
        return "BarnDetaljer(aktørId=***, navn=***, fodselsdato=***"
    }

    fun manglerIdentitetsnummer(): Boolean = norskIdentifikator.isNullOrEmpty()

    infix fun oppdaterNorskIdentifikator(norskIdentifikator: String?) {
        logger.info("Forsøker å oppdaterer fnr på barn")
        this.norskIdentifikator = norskIdentifikator
    }

    fun tilK9Barn(): Barn = when {
        norskIdentifikator != null -> K9Barn().medNorskIdentitetsnummer(NorskIdentitetsnummer.of(norskIdentifikator))
        fødselsdato != null -> K9Barn().medFødselsdato(fødselsdato)
        else -> K9Barn()
    }

    @Hidden
    @AssertTrue(message = "Må være satt dersom norskIdentifikator er null.")
    fun isFødselsDato(): Boolean {
        if (norskIdentifikator.isNullOrEmpty()) {
            return fødselsdato != null
        }
        return true
    }

    @Hidden
    @AssertTrue(message = "Må være satt dersom norskIdentifikator er null.")
    fun isÅrsakManglerIdentitetsnummer(): Boolean {
        if (norskIdentifikator.isNullOrEmpty()) {
            return årsakManglerIdentitetsnummer != null
        }
        return true
    }

    @Hidden
    @AssertTrue(message = "Når 'relasjonTilBarnet' er ANNET, kan ikke 'relasjonTilBarnetBeskrivelse' være tom")
    fun isRelasjonTilBarnetBeskrivelse(): Boolean {
        if (relasjonTilBarnet == BarnRelasjon.ANNET) {
            return !relasjonTilBarnetBeskrivelse.isNullOrBlank()
        }
        return true
    }
}

enum class ÅrsakManglerIdentitetsnummer {
    NYFØDT,
    BARNET_BOR_I_UTLANDET,
    ANNET
}
