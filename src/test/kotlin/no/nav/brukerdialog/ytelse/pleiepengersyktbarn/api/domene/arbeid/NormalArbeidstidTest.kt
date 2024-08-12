package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.api.domene.arbeid

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration

class NormalArbeidstidTest {

    @Test
    fun `Regner ut riktig timerPerDag fra timerPerUkeISnitt - 37,5 timer per uke gir 7,5 per dag`(){
        val normalarbeidstid = NormalArbeidstid(
            timerPerUkeISnitt = Duration.ofHours(37).plusMinutes(30)
        )

        assertEquals(Duration.ofHours(7).plusMinutes(30), normalarbeidstid.timerPerDagFraSnitt())
    }
}
