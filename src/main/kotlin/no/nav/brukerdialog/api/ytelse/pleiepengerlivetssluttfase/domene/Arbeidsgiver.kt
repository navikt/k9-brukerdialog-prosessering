package no.nav.brukerdialog.api.ytelse.pleiepengerlivetssluttfase.domene

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstaker
import no.nav.brukerdialog.api.ytelse.pleiepengerlivetssluttfase.domene.Arbeidsforhold.Companion.somK9ArbeidstidInfo
import java.time.LocalDate

class Arbeidsgiver(
    @field:Size(max = 20)
    @field:Pattern(regexp = "^\\d+$", message = "'\${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private val organisasjonsnummer: String,
    @field:NotBlank(message = "Kan ikke være tomt eller blankt") private val navn: String,
    private val erAnsatt: Boolean,
    private val sluttetFørSøknadsperiode: Boolean? = null,
    @field:Valid private val arbeidsforhold: Arbeidsforhold? = null,
) {

    companion object {

        internal fun List<Arbeidsgiver>.somK9Arbeidstaker(fraOgMed: LocalDate, tilOgMed: LocalDate) =
            map { it.somK9Arbeidstaker(fraOgMed, tilOgMed) }
    }

    fun somK9Arbeidstaker(fraOgMed: LocalDate, tilOgMed: LocalDate) = Arbeidstaker().apply {
        medOrganisasjonsnummer(Organisasjonsnummer.of(this@Arbeidsgiver.organisasjonsnummer))
        medOrganisasjonsnavn(navn)
        medArbeidstidInfo(arbeidsforhold.somK9ArbeidstidInfo(fraOgMed, tilOgMed))
    }

}
