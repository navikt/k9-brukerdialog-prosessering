package no.nav.k9brukerdialogprosessering.api.ytelse.felles

import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Bekreftelser
import no.nav.k9brukerdialogprosessering.utils.TestUtils.verifiserFeil
import no.nav.k9brukerdialogprosessering.utils.TestUtils.verifiserIngenFeil
import org.junit.jupiter.api.Test

class BekreftelserTest {

    @Test
    fun `Gyldig Bekreftelser gir ingen feil`() {
        Bekreftelser(
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = true
        ).valider("bekreftelser").verifiserIngenFeil()
    }

    @Test
    fun `Feiler om man sender harBekreftetOpplysninger som false`() {
        Bekreftelser(
            harBekreftetOpplysninger = false,
            harForståttRettigheterOgPlikter = true
        ).valider("bekreftelser").verifiserFeil(1, listOf("bekreftelser.harBekreftetOpplysninger må være true"))
    }

    @Test
    fun `Feiler om man sender harBekreftetOpplysninger som null`() {
        Bekreftelser(
            harBekreftetOpplysninger = null,
            harForståttRettigheterOgPlikter = true
        ).valider("bekreftelser").verifiserFeil(1, listOf("bekreftelser.harBekreftetOpplysninger må være true"))
    }

    @Test
    fun `Feiler om man sender harForståttRettigheterOgPlikter som false`() {
        Bekreftelser(
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = false
        ).valider("bekreftelser").verifiserFeil(1, listOf("bekreftelser.harForståttRettigheterOgPlikter må være true"))
    }

    @Test
    fun `Feiler om man sender harForståttRettigheterOgPlikter som null`() {
        Bekreftelser(
            harBekreftetOpplysninger = true,
            harForståttRettigheterOgPlikter = null
        ).valider("bekreftelser").verifiserFeil(1, listOf("bekreftelser.harForståttRettigheterOgPlikter må være true"))
    }
}
