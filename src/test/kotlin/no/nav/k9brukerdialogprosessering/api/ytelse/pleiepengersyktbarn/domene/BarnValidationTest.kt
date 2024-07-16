package no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.domene

import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.BarnDetaljer
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.valider
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.ÅrsakManglerIdentitetsnummer
import no.nav.k9brukerdialogprosessering.utils.TestUtils
import no.nav.k9brukerdialogprosessering.utils.TestUtils.assertFeilPå
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
    fun `Gyldig barn gir ingen feil`(){
        gyldigBarn.valider(felt).verifiserIngenFeil()
    }

    @Test
    fun `Når AktørId settes som ID på barnet kreves hverken relasjon til barnet eller navn`() {
        val barn = gyldigBarn.copy(
            aktørId = "10000001",
            navn = null
        )
        barn.valider(felt).verifiserIngenFeil()
    }

    @Test
    fun `Skal ikke gi feil selvom fødselsnummer er null så lenge fødselsdato og årsak er satt`() {
        val barn = gyldigBarn.copy(
            fødselsnummer = null,
            fødselsdato = LocalDate.parse("2021-01-01"),
            årsakManglerIdentitetsnummer = ÅrsakManglerIdentitetsnummer.NYFØDT
        )
        barn.valider(felt).verifiserIngenFeil()
    }

    @Test
    fun `Skal gi feil dersom fødselsnummer ikke settes og man ikke har satt fødsesldato og årsak`() {
        val barn = gyldigBarn.copy(
            fødselsnummer = null,
            årsakManglerIdentitetsnummer = null,
            fødselsdato = null
        )
        barn.valider(felt).assertFeilPå(listOf("barn.fødselsdato", "barn.årsakManglerIdentitetsnummer"))
    }

    @Test
    fun `Skal gi feil dersom fødselsdato er i fremtiden`() {
        val barn = gyldigBarn.copy(
            fødselsdato = LocalDate.now().plusDays(1)
        )
        barn.valider(felt).assertFeilPå(listOf("barn.fødselsdato"))
    }

    @Test
    fun `Forvent valideringsfeil dersom norskIdentifikator er ugyldig`() {
        TestUtils.VALIDATOR.validate(
            gyldigBarn.copy(fødselsnummer = "123ABC")
        ).verifiserFeil(
            2,
            "'123ABC' matcher ikke tillatt pattern '^\\d+\$'",
            "size must be between 11 and 11"
        )
    }
}
