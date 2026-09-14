package no.nav.brukerdialog.ytelse.felles

import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserIngenValideringsFeil
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import no.nav.brukerdialog.ytelse.fellesdomene.Land
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

    @ParameterizedTest
    @ValueSource(strings = [
        "Norge",                                   // enkelt ord
        "Antigua og Barbuda",                      // flere ord
        "Bosnia-Hercegovina",                      // bindestrek mellom ord
        "Saint-Barthélemy",                        // bindestrek + aksent (é)
        "Curaçao",                                 // spesialtegn (ç)
        "Réunion",                                 // spesialtegn (é)
        "Jomfrøyene (Britisk)",                    // parentes
        "St. Helena",                              // punktum
        "USA, mindre, utenforliggende øyer",       // komma
        "Sør-Georgia og de søre Sandwichøyene",    // bindestrek + flere ord + norske bokstaver
        "São Tomé og Príncipe",                    // flere spesialtegn i samme streng
        "Åland"                                    // stor forbokstav med norsk/svensk tegn
    ])
    fun `Gyldig Landnavn gir ingen valideringsfeil`(landnavn: String) {
        Validator.verifiserIngenValideringsFeil(Land(landkode = "NOR", landnavn = landnavn))
    }

    @Test
    fun `Land med blank landnavn gir valideringsfeil`() {
        Validator.verifiserValideringsFeil(Land(landkode = "NLD", landnavn = " "), 1, "Landnavn er ikke riktig formatert")
    }

    @Test
    fun `Landnavn med ugydlige tegn gir valideringsfeil`() {
        Validator.verifiserValideringsFeil(Land(landkode = "NOR", landnavn = "-Norge-"), 1, "Landnavn er ikke riktig formatert")
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
