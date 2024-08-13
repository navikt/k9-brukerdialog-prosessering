package no.nav.brukerdialog.ytelse.felles

import no.nav.brukerdialog.ytelse.fellesdomene.Land
import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserIngenValideringsFeil
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class LandTest {

    @ParameterizedTest
    @ValueSource(
        strings = [
            "NLD", "SWE", "DNK", "NOR", "FIN", "ISL", "GBR", "FRA", "DEU", "BEL", "LUX", "ITA", "ESP",
            "PRT", "GRC", "CYP", "MLT", "AUT", "HUN", "SVK", "SVN", "CZE", "POL", "EST", "LVA", "LTU", "BLR", "UKR", "MDA",
            "ROU", "BGR", "HRV", "BIH", "SRB", "MNE", "ALB", "MKD", "GEO", "ARM", "AZE", "TUR", "CYP", "GIB", "GGY", "JEY",
            "IMN", "LIE", "CHE", "XXK"
        ]
    )
    fun `Gyldig Land gir ingen valideringsfeil`(landkode: String) {
        Validator.verifiserIngenValideringsFeil(Land(landkode = landkode, landnavn = "Uviktig"))
    }

    @Test
    fun `Land med ugydlig landnavn gir valideringsfeil`() {
        Validator.verifiserValideringsFeil(Land(landkode = "NLD", landnavn = " "), 1, "Kan ikke være tomt eller blankt")
    }

    @Test
    fun `Land med ugydlig landkode gir valideringsfeil`() {
        Validator.verifiserValideringsFeil(
            Land(landkode = "AAA", landnavn = "Nederland"),
            1,
            "AAA er ikke en gyldig ISO 3166-1 alpha-3 kode"
        )
    }
}
