package no.nav.brukerdialog.ytelse.opplæringspenger.api.k9Format

import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.Kurs
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
        navn = "Kurssenter AS"
    )

    private val STANDARD_KURSPERIODE = KursPerioderMedReiseTid(
        avreise = mandag,
        hjemkomst = fredag,
        kursperiode = Periode(onsdag, torsdag),
        harTaptArbeidstid = true,
        begrunnelseForReiseOverEnDag = "Begrunnelse for reise over en dag"
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
    fun `Kurs med kursholder mappes riktig til k9Format`() {
        val kurs = Kurs(
            kursholder = KJENT_KURSHOLDER,
            perioder = listOf(STANDARD_KURSPERIODE)
        )

        val k9Kurs = kurs.tilK9Format()

        assertEquals(null, k9Kurs.kursholder.institusjonUuid)
        assertEquals(kurs.perioder.size, k9Kurs.kursperioder.size)

        assertEquals(kurs.perioder[0].avreise, k9Kurs.kursperioder[0].avreise)
        assertEquals(kurs.perioder[0].hjemkomst, k9Kurs.kursperioder[0].hjemkomst)
        assertEquals(kurs.perioder[0].kursperiode.tilOgMed, k9Kurs.kursperioder[0].periode.tilOgMed)
        assertEquals(kurs.perioder[0].kursperiode.fraOgMed, k9Kurs.kursperioder[0].periode.fraOgMed)
        assertEquals(kurs.perioder[0].begrunnelseForReiseOverEnDag, k9Kurs.kursperioder[0].begrunnelseReisetidTil)
    }
}
