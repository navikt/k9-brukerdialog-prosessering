package no.nav.k9brukerdialogprosessering.utils

import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.grupperSammenhengendeDatoerPerUke
import no.nav.k9brukerdialogprosessering.utils.DateUtils.grupperMedUker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.LocalDate

class DateUtilsKtTest {

    @Test
    @Disabled("Kankun kjøre denne lokalt.Den feiler på GitHub Actions")
    fun `gitt sammenhengende datoer per uke, forvent at de grupperes sammen`() {
        val datoer = listOf(
            // Uke 53
            LocalDate.parse("2022-01-01"),
            LocalDate.parse("2022-01-02"),
            // Uke 1
            LocalDate.parse("2022-01-04"),
            // Uke 2
            LocalDate.parse("2022-01-11"),
            LocalDate.parse("2022-01-12"),
            LocalDate.parse("2022-01-13"),
            LocalDate.parse("2022-01-14"),
            LocalDate.parse("2022-01-15"),
            LocalDate.parse("2022-01-16"),
            // Uke 3
            LocalDate.parse("2022-01-17"),
            LocalDate.parse("2022-01-18"),
            LocalDate.parse("2022-01-19"),
            // Uke 3
            LocalDate.parse("2022-01-21"),
            LocalDate.parse("2022-01-22"),
            // Uke 4
            LocalDate.parse("2022-01-24"),
            LocalDate.parse("2022-01-25"),
            LocalDate.parse("2022-01-26"),
            LocalDate.parse("2022-01-27"),
            LocalDate.parse("2022-01-28"),
            LocalDate.parse("2022-01-29"),
            LocalDate.parse("2022-01-30"),
            // Uke 5
            LocalDate.parse("2022-01-31"),
            LocalDate.parse("2022-02-01"),
        )

        val uker: List<Map<String, Any>> = datoer.grupperMedUker().grupperSammenhengendeDatoerPerUke()

        assertEquals(6, uker.size)
        assertEquals("[Lørdag 01.01.2022 - Søndag 02.01.2022]", uker[0]["perioder"].toString())
        assertEquals("[Tirsdag 04.01.2022]", uker[1]["perioder"].toString())
        assertEquals("[Tirsdag 11.01.2022 - Søndag 16.01.2022]", uker[2]["perioder"].toString())
        assertEquals("[Mandag 17.01.2022 - Onsdag 19.01.2022, Fredag 21.01.2022 - Lørdag 22.01.2022]", uker[3]["perioder"].toString())
        assertEquals("[Mandag 24.01.2022 - Søndag 30.01.2022]", uker[4]["perioder"].toString())
        assertEquals("[Mandag 31.01.2022 - Tirsdag 01.02.2022]", uker[5]["perioder"].toString())
    }
}
