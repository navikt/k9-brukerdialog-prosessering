package no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import no.nav.k9.søknad.felles.opptjening.SelvstendigNæringsdrivende.SelvstendigNæringsdrivendePeriodeInfo
import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Regnskapsfører.Companion.leggTilK9Regnskapsfører
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.VarigEndring.Companion.leggTilVarigEndring
import no.nav.k9brukerdialogprosessering.utils.erLikEllerEtter
import java.math.BigDecimal
import java.time.LocalDate
import no.nav.k9.søknad.felles.opptjening.SelvstendigNæringsdrivende as K9SelvstendigNæringsdrivende

data class Virksomhet(
    @JsonFormat(pattern = "yyyy-MM-dd") val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val tilOgMed: LocalDate? = null,
    val næringstype: Næringstype,
    val fiskerErPåBladB: Boolean? = null,
    val næringsinntekt: Int? = null,
    val navnPåVirksomheten: String,
    @field:Size(max = 20)
    @field:Pattern(regexp = "^\\d+$", message = "'\${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    val organisasjonsnummer: String? = null,
    @field:NotNull(message = "Kan ikke være null.")
    val registrertINorge: Boolean,
    @field:Valid val registrertIUtlandet: Land? = null,
    val yrkesaktivSisteTreFerdigliknedeÅrene: YrkesaktivSisteTreFerdigliknedeArene? = null,
    val varigEndring: VarigEndring? = null,
    val regnskapsfører: Regnskapsfører? = null,
    val erNyoppstartet: Boolean,
    @field:NotNull(message = "Kan ikke være null")
    val harFlereAktiveVirksomheter: Boolean? = null,
) {

    @AssertTrue(message = "Kan ikke være null når registrertINorge er false")
    fun isRegistrertIUtlandet(): Boolean {
        if (registrertINorge == false) {
            return registrertIUtlandet != null
        }
        return true
    }

    @AssertTrue(message = "Kan ikke være null når registrertINorge er true")
    fun isOrganisasjonsnummer(): Boolean {
        if (registrertINorge == true) {
            return organisasjonsnummer != null
        }
        return true
    }

    @AssertTrue(message = "Må være lik eller etter fraOgMed")
    fun isTilOgMed(): Boolean {
        if (tilOgMed != null) {
            return tilOgMed.erLikEllerEtter(fraOgMed)
        }
        return true
    }

    @AssertTrue(message = "Kan ikke være null når næringstype er FISKE")
    fun isFiskerErPåBladB(): Boolean {
        if (næringstype == Næringstype.FISKE) {
            return fiskerErPåBladB != null
        }
        return true
    }

    @AssertTrue(message = "Når nyoppstartet er true, må fraOgMed være maks 4 år siden")
    fun isErNyoppstartet(): Boolean {
        val fireÅrSiden = LocalDate.now().minusYears(4)
        if (erNyoppstartet) {
            return fraOgMed.erLikEllerEtter(fireÅrSiden)
        }
        return true
    }

    @AssertTrue(message = "Når nyoppstartet er false, må fraOgMed må være over 4 år siden")
    fun isErIkkeNyoppstartet(): Boolean {
        val fireÅrSiden = LocalDate.now().minusYears(4)
        if (!erNyoppstartet) {
            return fraOgMed < fireÅrSiden
        }
        return true
    }

    fun somK9SelvstendigNæringsdrivende() = K9SelvstendigNæringsdrivende().apply {
        medVirksomhetNavn(navnPåVirksomheten)
        medPerioder(mapOf(Periode(fraOgMed, tilOgMed) to byggK9SelvstendingNæringsdrivendeInfo()))
        this@Virksomhet.organisasjonsnummer?.let { medOrganisasjonsnummer(Organisasjonsnummer.of(it)) }
    }

    private fun byggK9SelvstendingNæringsdrivendeInfo() = SelvstendigNæringsdrivendePeriodeInfo().apply {
        medVirksomhetstyper(listOf(næringstype.somK9Virksomhetstype()))
        medRegistrertIUtlandet(!registrertINorge!!)
        medErNyoppstartet(this@Virksomhet.erNyoppstartet)

        næringsinntekt?.let { medBruttoInntekt(BigDecimal.valueOf(it.toLong())) }
        regnskapsfører?.let { leggTilK9Regnskapsfører(it) }
        yrkesaktivSisteTreFerdigliknedeÅrene?.let { medErNyIArbeidslivet(true) }
        varigEndring?.let { leggTilVarigEndring(it) }

        this@Virksomhet.registrertIUtlandet?.let {
            medLandkode(it.somK9Landkode())
        } ?: medLandkode(Landkode.NORGE)
    }
}
