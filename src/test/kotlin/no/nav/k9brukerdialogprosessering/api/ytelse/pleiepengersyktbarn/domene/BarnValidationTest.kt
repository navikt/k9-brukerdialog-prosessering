package no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.domene

import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.BarnDetaljer
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.ÅrsakManglerIdentitetsnummer
import no.nav.k9brukerdialogprosessering.utils.TestUtils.VALIDATOR
import no.nav.k9brukerdialogprosessering.utils.TestUtils.verifiserFeil
import no.nav.k9brukerdialogprosessering.utils.TestUtils.verifiserIngenFeil
import org.junit.jupiter.api.Test
import java.time.LocalDate

class BarnValidationTest {
    private companion object {
        val felt = "barn"
        val gyldigBarn = BarnDetaljer(
            fødselsnummer = "02119970078",
            fødselsdato = LocalDate.parse("2021-01-01"),
            aktørId = "10000001",
            navn = "Barnesen",
            årsakManglerIdentitetsnummer = null
        )
    }

    @Test
    fun `Gyldig barn gir ingen feil`() {
        VALIDATOR.validate(gyldigBarn).verifiserIngenFeil()
    }

    @Test
    fun `Skal ikke gi feil selvom fødselsnummer er null så lenge fødselsdato og årsak er satt`() {
        VALIDATOR.validate(
            gyldigBarn.copy(
                fødselsnummer = null,
                fødselsdato = LocalDate.parse("2021-01-01"),
                årsakManglerIdentitetsnummer = ÅrsakManglerIdentitetsnummer.NYFØDT
            )
        ).verifiserIngenFeil()
    }

    @Test
    fun `Skal gi feil dersom fødselsnummer ikke settes og man ikke har satt fødsesldato og årsak`() {
        VALIDATOR.validate(
            gyldigBarn.copy(
                fødselsnummer = null,
                årsakManglerIdentitetsnummer = null,
                fødselsdato = null
            )
        ).verifiserFeil(2, "Må være satt dersom fødselsnummer er null.", "Må være satt dersom fødselsnummer er null.")
    }

    @Test
    fun `Skal gi feil dersom fødselsdato er i fremtiden`() {
        VALIDATOR.validate(
            gyldigBarn.copy(
                fødselsdato = LocalDate.now().plusDays(1)
            )
        ).verifiserFeil(1, "kan ikke være i fremtiden.")
    }

    @Test
    fun `Forvent valideringsfeil dersom norskIdentifikator er ugyldig`() {
        VALIDATOR.validate(
            gyldigBarn.copy(fødselsnummer = "123ABC")
        ).verifiserFeil(
            2,
            "'123ABC' matcher ikke tillatt pattern '^\\d+\$'",
            "size must be between 11 and 11"
        )
    }
}


