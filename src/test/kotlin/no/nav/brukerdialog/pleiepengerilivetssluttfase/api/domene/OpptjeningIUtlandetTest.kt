package no.nav.brukerdialog.pleiepengerilivetssluttfase.api.domene

import no.nav.brukerdialog.api.ytelse.fellesdomene.Land
import no.nav.brukerdialog.pleiepengerilivetsslutttfase.api.domene.OpptjeningIUtlandet.Companion.valider
import no.nav.brukerdialog.pleiepengerilivetsslutttfase.api.domene.OpptjeningType.ARBEIDSTAKER
import no.nav.brukerdialog.pleiepengerilivetsslutttfase.api.domene.OpptjeningIUtlandet
import no.nav.brukerdialog.utils.TestUtils.verifiserIngenValideringsFeil
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import org.junit.jupiter.api.Test
import java.time.LocalDate

class OpptjeningIUtlandetTest{

    @Test
    fun `Gyldig OpptjeningIUtlandet gir ingen valideringsfeil`(){
        OpptjeningIUtlandet(
            navn = "Fisk AS",
            opptjeningType = ARBEIDSTAKER,
            land = Land("NLD", "Nederland"),
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-01-01")
        ).valider("opptjeningIUtlandet").verifiserIngenValideringsFeil()
    }

    @Test
    fun `Ugyldig land gir valideringsfeil`(){
        OpptjeningIUtlandet(
            navn = "Fisk AS",
            opptjeningType = ARBEIDSTAKER,
            land = Land("MARS", "  "),
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-03-01")
        ).valider("opptjeningIUtlandet").verifiserValideringsFeil(2,
            listOf(
                "opptjeningIUtlandet.land.landkode 'MARS' er ikke en gyldig ISO 3166-1 alpha-3 kode.",
                "opptjeningIUtlandet.land.landnavn kan ikke være tomt eller blankt."
            )
        )
    }

    @Test
    fun `Liste med OpptjeningIUtlandet hvor fraOgMed er etter tilOgMed gir valideringsfeil`() {
        listOf(
            OpptjeningIUtlandet(
                "Fisk AS", ARBEIDSTAKER,
                Land("NLD", "Nederland"),
                LocalDate.parse("2022-01-10"),
                LocalDate.parse("2022-01-01")
            ),
            OpptjeningIUtlandet(
                "Fisk AS", ARBEIDSTAKER,
                Land("NLD", "Nederland"),
                LocalDate.parse("2022-01-10"),
                LocalDate.parse("2022-01-01")
            )
        ).valider("opptjeningIUtlandet").verifiserValideringsFeil(2,
            listOf(
                "opptjeningIUtlandet[0].tilOgMed må være lik eller etter fraOgMed.",
                "opptjeningIUtlandet[1].tilOgMed må være lik eller etter fraOgMed."
            )
        )
    }
}
