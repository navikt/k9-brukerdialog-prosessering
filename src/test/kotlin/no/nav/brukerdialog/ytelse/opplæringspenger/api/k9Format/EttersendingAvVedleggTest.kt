package no.nav.brukerdialog.ytelse.opplæringspenger.api.k9Format

import jakarta.validation.Validation
import jakarta.validation.Validator
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.EttersendingAvVedlegg
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.VedleggType
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EttersendingAvVedleggTest {
    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `gir ingen feil når skalEttersendeVedlegg er true og vedleggSomSkalEttersendes ikke er tomt`() {
        val ettersending = EttersendingAvVedlegg(
            skalEttersendeVedlegg = true,
            vedleggSomSkalEttersendes = listOf(VedleggType.LEGEERKLÆRING)
        )

        val violations = validator.validate(ettersending)
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `gir feil når skalEttersendeVedlegg er true og vedleggSomSkalEttersendes er tomt`() {
        val ettersending = EttersendingAvVedlegg(
            skalEttersendeVedlegg = true,
            vedleggSomSkalEttersendes = listOf()
        )

        val violations = validator.validate(ettersending)
        assertFalse(violations.isEmpty())
        assertTrue(violations.size == 1)
        assertTrue((violations.all { it.message.contains("Vedlegg som skal ettersendes kan ikke være tomt når skalEttersendeVedlegg er true.") }))

    }

    @Test
    fun `gir feil når skalEttersendeVedlegg er true og vedleggSomSkalEttersendes er null`() {
        val ettersending = EttersendingAvVedlegg(
            skalEttersendeVedlegg = true,
            vedleggSomSkalEttersendes = null
        )

        val violations = validator.validate(ettersending)
        assertFalse(violations.isEmpty())
        assertTrue(violations.size == 1)
        assertTrue((violations.all { it.message.contains("Vedlegg som skal ettersendes kan ikke være tomt når skalEttersendeVedlegg er true.") }))

    }
}