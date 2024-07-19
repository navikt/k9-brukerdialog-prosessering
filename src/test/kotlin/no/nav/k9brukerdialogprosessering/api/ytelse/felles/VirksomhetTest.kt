package no.nav.k9brukerdialogprosessering.api.ytelse.felles

import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Land
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Næringstype
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Virksomhet
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.YrkesaktivSisteTreFerdigliknedeArene
import no.nav.k9brukerdialogprosessering.utils.TestUtils.VALIDATOR
import no.nav.k9brukerdialogprosessering.utils.TestUtils.verifiserFeil
import no.nav.k9brukerdialogprosessering.utils.TestUtils.verifiserIngenFeil
import org.junit.jupiter.api.Test
import java.time.LocalDate

class VirksomhetTest {

    private companion object {
        val felt = "sn"

        private fun gyldigVirksomhet(
            næringstype: Næringstype = Næringstype.ANNEN,
            fiskerErPåBladB: Boolean = false,
            fraOgMed: LocalDate = LocalDate.now().minusDays(1),
            tilOgMed: LocalDate = LocalDate.now(),
            næringsinntekt: Int = 1111,
            navnPåVirksomheten: String = "TullOgTøys",
            registrertINorge: Boolean = true,
            registrertIUtlandet: Land? = null,
            organisasjonsnummer: String? = "101010",
            yrkesaktivSisteTreFerdigliknedeÅrene: YrkesaktivSisteTreFerdigliknedeArene = YrkesaktivSisteTreFerdigliknedeArene(
                LocalDate.now()
            ),
            harFlereAktiveVirksomheter: Boolean? = true,
            erNyoppstartet: Boolean = true,
        ) = Virksomhet(
            næringstype = næringstype,
            fiskerErPåBladB = fiskerErPåBladB,
            fraOgMed = fraOgMed,
            tilOgMed = tilOgMed,
            næringsinntekt = næringsinntekt,
            navnPåVirksomheten = navnPåVirksomheten,
            registrertINorge = registrertINorge,
            registrertIUtlandet = registrertIUtlandet,
            organisasjonsnummer = organisasjonsnummer,
            yrkesaktivSisteTreFerdigliknedeÅrene = yrkesaktivSisteTreFerdigliknedeÅrene,
            harFlereAktiveVirksomheter = harFlereAktiveVirksomheter,
            erNyoppstartet = erNyoppstartet
        )
    }

    @Test
    fun `FraOgMed kan ikke være før tilOgMed, validate skal returnere en violation`() {
        VALIDATOR.validate(
            gyldigVirksomhet(
                fraOgMed = LocalDate.now(),
                tilOgMed = LocalDate.now().minusDays(1),
            )
        ).verifiserFeil(1, "Må være lik eller etter fraOgMed.")
    }

    @Test
    fun `FraOgMed er før tilogmed, validate skal ikke reagere`() {
        VALIDATOR.validate(
            gyldigVirksomhet(
                fraOgMed = LocalDate.now().minusDays(1),
                tilOgMed = LocalDate.now()
            )
        ).verifiserIngenFeil()
    }

    @Test
    fun `FraOgMed er lik tilogmed, validate skal ikke reagere`() {
        VALIDATOR.validate(
            gyldigVirksomhet(
                fraOgMed = LocalDate.now(),
                tilOgMed = LocalDate.now(),
            )
        ).verifiserIngenFeil()
    }

    @Test
    fun `Hvis virksomheten er registrert i Norge så må orgnummer være satt, validate skal ikke reagere`() {
        VALIDATOR.validate(
            gyldigVirksomhet(
                registrertINorge = true,
                organisasjonsnummer = "101010",
            )
        ).verifiserIngenFeil()
    }

    @Test
    fun `Hvis virksomheten er registrert i Norge så skal den feile hvis orgnummer ikke er satt, validate skal returnere en violation`() {
        VALIDATOR.validate(
            gyldigVirksomhet(
                organisasjonsnummer = null,
                registrertINorge = true,
            )
        ).verifiserFeil(1, "Kan ikke være null når registrertINorge er true")
    }

    @Test
    fun `Hvis virksomheten ikke er registrert i Norge så må registrertIUtlandet være satt til noe, validate skal ikke reagere`() {
        VALIDATOR.validate(
            gyldigVirksomhet(
                registrertINorge = false,
                registrertIUtlandet = Land(
                    landkode = "DEU",
                    landnavn = "Tyskland"
                )
            )
        ).verifiserIngenFeil()
    }

    @Test
    fun `Hvis virksomheten ikke er registrert i Norge så må den feile hvis registrertIUtlandet ikke er satt til null, validate skal returnere en violation`() {
        VALIDATOR.validate(
            gyldigVirksomhet(
                registrertINorge = false,
                registrertIUtlandet = null
            )
        ).verifiserFeil(1, "Kan ikke være null når registrertINorge er false")
    }

    @Test
    fun `Hvis registrert i utlandet så må landkode være riktig ISO 3166 alpha-3 landkode, validering skal gi feil`() {
        VALIDATOR.validate(
            gyldigVirksomhet(
                registrertINorge = false,
                registrertIUtlandet = Land(
                    landnavn = "Tyskland",
                    landkode = "NO"
                )
            )
        ).verifiserFeil(1, "NO er ikke en gyldig ISO 3166-1 alpha-3 kode.")
    }

    @Test
    fun `Hvis registrert i utlandet så må landkode være riktig ISO 3166 alpha-3 landkode`() {
        VALIDATOR.validate(
            gyldigVirksomhet(
                registrertINorge = false,
                registrertIUtlandet = Land(
                    landnavn = "Tyskland",
                    landkode = "DEU"
                )
            )
        ).verifiserIngenFeil()
    }

    @Test
    fun `Hvis harFlereAktiveVirksomheter er null skal validering gi feil`() {
        VALIDATOR.validate(
            gyldigVirksomhet(
                harFlereAktiveVirksomheter = null
            )
        ).verifiserFeil(1, "Kan ikke være null")
    }
}

