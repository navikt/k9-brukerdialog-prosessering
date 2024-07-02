package no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase.domene

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.Arbeidstaker
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengerlivetssluttfase.domene.Arbeidsforhold.Companion.somK9ArbeidstidInfo
import no.nav.k9brukerdialogprosessering.utils.krever
import java.time.LocalDate

class Arbeidsgiver(
    @Size(max = 20)
    @Pattern(regexp = "^\\d+$", message = "'\${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private val organisasjonsnummer: String,
    private val navn: String? = null,
    private val erAnsatt: Boolean,
    private val sluttetFørSøknadsperiode: Boolean? = null,
    private val arbeidsforhold: Arbeidsforhold? = null,
) {

    companion object {
        internal fun List<Arbeidsgiver>.valider(felt: String = "arbeidsgivere") =
            flatMapIndexed { index: Int, arbeidsgiver: Arbeidsgiver ->
                arbeidsgiver.valider("$felt[$index]")
            }

        internal fun List<Arbeidsgiver>.somK9Arbeidstaker(fraOgMed: LocalDate, tilOgMed: LocalDate) =
            map { it.somK9Arbeidstaker(fraOgMed, tilOgMed) }
    }

    internal fun valider(felt: String = "arbeidsgiver") = mutableListOf<String>().apply {
        krever(!navn.isNullOrBlank(), "$felt.navn kan ikke være null eller blankt.")
        arbeidsforhold?.let { addAll(it.valider("$felt.arbeidsforhold")) }
    }

    fun somK9Arbeidstaker(fraOgMed: LocalDate, tilOgMed: LocalDate) = Arbeidstaker().apply {
        medOrganisasjonsnummer(Organisasjonsnummer.of(this@Arbeidsgiver.organisasjonsnummer))
        medOrganisasjonsnavn(navn)
        medArbeidstidInfo(arbeidsforhold.somK9ArbeidstidInfo(fraOgMed, tilOgMed))
    }

}
