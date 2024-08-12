package no.nav.brukerdialog.api.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import no.nav.brukerdialog.api.ytelse.omsorgspengerutbetalingarbeidstaker.domene.Barn.Companion.somK9BarnListe
import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserIngenValideringsFeil
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate

class BarnTest {

    @Test
    fun `Gyldig barn er gyldig`() {
        val gyldigBarn = Barn(
            identitetsnummer = "02119970078",
            fødselsdato = LocalDate.parse("2024-01-01"),
            aktørId = "12345",
            navn = "Barn Barnesen",
            type = TypeBarn.FRA_OPPSLAG
        )
        Validator.verifiserIngenValideringsFeil(gyldigBarn)
    }

    @Test
    fun `Navnløse barn er ikke gyldig`() {
        val noname = Barn(
            identitetsnummer = "02119970078",
            fødselsdato = LocalDate.parse("2024-01-01"),
            aktørId = "12345",
            navn = "",
            type = TypeBarn.FRA_OPPSLAG
        )
        Validator.verifiserValideringsFeil(noname, 1, "Kan ikke være tomt eller blankt")
    }

    @Test
    fun `Ufødte barn er ikke gyldig`() {
        val fremtidsbarn = Barn(
            identitetsnummer = "02119970078",
            fødselsdato = LocalDate.parse("2999-01-01"),
            aktørId = "12345",
            navn = "Barn Barnesen",
            type = TypeBarn.FRA_OPPSLAG
        )
        Validator.verifiserValideringsFeil(fremtidsbarn, 1, "Kan ikke være i fremtiden")
    }

    @Test
    fun `Personer over 18 år er ikke gyldige barn`() {
        val voksen = Barn(
            identitetsnummer = "02119970078",
            fødselsdato = LocalDate.parse("1987-01-01"),
            aktørId = "12345",
            navn = "Indre Barnesen",
            type = TypeBarn.FRA_OPPSLAG
        )
        Validator.verifiserValideringsFeil(voksen, 1, "Kan ikke være eldre enn 19 år")
    }

    @Test
    fun `Det går an å kjøre valideringer på lister av barn`() {
        val flereBarn = listOf(
            Barn(
                identitetsnummer = "02119970078",
                fødselsdato = LocalDate.parse("2999-01-01"),
                aktørId = "12345",
                navn = "Barn Barnesen",
                type = TypeBarn.FRA_OPPSLAG
            ), Barn(
                identitetsnummer = "02119970078",
                fødselsdato = LocalDate.parse("1987-01-01"),
                aktørId = "12345",
                navn = "Indre Barnesen",
                type = TypeBarn.FRA_OPPSLAG
            )
        )
        Validator.verifiserValideringsFeil(
            DineBarn(
                barn = flereBarn,
                harDeltBosted = true
            ),
            2,
            "Kan ikke være i fremtiden", "Kan ikke være eldre enn 19 år"
        )
    }

    @Test
    fun `K9Barn-listen skal kun inkludere fosterbarn`() {
        val listeMedBarn = listOf(
            Barn(
                identitetsnummer = "01010100000",
                fødselsdato = LocalDate.parse("2020-01-01"),
                aktørId = "12345",
                navn = "Van Li Barnesen",
                type = TypeBarn.FRA_OPPSLAG
            ),
            Barn(
                identitetsnummer = "02020200000",
                fødselsdato = LocalDate.parse("2021-01-01"),
                aktørId = "12345",
                navn = "Foster Barnesen",
                type = TypeBarn.FOSTERBARN
            ),
            Barn(
                identitetsnummer = "03030300000",
                fødselsdato = LocalDate.parse("2022-01-01"),
                aktørId = "12345",
                navn = "Anna Barnesen",
                type = TypeBarn.ANNET
            )
        )
        val k9BarnListe = listeMedBarn.somK9BarnListe()
        assertEquals(k9BarnListe.size, 1);
        assertEquals(k9BarnListe.get(0).personIdent.verdi, "02020200000")
    }

    @Test
    fun `K9Barn skal kun ha enten fødselsnummer eller fødselsdato`() {
        val barnMedAlt = Barn(
            identitetsnummer = "03030300000",
            fødselsdato = LocalDate.parse("2022-01-01"),
            aktørId = "12345",
            navn = "Anna Barnesen",
            type = TypeBarn.ANNET
        )
        assertNull(barnMedAlt.somK9Barn().fødselsdato)
        assertNotNull(barnMedAlt.somK9Barn().personIdent)

        val barnMedKunFødselsnummer = Barn(
            fødselsdato = LocalDate.parse("2022-01-01"),
            aktørId = "12345",
            navn = "Anna Barnesen",
            type = TypeBarn.ANNET
        )

        assertNotNull(barnMedKunFødselsnummer.somK9Barn().fødselsdato)
        assertNull(barnMedKunFødselsnummer.somK9Barn().personIdent)
    }
}
