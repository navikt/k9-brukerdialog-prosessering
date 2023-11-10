package no.nav.k9brukerdialogprosessering.utils

import no.nav.k9brukerdialogprosessering.meldinger.pleiepengerilivetsslutttfase.grupperSammenhengendeDatoerPerUke
import no.nav.k9brukerdialogprosessering.utils.DateUtils.grupperMedUker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.time.LocalDate

class DateUtilsKtTest {

    @Test
    fun `gitt sammenhengende datoer per uke, forvent at de grupperes sammen`() {
        val logger = LoggerFactory.getLogger(DateUtilsKtTest::class.java)
        val datoer = listOf(
            LocalDate.parse("2022-01-01"),
            LocalDate.parse("2022-01-02"),
            LocalDate.parse("2022-01-04"),
            LocalDate.parse("2022-01-11"),
            LocalDate.parse("2022-01-12"),
            LocalDate.parse("2022-01-13"),
            LocalDate.parse("2022-01-14"),
            LocalDate.parse("2022-01-15"),
            LocalDate.parse("2022-01-16"),
            LocalDate.parse("2022-01-17"),
            LocalDate.parse("2022-01-18"),
            LocalDate.parse("2022-01-19"),
            LocalDate.parse("2022-01-21"),
            LocalDate.parse("2022-01-22"),
            LocalDate.parse("2022-01-24"),
            LocalDate.parse("2022-01-25"),
            LocalDate.parse("2022-01-26"),
            LocalDate.parse("2022-01-27"),
            LocalDate.parse("2022-01-28"),
            LocalDate.parse("2022-01-29"),
            LocalDate.parse("2022-01-30"),
            LocalDate.parse("2022-01-31"),
            LocalDate.parse("2022-02-01"),
        )

        val uker: List<Map<String, Any>> = datoer.grupperMedUker().grupperSammenhengendeDatoerPerUke()
        uker.forEach {
            logger.info("uke: ${it["uke"]}, periode: ${it["perioder"]}")
        }
        /*
        uke: 53, periode: [Lørdag 01.01.2022 - Søndag 02.01.2022]
        uke: 1, periode: [Tirsdag 04.01.2022]
        uke: 2, periode: [Tirsdag 11.01.2022 - Søndag 16.01.2022]
        uke: 3, periode: [Mandag 17.01.2022 - Onsdag 19.01.2022, Fredag 21.01.2022 - Lørdag 22.01.2022]
        uke: 4, periode: [Mandag 24.01.2022 - Søndag 30.01.2022]
        uke: 5, periode: [Mandag 31.01.2022 - Tirsdag 01.02.2022]
         */

        assertEquals(6, uker.size)
        assertEquals("[Lørdag 01.01.2022 - Søndag 02.01.2022]", uker[0]["perioder"].toString())
        assertEquals("[Tirsdag 04.01.2022]", uker[1]["perioder"].toString())
        assertEquals("[Tirsdag 11.01.2022 - Søndag 16.01.2022]", uker[2]["perioder"].toString())
        assertEquals("[Mandag 17.01.2022 - Onsdag 19.01.2022, Fredag 21.01.2022 - Lørdag 22.01.2022]", uker[3]["perioder"].toString())
        assertEquals("[Mandag 24.01.2022 - Søndag 30.01.2022]", uker[4]["perioder"].toString())
        assertEquals("[Mandag 31.01.2022 - Tirsdag 01.02.2022]", uker[5]["perioder"].toString())
    }
}
