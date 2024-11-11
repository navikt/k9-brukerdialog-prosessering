package no.nav.brukerdialog.ytelse.opplæringspenger.api.k9Format

import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.Kurs
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.Kurs.Companion.tilK9Format
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.KursPerioderMedReiseTid
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.Kursholder
import no.nav.brukerdialog.ytelse.opplæringspenger.utils.OLPTestUtils.fredag
import no.nav.brukerdialog.ytelse.opplæringspenger.utils.OLPTestUtils.mandag
import no.nav.brukerdialog.ytelse.opplæringspenger.utils.OLPTestUtils.onsdag
import no.nav.brukerdialog.ytelse.opplæringspenger.utils.OLPTestUtils.torsdag
import no.nav.k9.søknad.felles.type.Periode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KursTest {
    private val KJENT_KURSHOLDER = Kursholder(
        id = "752cf5b5-3095-4282-8485-6764aeef815b",
        navn = "Kurssenter AS"
    )

    private val STANDARD_KURSPERIODE = KursPerioderMedReiseTid(
        avreise = mandag,
        hjemkomst = fredag,
        kursperiode = Periode(onsdag, torsdag),
        beskrivelseReisetidTil = "Beskrivelse av reisetid til",
        beskrivelseReisetidHjem = "Beskrivelse av reisetid hjem"
    )


    @Test
    fun `Forvent feil derom det sendes tom liste med enkeltdager`() {
        val kurs = Kurs(
            kursholder = KJENT_KURSHOLDER,
            perioder = emptyList()
        )

        Validator.verifiserValideringsFeil(kurs, 1, "Kan ikke være tom liste")
    }

    @Test
    fun `Forvent feil derom erAnnen ikke stememr med id og navn`() {
        val kjentKursHolder = Kursholder(
            id = "752cf5b5-3095-4282-8485-6764aeef815b",
            navn = "Kurssenter AS",
            erAnnen = true
        )
        val ukjentKursholder = Kursholder(
            id = null,
            navn = null,
            erAnnen = false
        )

        Validator.verifiserValideringsFeil(kjentKursHolder, 1, "id og navn må være null for annen kursholder")
        Validator.verifiserValideringsFeil(ukjentKursholder, 1, "id og navn må være satt for kjent kursholder")
    }

    @Test
    fun `Kurs med kjent kursholder mappes riktig til k9Format`() {
        val kurs = Kurs(
            kursholder = KJENT_KURSHOLDER,
            perioder = listOf(STANDARD_KURSPERIODE)
        )

        val k9Kurs = kurs.tilK9Format()

        assertEquals(kurs.kursholder.id, k9Kurs.kursholder.institusjonUuid.toString())
        assertEquals(kurs.perioder.size, k9Kurs.kursperioder.size)

        assertEquals(kurs.perioder[0].avreise, k9Kurs.kursperioder[0].avreise)
        assertEquals(kurs.perioder[0].hjemkomst, k9Kurs.kursperioder[0].hjemkomst)
        assertEquals(kurs.perioder[0].kursperiode.tilOgMed, k9Kurs.kursperioder[0].periode.tilOgMed)
        assertEquals(kurs.perioder[0].kursperiode.fraOgMed, k9Kurs.kursperioder[0].periode.fraOgMed)
        assertEquals(kurs.perioder[0].beskrivelseReisetidTil, k9Kurs.kursperioder[0].begrunnelseReisetidTil)
        assertEquals(kurs.perioder[0].beskrivelseReisetidHjem, k9Kurs.kursperioder[0].begrunnelseReisetidHjem)
    }

    @Test
    fun `Kurs med ukjent kursholder mappes riktig til k9Format`() {
        val kursholder = Kursholder(
            erAnnen = true
        )

        val kurs = Kurs(
            kursholder = kursholder,
            perioder = listOf(STANDARD_KURSPERIODE)
        )

        val k9Kurs = kurs.tilK9Format()

        assertEquals(kurs.kursholder.id, k9Kurs.kursholder.institusjonUuid)
        assertEquals(kurs.perioder.size, k9Kurs.kursperioder.size)

        assertEquals(kurs.perioder[0].avreise, k9Kurs.kursperioder[0].avreise)
        assertEquals(kurs.perioder[0].hjemkomst, k9Kurs.kursperioder[0].hjemkomst)
        assertEquals(kurs.perioder[0].kursperiode.tilOgMed, k9Kurs.kursperioder[0].periode.tilOgMed)
        assertEquals(kurs.perioder[0].kursperiode.fraOgMed, k9Kurs.kursperioder[0].periode.fraOgMed)
        assertEquals(kurs.perioder[0].beskrivelseReisetidTil, k9Kurs.kursperioder[0].begrunnelseReisetidTil)
        assertEquals(kurs.perioder[0].beskrivelseReisetidHjem, k9Kurs.kursperioder[0].begrunnelseReisetidHjem)
    }
}
