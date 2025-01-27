package no.nav.brukerdialog.ytelse.opplæringspenger.api.k9Format

import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.Kurs
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.Reise
import no.nav.brukerdialog.ytelse.opplæringspenger.utils.OLPTestUtils.mandag
import no.nav.brukerdialog.ytelse.opplæringspenger.utils.OLPTestUtils.torsdag
import no.nav.k9.søknad.felles.type.Periode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KursTest {
    private val KJENT_KURSHOLDER = "Kurssenter AS"

    private val STANDARD_REISE = Reise(
        reiserUtenforKursdager = true,
        reisedager = listOf(mandag, torsdag),
        reisedagerBeskrivelse = "Begrunnelse for reise"
    )


    @Test
    fun `Forvent feil derom det sendes tom liste med enkeltdager`() {
        val kurs = Kurs(
            kursholder = KJENT_KURSHOLDER,
            perioder = emptyList(),
            reise = STANDARD_REISE
        )

        Validator.verifiserValideringsFeil(kurs, 1, "Kan ikke være tom liste")
    }

    @Test
    fun `Forvent feil derom det ikke sendes med navn på kursholder`() {
        val kurs = Kurs(
            kursholder = "",
            perioder = listOf(Periode(mandag, torsdag)),
            reise = STANDARD_REISE
        )

        Validator.verifiserValideringsFeil(kurs, 1, "Kan ikke være tom")
    }

    @Test
    fun `Forvent feil derom det ikke sendes med beskrivelse for reise`() {
        val kurs = Kurs(
            kursholder = KJENT_KURSHOLDER,
            perioder = listOf(Periode(mandag, torsdag)),
            reise = Reise(
                reiserUtenforKursdager = true,
                reisedager = listOf(mandag, torsdag),
                reisedagerBeskrivelse = ""
            )
        )

        Validator.verifiserValideringsFeil(kurs, 1, "Dersom 'reiserUtenforKursdager' er true, må man sende med beskrivelse")
    }

    @Test
    fun `Forvent feil derom det ikke sendes med reisedager`() {
        val kurs = Kurs(
            kursholder = KJENT_KURSHOLDER,
            perioder = listOf(Periode(mandag, torsdag)),
            reise = Reise(
                reiserUtenforKursdager = true,
                reisedager = listOf(),
                reisedagerBeskrivelse = "Derfor"
            )
        )

        Validator.verifiserValideringsFeil(kurs, 1, "Dersom 'reiserUtenforKursdager' er true, kan ikke 'reisedager' være tom liste")
    }

    @Test
    fun `Kurs med kursholder mappes riktig til k9Format`() {
        val kurs = Kurs(
            kursholder = KJENT_KURSHOLDER,
            perioder = listOf(Periode(mandag, torsdag)),
            reise = STANDARD_REISE
        )

        val k9Kurs = kurs.tilK9Format()
        val k9Reise = k9Kurs.reise

        assertEquals(kurs.kursholder, k9Kurs.kursholder.navn)
        assertEquals(null, k9Kurs.kursholder.institusjonUuid)
        assertEquals(kurs.perioder.size, k9Kurs.kursperioder.size)
        assertEquals(kurs.perioder[0].tilOgMed, k9Kurs.kursperioder[0].tilOgMed)
        assertEquals(kurs.perioder[0].fraOgMed, k9Kurs.kursperioder[0].fraOgMed)

        assertEquals(kurs.reise.reiserUtenforKursdager, k9Reise.isReiserUtenforKursdager)
        assertEquals(kurs.reise.reisedagerBeskrivelse, k9Reise.reisedagerBeskrivelse)
        assertEquals(kurs.reise.reisedager?.size, k9Reise.reisedager.size)
        assertEquals(kurs.reise.reisedager?.get(0), k9Reise.reisedager[0])
    }
}
