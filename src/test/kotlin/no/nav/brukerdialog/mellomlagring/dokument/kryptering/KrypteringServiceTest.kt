package no.nav.brukerdialog.mellomlagring.dokument.kryptering

import no.nav.brukerdialog.mellomlagring.dokument.DokumentEier
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KrypteringServiceTest {

    private lateinit var krypteringService: KrypteringService

    @BeforeEach
    fun setUp() {
        krypteringService = KrypteringService(
            krypteringsnøkkel = "testKrypteringsnøkkel"
        )
    }

    @Test
    fun `Kryptering fungerer`() {
        val vanligTekst = "testPlainText"
        val forventetKryptertTekst = "acHOtzr_9AL4t1J595KKjFSbjuVflYcONvZJmC0="

        val kryptertVerdi = krypteringService.krypter(
            id = "testId",
            plainText = vanligTekst,
            dokumentEier = DokumentEier("12345678901")
        )
        assertThat(kryptertVerdi).isEqualTo(forventetKryptertTekst)
    }

    @Test
    fun `Dekryptering fungerer`() {
        val kryptertTekst = "acHOtzr_9AL4t1J595KKjFSbjuVflYcONvZJmC0="
        val forventetKlartekst = "testPlainText"

        val dekryptertTekst = krypteringService.dekrypter(
            id = "testId",
            encrypted = kryptertTekst,
            dokumentEier = DokumentEier("12345678901")
        )

        assertThat(dekryptertTekst).isEqualTo(forventetKlartekst)
    }
}
