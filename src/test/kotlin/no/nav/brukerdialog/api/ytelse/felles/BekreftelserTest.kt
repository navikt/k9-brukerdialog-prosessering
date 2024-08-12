package no.nav.brukerdialog.api.ytelse.felles

import no.nav.brukerdialog.api.ytelse.fellesdomene.Bekreftelser
import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserIngenValideringsFeil
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import org.junit.jupiter.api.Test

class BekreftelserTest {

    @Test
    fun `Gyldig Bekreftelser gir ingen feil`() {
        Validator.verifiserIngenValideringsFeil(
            Bekreftelser(
                harBekreftetOpplysninger = true,
                harForståttRettigheterOgPlikter = true
            )
        )
    }

    @Test
    fun `Feiler om man sender bekreftelser med verdier false`() {
        Validator.verifiserValideringsFeil(
            Bekreftelser(
                harBekreftetOpplysninger = false,
                harForståttRettigheterOgPlikter = false
            ), 2,
            "Opplysningene må bekreftes for å sende inn søknad",
            "Må ha forstått rettigheter og plikter for å sende inn søknad"
        )
    }
}
