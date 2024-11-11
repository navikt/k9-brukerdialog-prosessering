package no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.arbeid.ArbeidsforholdOLP.Companion.somK9ArbeidstidInfo
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstaker
import java.time.LocalDate

class ArbeidsgiverOLP(
    @field:Size(max = 20)
    @field:Pattern(regexp = "^\\d+$", message = "'\${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private val organisasjonsnummer: String,

    @field:NotBlank(message = "navn kan ikke være tomt eller blankt")
    private val navn: String,
    //TODO: skal erAnsatt og sluttetFørSøknadsperiode disse verdiene brukes til noe?
    private val erAnsatt: Boolean,
    private val sluttetFørSøknadsperiode: Boolean? = null,

    @field:Valid
    private val arbeidsforhold: ArbeidsforholdOLP? = null,
) {

    companion object {

        internal fun List<ArbeidsgiverOLP>.somK9Arbeidstaker(fraOgMed: LocalDate, tilOgMed: LocalDate) =
            map { it.somK9Arbeidstaker(fraOgMed, tilOgMed) }
    }

    fun somK9Arbeidstaker(fraOgMed: LocalDate, tilOgMed: LocalDate) = Arbeidstaker().apply {
        medOrganisasjonsnummer(Organisasjonsnummer.of(this@ArbeidsgiverOLP.organisasjonsnummer))
        medOrganisasjonsnavn(navn)
        medArbeidstidInfo(arbeidsforhold.somK9ArbeidstidInfo(fraOgMed, tilOgMed))
    }

}
