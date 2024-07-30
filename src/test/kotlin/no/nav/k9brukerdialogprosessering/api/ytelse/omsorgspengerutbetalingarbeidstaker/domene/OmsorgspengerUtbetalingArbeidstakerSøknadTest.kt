package no.nav.k9brukerdialogprosessering.api.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.AktivitetFravær.ARBEIDSTAKER
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Bekreftelser
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.FraværÅrsak.SMITTEVERNHENSYN
import no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene.Utbetalingsperiode
import no.nav.k9brukerdialogprosessering.utils.SøknadUtils.Companion.metadata
import no.nav.k9brukerdialogprosessering.utils.SøknadUtils.Companion.somJson
import no.nav.k9brukerdialogprosessering.utils.SøknadUtils.Companion.søker
import no.nav.k9brukerdialogprosessering.utils.TestUtils.Validator
import no.nav.k9brukerdialogprosessering.utils.TestUtils.verifiserIngenValideringsFeil
import no.nav.k9brukerdialogprosessering.utils.TestUtils.verifiserValideringsFeil
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import java.util.*

class OmsorgspengerUtbetalingArbeidstakerSøknadTest {

    @Test
    fun `K9Format blir som forventet`() {
        val søknad = SøknadUtils.defaultSøknad
        val søknadId = UUID.randomUUID().toString()

        val faktiskK9Format = søknad.copy(søknadId = søknadId).somK9Format(søker, metadata).somJson()
        val forventetK9Format = """
            {
              "søknadId": "$søknadId",
              "versjon": "1.1.0",
              "mottattDato": "2022-01-02T03:04:05Z",
              "søker": {
                "norskIdentitetsnummer": "02119970078"
              },
              "ytelse": {
                "type": "OMP_UT",
                "fosterbarn": [],
                "aktivitet": {},
                "fraværsperioder": [
                  {
                    "periode": "2022-01-25/2022-01-28",
                    "duration": null,
                    "årsak": "SMITTEVERNHENSYN",
                    "søknadÅrsak": "KONFLIKT_MED_ARBEIDSGIVER",
                    "aktivitetFravær": [
                      "ARBEIDSTAKER"
                    ],
                    "arbeidsforholdId": null,
                    "arbeidsgiverOrgNr": "825905162",
                    "delvisFravær": null
                  }
                ],
                "fraværsperioderKorrigeringIm": null,
                "bosteder": {
                  "perioder": {
                    "2022-01-01/2022-01-10": {
                      "land": "BEL"
                    }
                  },
                  "perioderSomSkalSlettes": {}
                },
                "utenlandsopphold": {
                  "perioder": {
                    "2022-01-20/2022-01-25": {
                      "land": "BEL",
                      "årsak": null,
                      "erSammenMedBarnet": true
                    }
                  },
                  "perioderSomSkalSlettes": {}
                },
                "dataBruktTilUtledning": {
                    "harBekreftetOpplysninger": true,
                    "harForståttRettigheterOgPlikter": true,
                    "soknadDialogCommitSha": "abc-123",
                    "annetData": "{\"string\": \"tekst\", \"boolean\": false, \"number\": 1, \"array\": [1,2,3], \"object\": {\"key\": \"value\"}}"
                }
              },
              "språk": "nb",
              "journalposter": [],
              "begrunnelseForInnsending": {
                "tekst": null
              },
              "kildesystem": "søknadsdialog"
            }
        """.trimIndent()
        JSONAssert.assertEquals(forventetK9Format, faktiskK9Format, true)
    }

    @Test
    fun `Gyldig søknad gir ingen feil`() {
        OmsorgspengerutbetalingArbeidstakerSøknad(
            språk = "nb",
            vedlegg = listOf(),
            bosteder = listOf(),
            opphold = listOf(),
            bekreftelser = Bekreftelser(
                harBekreftetOpplysninger = true,
                harForståttRettigheterOgPlikter = true
            ),
            dineBarn = DineBarn(
                harDeltBosted = false,
                barn = listOf(
                    Barn(
                        identitetsnummer = "11223344567",
                        aktørId = "1234567890",
                        LocalDate.now(),
                        "Barn Barnesen",
                        TypeBarn.FRA_OPPSLAG
                    )
                ),
            ),
            arbeidsgivere = listOf(
                Arbeidsgiver(
                    navn = "Kiwi AS",
                    organisasjonsnummer = "825905162",
                    utbetalingsårsak = Utbetalingsårsak.KONFLIKT_MED_ARBEIDSGIVER,
                    konfliktForklaring = "Fordi blablabla",
                    harHattFraværHosArbeidsgiver = true,
                    arbeidsgiverHarUtbetaltLønn = true,
                    perioder = listOf(
                        Utbetalingsperiode(
                            fraOgMed = LocalDate.parse("2022-01-25"),
                            tilOgMed = LocalDate.parse("2022-01-28"),
                            årsak = SMITTEVERNHENSYN,
                            aktivitetFravær = listOf(ARBEIDSTAKER)
                        )
                    )
                )
            ),
            hjemmePgaSmittevernhensyn = true,
            hjemmePgaStengtBhgSkole = true
        ).valider()
    }

    @Test
    fun `Søknad uten arbeidsgivere gir feil`() {
        Validator.verifiserValideringsFeil(
            SøknadUtils.defaultSøknad.copy(arbeidsgivere = listOf()),
            1,
            "Må ha minst en arbeidsgiver satt"
        )
    }

    @Test
    fun `Gyldig søknad med dineBarn gir ingen feil`() {
        Validator.verifiserIngenValideringsFeil(
            SøknadUtils.defaultSøknad.copy(
                dineBarn = DineBarn(
                    harDeltBosted = false,
                    barn = listOf(
                        Barn(
                            identitetsnummer = "11223344567",
                            aktørId = "1234567890",
                            fødselsdato = LocalDate.now(),
                            navn = "Barn Barnesen",
                            TypeBarn.FRA_OPPSLAG
                        )
                    ),
                )
            )
        )
    }
}
