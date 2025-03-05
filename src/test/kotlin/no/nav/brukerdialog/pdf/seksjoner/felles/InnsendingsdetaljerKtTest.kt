package no.nav.brukerdialog.pdf.seksjoner.felles

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class InnsendingsdetaljerKtTest {
    @Test
    fun `Innsendingsdetaljer er strukturert riktig`() {
        val svarTidspunkt = ZonedDateTime.of(2025, 3, 4, 1, 0, 0, 0, ZoneId.of("Europe/Oslo"))

        val result = strukturerInnsendingsdetaljerSeksjon(svarTidspunkt)

        Assertions.assertEquals("Innsendingsdetaljer", result.label)
        Assertions.assertEquals(1, result.verdiliste?.size)
        Assertions.assertEquals("Tirsdag 04.03.2025 01:00", result.verdiliste?.get(0)?.verdi)
    }
}
