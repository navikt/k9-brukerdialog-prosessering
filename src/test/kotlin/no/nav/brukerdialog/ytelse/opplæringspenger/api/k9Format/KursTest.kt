package no.nav.brukerdialog.ytelse.opplæringspenger.api.k9Format

import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import no.nav.brukerdialog.utils.TestUtils.verifiserIngenValideringsFeil
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.Kurs
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.KursDag
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.Kursholder
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.KursVarighetType
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.Reise
import no.nav.brukerdialog.ytelse.opplæringspenger.utils.OLPTestUtils.mandag
import no.nav.brukerdialog.ytelse.opplæringspenger.utils.OLPTestUtils.torsdag
import no.nav.brukerdialog.ytelse.opplæringspenger.utils.OLPTestUtils.tirsdag
import no.nav.brukerdialog.ytelse.opplæringspenger.utils.OLPTestUtils.onsdag
import no.nav.k9.søknad.felles.type.Periode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.*

class KursTest {
    private val KJENT_KURSHOLDER = Kursholder(UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8"), "Kurssenter AS")

    private val STANDARD_REISE = Reise(
        reiserUtenforKursdager = true,
        reisedager = listOf(mandag, torsdag),
        reisedagerBeskrivelse = "Begrunnelse for reise"
    )

    private val STANDARD_KURSDAGER = listOf(
        KursDag(mandag, Duration.ofHours(6), Duration.ofHours(2)),
        KursDag(tirsdag, Duration.ofHours(7), Duration.ofHours(1)),
        KursDag(onsdag, Duration.ofHours(8), null)
    )

    // Tester for kurs med PERIODE
    @Test
    fun `Gyldig kurs med periode validerer uten feil`() {
        val kurs = Kurs(
            kursholder = KJENT_KURSHOLDER,
            enkeltdagEllerPeriode = KursVarighetType.PERIODE,
            kursperioder = listOf(Periode(mandag, torsdag)),
            kursdager = null,
            reise = STANDARD_REISE
        )

        Validator.verifiserIngenValideringsFeil(kurs)
    }

    @Test
    fun `Periode kurs uten kursperioder gir valideringsfeil`() {
        val kurs = Kurs(
            kursholder = KJENT_KURSHOLDER,
            enkeltdagEllerPeriode = KursVarighetType.PERIODE,
            kursperioder = null,
            kursdager = null,
            reise = STANDARD_REISE
        )

        Validator.verifiserValideringsFeil(kurs, 1, "Hvis enkeltdagEllerPeriode=PERIODE må det eksistere kursperioder og reise")
    }

    @Test
    fun `Periode kurs med tom liste kursperioder gir valideringsfeil`() {
        val kurs = Kurs(
            kursholder = KJENT_KURSHOLDER,
            enkeltdagEllerPeriode = KursVarighetType.PERIODE,
            kursperioder = emptyList(),
            kursdager = null,
            reise = STANDARD_REISE
        )

        Validator.verifiserValideringsFeil(kurs, 1, "Hvis enkeltdagEllerPeriode=PERIODE må det eksistere kursperioder og reise")
    }

    @Test
    fun `Periode kurs uten reise gir valideringsfeil`() {
        val kurs = Kurs(
            kursholder = KJENT_KURSHOLDER,
            enkeltdagEllerPeriode = KursVarighetType.PERIODE,
            kursperioder = listOf(Periode(mandag, torsdag)),
            kursdager = null,
            reise = null
        )

        Validator.verifiserValideringsFeil(kurs, 1, "Hvis enkeltdagEllerPeriode=PERIODE må det eksistere kursperioder og reise")
    }

    @Test
    fun `Periode kurs med kursdager gir valideringsfeil`() {
        val kurs = Kurs(
            kursholder = KJENT_KURSHOLDER,
            enkeltdagEllerPeriode = KursVarighetType.PERIODE,
            kursperioder = listOf(Periode(mandag, torsdag)),
            kursdager = STANDARD_KURSDAGER,
            reise = STANDARD_REISE
        )

        Validator.verifiserValideringsFeil(kurs, 1, "Hvis enkeltdagEllerPeriode=PERIODE må det eksistere kursperioder og reise")
    }

    // Tester for kurs med ENKELTDAG
    @Test
    fun `Gyldig kurs med enkeltdager validerer uten feil`() {
        val kurs = Kurs(
            kursholder = KJENT_KURSHOLDER,
            enkeltdagEllerPeriode = KursVarighetType.ENKELTDAG,
            kursperioder = null,
            kursdager = STANDARD_KURSDAGER,
            reise = null
        )

        Validator.verifiserIngenValideringsFeil(kurs)
    }

    @Test
    fun `Enkeltdag kurs uten kursdager gir valideringsfeil`() {
        val kurs = Kurs(
            kursholder = KJENT_KURSHOLDER,
            enkeltdagEllerPeriode = KursVarighetType.ENKELTDAG,
            kursperioder = null,
            kursdager = null,
            reise = null
        )

        Validator.verifiserValideringsFeil(kurs, 1, "Hvis enkeltdagEllerPeriode=ENKELTDAG må det eksistere kursdager og ikke reise")
    }

    @Test
    fun `Enkeltdag kurs med tom liste kursdager gir valideringsfeil`() {
        val kurs = Kurs(
            kursholder = KJENT_KURSHOLDER,
            enkeltdagEllerPeriode = KursVarighetType.ENKELTDAG,
            kursperioder = null,
            kursdager = emptyList(),
            reise = null
        )

        Validator.verifiserValideringsFeil(kurs, 1, "Hvis enkeltdagEllerPeriode=ENKELTDAG må det eksistere kursdager og ikke reise")
    }

    @Test
    fun `Enkeltdag kurs med reise gir valideringsfeil`() {
        val kurs = Kurs(
            kursholder = KJENT_KURSHOLDER,
            enkeltdagEllerPeriode = KursVarighetType.ENKELTDAG,
            kursperioder = null,
            kursdager = STANDARD_KURSDAGER,
            reise = STANDARD_REISE
        )

        Validator.verifiserValideringsFeil(kurs, 1, "Hvis enkeltdagEllerPeriode=ENKELTDAG må det eksistere kursdager og ikke reise")
    }

    @Test
    fun `Enkeltdag kurs med kursperioder gir valideringsfeil`() {
        val kurs = Kurs(
            kursholder = KJENT_KURSHOLDER,
            enkeltdagEllerPeriode = KursVarighetType.ENKELTDAG,
            kursperioder = listOf(Periode(mandag, torsdag)),
            kursdager = STANDARD_KURSDAGER,
            reise = null
        )

        Validator.verifiserValideringsFeil(kurs, 1, "Hvis enkeltdagEllerPeriode=ENKELTDAG må det eksistere kursdager og ikke reise")
    }

    // Test for kursholder validering
    @Test
    fun `Forvent feil dersom det ikke sendes med navn paa kursholder`() {
        val kurs = Kurs(
            kursholder = Kursholder(UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8"), ""),
            enkeltdagEllerPeriode = KursVarighetType.PERIODE,
            kursperioder = listOf(Periode(mandag, torsdag)),
            reise = STANDARD_REISE
        )

        Validator.verifiserValideringsFeil(kurs, 1, "Kan ikke være tom")
    }

    // Tester for reise validering
    @Test
    fun `Forvent feil dersom det ikke sendes med beskrivelse for reise`() {
        val kurs = Kurs(
            kursholder = KJENT_KURSHOLDER,
            enkeltdagEllerPeriode = KursVarighetType.PERIODE,
            kursperioder = listOf(Periode(mandag, torsdag)),
            reise = Reise(
                reiserUtenforKursdager = true,
                reisedager = listOf(mandag, torsdag),
                reisedagerBeskrivelse = ""
            )
        )

        Validator.verifiserValideringsFeil(kurs, 1, "Dersom 'reiserUtenforKursdager' er true, må man sende med beskrivelse")
    }

    @Test
    fun `Forvent feil dersom det ikke sendes med reisedager`() {
        val kurs = Kurs(
            kursholder = KJENT_KURSHOLDER,
            enkeltdagEllerPeriode = KursVarighetType.PERIODE,
            kursperioder = listOf(Periode(mandag, torsdag)),
            reise = Reise(
                reiserUtenforKursdager = true,
                reisedager = listOf(),
                reisedagerBeskrivelse = "Derfor"
            )
        )

        Validator.verifiserValideringsFeil(kurs, 1, "Dersom 'reiserUtenforKursdager' er true, kan ikke 'reisedager' være tom liste")
    }

    // Tester for tilK9Format()
    @Test
    fun `Kurs med periode mappes riktig til k9Format`() {
        val kurs = Kurs(
            kursholder = KJENT_KURSHOLDER,
            enkeltdagEllerPeriode = KursVarighetType.PERIODE,
            kursperioder = listOf(Periode(mandag, torsdag)),
            reise = STANDARD_REISE
        )

        val k9Kurs = kurs.tilK9Format()
        val k9Reise = k9Kurs.reise

        assertEquals(kurs.kursholder.navn, k9Kurs.kursholder.navn)
        assertEquals(kurs.kursholder.uuid, k9Kurs.kursholder.institusjonUuid)
        assertEquals(kurs.kursperioder?.size, k9Kurs.kursperioder.size)
        assertEquals(kurs.kursperioder?.get(0)?.tilOgMed, k9Kurs.kursperioder[0].tilOgMed)
        assertEquals(kurs.kursperioder?.get(0)?.fraOgMed, k9Kurs.kursperioder[0].fraOgMed)

        assertNotNull(k9Reise)
        assertEquals(kurs.reise?.reiserUtenforKursdager, k9Reise!!.isReiserUtenforKursdager)
        assertEquals(kurs.reise?.reisedagerBeskrivelse, k9Reise.reisedagerBeskrivelse)
        assertEquals(kurs.reise?.reisedager?.size, k9Reise.reisedager.size)
        assertEquals(kurs.reise?.reisedager?.get(0), k9Reise.reisedager[0])
    }

    @Test
    fun `Kurs med enkeltdager mappes riktig til k9Format`() {
        val kurs = Kurs(
            kursholder = KJENT_KURSHOLDER,
            enkeltdagEllerPeriode = KursVarighetType.ENKELTDAG,
            kursdager = STANDARD_KURSDAGER,
            reise = null
        )

        val k9Kurs = kurs.tilK9Format()
        val k9Reise = k9Kurs.reise

        // Verify kursholder mapping
        assertEquals(kurs.kursholder.navn, k9Kurs.kursholder.navn)
        assertEquals(kurs.kursholder.uuid, k9Kurs.kursholder.institusjonUuid)

        // Verify kursdager are mapped to periods
        assertEquals(kurs.kursdager?.size, k9Kurs.kursperioder.size)
        assertEquals(kurs.kursdager?.get(0)?.dato, k9Kurs.kursperioder[0].fraOgMed)
        assertEquals(kurs.kursdager?.get(0)?.dato, k9Kurs.kursperioder[0].tilOgMed)
        assertEquals(kurs.kursdager?.get(1)?.dato, k9Kurs.kursperioder[1].fraOgMed)
        assertEquals(kurs.kursdager?.get(1)?.dato, k9Kurs.kursperioder[1].tilOgMed)
        assertEquals(kurs.kursdager?.get(2)?.dato, k9Kurs.kursperioder[2].fraOgMed)
        assertEquals(kurs.kursdager?.get(2)?.dato, k9Kurs.kursperioder[2].tilOgMed)

        // Verify that a default reise object is created with reiserUtenforKursdager = false
        assertNotNull(k9Reise)
        assertEquals(false, k9Reise!!.isReiserUtenforKursdager)
        assertNull(k9Reise.reisedager)
        assertNull(k9Reise.reisedagerBeskrivelse)
    }

    @Test
    fun `Kursholder uten UUID mappes riktig til k9Format`() {
        val kursholder = Kursholder(uuid = null, navn = "Test Kursholder")
        val kurs = Kurs(
            kursholder = kursholder,
            enkeltdagEllerPeriode = KursVarighetType.PERIODE,
            kursperioder = listOf(Periode(mandag, torsdag)),
            reise = STANDARD_REISE
        )

        val k9Kurs = kurs.tilK9Format()

        assertEquals(kursholder.navn, k9Kurs.kursholder.navn)
        assertNull(k9Kurs.kursholder.institusjonUuid)
    }

    @Test
    fun `Reise uten reiser utenfor kursdager mappes riktig til k9Format`() {
        val reise = Reise(
            reiserUtenforKursdager = false,
            reisedager = null,
            reisedagerBeskrivelse = null
        )
        val kurs = Kurs(
            kursholder = KJENT_KURSHOLDER,
            enkeltdagEllerPeriode = KursVarighetType.PERIODE,
            kursperioder = listOf(Periode(mandag, torsdag)),
            reise = reise
        )

        val k9Kurs = kurs.tilK9Format()
        val k9Reise = k9Kurs.reise

        assertNotNull(k9Reise)
        assertEquals(false, k9Reise!!.isReiserUtenforKursdager)
        assertNull(k9Reise.reisedager)
        assertNull(k9Reise.reisedagerBeskrivelse)
    }
}
