package no.nav.k9brukerdialogprosessering.api.ytelse.omsorgspengerutbetalingsnf.domene

import no.nav.k9.søknad.JsonUtils
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Land
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Næringstype.DAGMAMMA
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Regnskapsfører
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.VarigEndring
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Virksomhet
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.YrkesaktivSisteTreFerdigliknedeArene
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate

class SelvstendigNæringsdrivendeTest {

    @Test
    fun `SelvstendigNæringsdrivende blir mappet til riktig K9SelvstendigNæringsdrivende`() {
        val k9Virksomhet = Virksomhet(
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-10-01"),
            næringstype = DAGMAMMA,
            næringsinntekt = 3_000_000,
            navnPåVirksomheten = "Kiwi ASA",
            organisasjonsnummer = "975959171",
            registrertINorge = false,
            registrertIUtlandet = Land("BEL", "Belgia"),
            yrkesaktivSisteTreFerdigliknedeÅrene = YrkesaktivSisteTreFerdigliknedeArene(
                oppstartsdato = LocalDate.parse("2022-01-01")
            ),
            varigEndring = VarigEndring(
                dato = LocalDate.parse("2022-01-01"),
                inntektEtterEndring = 1_500_00,
                forklaring = "Fordi atte atte atte"
            ),
            regnskapsfører = Regnskapsfører(
                navn = "Knut",
                telefon = "123123123"
            ),
            erNyoppstartet = true,
            harFlereAktiveVirksomheter = true
        ).somK9SelvstendigNæringsdrivende()

        val forventet = """
            {
              "perioder": {
                "2022-01-01/2022-10-01": {
                  "virksomhetstyper": [
                    "DAGMAMMA"
                  ],
                  "regnskapsførerNavn": "Knut",
                  "regnskapsførerTlf": "123123123",
                  "erVarigEndring": true,
                  "erNyIArbeidslivet": true,
                  "endringDato": "2022-01-01",
                  "endringBegrunnelse": "Fordi atte atte atte",
                  "bruttoInntekt": 150000,
                  "erNyoppstartet": true,
                  "registrertIUtlandet": true,
                  "landkode": "BEL"
                }
              },
              "organisasjonsnummer": "975959171",
              "virksomhetNavn": "Kiwi ASA"
            }
        """.trimIndent()

        JSONAssert.assertEquals(forventet, JsonUtils.toString(k9Virksomhet), true)
    }
}
