package no.nav.brukerdialog.ytelse.opplæringspenger.api.k9Format


import jakarta.validation.Validation
import jakarta.validation.Validator
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.Utenlandsopphold
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.UtenlandsoppholdIPerioden
import no.nav.k9.søknad.felles.type.Periode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

class UtenlandsoppholdIPeriodenTest {

    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `isValid returnerer true når fraOgMed er før tilOgMed`() {
        val utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(
            skalOppholdeSegIUtlandetIPerioden = true,
            opphold = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2022-01-10"),
                    landkode = "SWE",
                    landnavn = "Sverige"
                )
            )
        )

        val violations = validator.validate(utenlandsoppholdIPerioden)
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `isValid returnerer true når fraOgMed er lik tilOgMed`() {
        val utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(
            skalOppholdeSegIUtlandetIPerioden = true,
            opphold = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2022-01-01"),
                    landkode = "SWE",
                    landnavn = "Sverige"
                )
            )
        )

        val violations = validator.validate(utenlandsoppholdIPerioden)
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `isValid returnerer false når fraOgMed er etter tilOgMed`() {
        val utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(
            skalOppholdeSegIUtlandetIPerioden = true,
            opphold = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2022-01-10"),
                    tilOgMed = LocalDate.parse("2022-01-01"),
                    landkode = "SWE",
                    landnavn = "Sverige"
                )
            )
        )

        val violations = validator.validate(utenlandsoppholdIPerioden)
        assertFalse(violations.isEmpty())
        assertTrue(violations.size == 1)
        assertTrue(violations.all { it.message.contains("fraOgMed må være før eller lik tilOgMed.") })
    }

    @Test
    fun `validator feiler ved ukjent landkode`() {
        val utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(
            skalOppholdeSegIUtlandetIPerioden = true,
            opphold = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2022-01-10"),
                    landkode = "hva",
                    landnavn = "Sverige"
                )
            )
        )

        val violations = validator.validate(utenlandsoppholdIPerioden)
        assertFalse(violations.isEmpty())
        assertTrue(violations.size == 1)
        assertTrue(violations.all { it.propertyPath.toString().contains("landkode" )})
        assertTrue(violations.all { it.message.contains("er ikke en gyldig ISO 3166-1 alpha-3 kode") })

    }

    @Test
    fun `validator feiler ved tom landkode`() {

        val utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(
            skalOppholdeSegIUtlandetIPerioden = true,
            opphold = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2022-01-10"),
                    landkode = "",
                    landnavn = "Sverige"
                )
            )
        )

        val violations = validator.validate(utenlandsoppholdIPerioden)
        assertFalse(violations.isEmpty())
        assertTrue(violations.size == 2)
        assertTrue(violations.all { it.propertyPath.toString().contains("landkode") })
        assertTrue(violations.any { it.message.contains("er ikke en gyldig ISO 3166-1 alpha-3 kode") })
        assertTrue(violations.any { it.propertyPath.toString().contains("landkode") })
        assertTrue(violations.any { it.message.contains("must not be blank") })
    }

    @Test
    fun `validator feiler ved tom landnavn`() {
        val utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(
            skalOppholdeSegIUtlandetIPerioden = true,
            opphold = listOf(
                Utenlandsopphold(
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2022-01-10"),
                    landkode = "SWE",
                    landnavn = ""
                )
            )
        )

        val violations = validator.validate(utenlandsoppholdIPerioden)
        assertFalse(violations.isEmpty())
        assertTrue(violations.all { it.propertyPath.toString().contains("landnavn") })
        assertTrue(violations.all { it.message.contains("must not be blank") })
    }

    @Test
    fun `k9Periode returnerer korrekt Periode objekt`() {
        val fraOgMed = LocalDate.parse("2022-01-01")
        val tilOgMed = LocalDate.parse("2022-01-10")
        val landkode = "SWE"
        val k9Periode = Periode(fraOgMed, tilOgMed)

        val utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(
            skalOppholdeSegIUtlandetIPerioden = true,
            opphold = listOf(
                Utenlandsopphold(
                    fraOgMed = fraOgMed,
                    tilOgMed = tilOgMed,
                    landkode = landkode,
                    landnavn = "Sverige"
                )
            )
        )

        val violations = validator.validate(utenlandsoppholdIPerioden)
        assertTrue(violations.isEmpty())

        val k9UtenlandsoppholdIPerioden = utenlandsoppholdIPerioden.tilK9Utenlandsopphold()
        assertEquals(fraOgMed, k9UtenlandsoppholdIPerioden.perioder.keys.first().fraOgMed)
        assertEquals(tilOgMed, k9UtenlandsoppholdIPerioden.perioder.keys.first().tilOgMed)
        assertEquals(landkode, k9UtenlandsoppholdIPerioden.perioder.get(k9Periode)?.land?.landkode)
    }
}
