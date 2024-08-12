package no.nav.brukerdialog.api.ytelse.omsorgspengerutbetalingsnf.domene

import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserIngenValideringsFeil
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import org.junit.jupiter.api.Test

class SpørsmålOgSvarTest {

    @Test
    fun `Gyldig SpørsmålOgSvar gir ingen feil`() {
        Validator.verifiserIngenValideringsFeil(
            SpørsmålOgSvar(
                spørsmål = "Har du hund?",
                svar = true
            )
        )
    }

    @Test
    fun `Spørsmål som blank gir feil`() {
        Validator.verifiserValideringsFeil(
            SpørsmålOgSvar(
                spørsmål = " ",
                svar = true
            ), 1, "Kan ikke være tomt eller blankt"
        )
    }

    @Test
    fun `Spørsmål over 1000 tegn gir feil`() {
        Validator.verifiserValideringsFeil(
            SpørsmålOgSvar(
                spørsmål = "A".repeat(1001),
                svar = true
            ), 1, "Kan ikke være mer enn 1000 tegn"
        )
    }

    @Test
    fun `Svar som null gir feil`() {
        Validator.verifiserValideringsFeil(
            SpørsmålOgSvar(
                spørsmål = "Har du hund?",
                svar = null
            ), 1, "Kan ikke være null"
        )
    }
}
