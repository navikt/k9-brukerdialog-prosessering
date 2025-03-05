package no.nav.brukerdialog.pdf.seksjoner.felles

import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Bosted
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Medlemskap
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate

class MedlemskapKtTest {
    @Test
    fun `Medlemskap er strukturert riktig`() {
        val medlemskap =
            Medlemskap(
                harBoddIUtlandetSiste12Mnd = true,
                utenlandsoppholdSiste12Mnd =
                    listOf(
                        Bosted(
                            LocalDate.of(2020, 1, 2),
                            LocalDate.of(2020, 1, 3),
                            "US",
                            "USA",
                        ),
                        Bosted(
                            LocalDate.of(1900, 1, 2),
                            LocalDate.of(1910, 1, 3),
                            "NO",
                            "Norge",
                        ),
                    ),
                skalBoIUtlandetNeste12Mnd = false,
            )

        val resultat = strukturerMedlemskapSeksjon(medlemskap)

        println(
            resultat.verdiliste,
        )

        // Riktig parent
        Assertions.assertEquals("Medlemskap i folketrygden", resultat.label)
        // Riktig antall underseksjoner
        Assertions.assertEquals(2, resultat.verdiliste?.size)
        // 2 utenlandsboforhold siste 12 månedene
        Assertions.assertEquals(
            2,
            resultat.verdiliste
                ?.get(0)
                ?.verdiliste
                ?.size,
        )
        // 0 utenlandsboforhold de neste 12 månedene, men et "Nei"-svar
        Assertions.assertEquals(
            1,
            resultat.verdiliste
                ?.get(1)
                ?.verdiliste
                ?.size,
        )
    }
}
