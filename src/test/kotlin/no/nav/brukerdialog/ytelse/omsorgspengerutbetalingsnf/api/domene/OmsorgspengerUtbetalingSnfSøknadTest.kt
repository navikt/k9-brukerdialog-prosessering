package no.nav.brukerdialog.ytelse.omsorgspengerutbetalingsnf.api.domene

import no.nav.brukerdialog.ytelse.fellesdomene.AktivitetFravær
import no.nav.brukerdialog.ytelse.fellesdomene.Bekreftelser
import no.nav.brukerdialog.ytelse.fellesdomene.Bosted
import no.nav.brukerdialog.ytelse.fellesdomene.FraværÅrsak
import no.nav.brukerdialog.ytelse.fellesdomene.Næringstype
import no.nav.brukerdialog.ytelse.fellesdomene.Opphold
import no.nav.brukerdialog.ytelse.fellesdomene.Utbetalingsperiode
import no.nav.brukerdialog.ytelse.fellesdomene.Virksomhet
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingsnf.api.domene.Barn
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingsnf.api.domene.Frilans
import no.nav.brukerdialog.ytelse.omsorgpengerutbetalingsnf.api.domene.TypeBarn
import no.nav.brukerdialog.ytelse.omsorgspengerutbetalingsnf.utils.SøknadUtils.defaultSøknad
import no.nav.brukerdialog.utils.SøknadUtils
import no.nav.brukerdialog.utils.SøknadUtils.Companion.metadata
import no.nav.brukerdialog.utils.SøknadUtils.Companion.somJson
import no.nav.brukerdialog.utils.TestUtils.Validator
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.Duration
import java.time.LocalDate

class OmsorgspengerUtbetalingSnfSøknadTest {

    @Test
    fun `Ugyldig opphold og bosteder skal gi validerinsfeil`() {
        Validator.verifiserValideringsFeil(
            defaultSøknad.copy(
                bosteder = listOf(
                    Bosted(
                        fraOgMed = LocalDate.now(),
                        tilOgMed = LocalDate.now().minusDays(2),
                        landkode = "BEL",
                        landnavn = "Belgia",
                        erEØSLand = true
                    )
                ),
                opphold = listOf(
                    Opphold(
                        fraOgMed = LocalDate.now(),
                        tilOgMed = LocalDate.now().plusDays(2),
                        landkode = "BEL",
                        landnavn = " ",
                        erEØSLand = true
                    )
                )
            ), 2, "'tilOgMed' må være lik eller etter 'fraOgMed'",
            "Kan ikke være tomt eller blankt"
        )
    }

    @Test
    fun `Ugyldig utbetalingsperioder skal gi valideringsfeil`() {
        Validator.verifiserValideringsFeil(
            defaultSøknad.copy(
                utbetalingsperioder = listOf(
                    Utbetalingsperiode(
                        fraOgMed = LocalDate.parse("2022-01-20"),
                        tilOgMed = LocalDate.parse("2022-01-19"),
                        årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                        aktivitetFravær = listOf(AktivitetFravær.SELVSTENDIG_VIRKSOMHET),
                        antallTimerPlanlagt = Duration.ofHours(5),
                        antallTimerBorte = null
                    ),
                    Utbetalingsperiode(
                        fraOgMed = LocalDate.parse("2022-01-20"),
                        tilOgMed = LocalDate.parse("2022-01-24"),
                        årsak = FraværÅrsak.ORDINÆRT_FRAVÆR,
                        aktivitetFravær = listOf(),
                        antallTimerPlanlagt = null,
                        antallTimerBorte = Duration.ofHours(5)
                    )
                )
            ), 4, "Kan ikke være tom",
            "'tilOgMed' må være lik eller etter 'fraOgMed'",
            "Dersom antallTimerPlanlagt er satt må antallTimerBorte være satt",
            "Dersom antallTimerBorte er satt må antallTimerPlanlagt være satt"
        )
    }

    @Test
    fun `Ugyldig parametere skal gi valideringsfeil`() {
        Validator.verifiserValideringsFeil(
            defaultSøknad.copy(
                barn = listOf(
                    Barn(
                        navn = "",
                        fødselsdato = LocalDate.now().minusYears(14),
                        type = TypeBarn.FRA_OPPSLAG,
                        identitetsnummer = "123ABC"
                    )
                ),
                frilans = Frilans(
                    startdato = LocalDate.parse("2022-02-01"),
                    sluttdato = LocalDate.parse("2022-01-01"),
                    jobberFortsattSomFrilans = true
                ),
                selvstendigNæringsdrivende = Virksomhet(
                    fraOgMed = LocalDate.parse("2022-01-01"),
                    tilOgMed = LocalDate.parse("2022-10-01"),
                    næringstype = Næringstype.DAGMAMMA,
                    navnPåVirksomheten = "Kiwi ASA",
                    organisasjonsnummer = "123ABC",
                    erNyoppstartet = true,
                    registrertINorge = true,
                    harFlereAktiveVirksomheter = false
                ),
                bekreftelser = Bekreftelser(
                    harBekreftetOpplysninger = false,
                    harForståttRettigheterOgPlikter = false
                ),
            ), 8,
            "'123ABC' matcher ikke tillatt pattern '^\\d+$'",
            "size must be between 11 and 11",
            "Kan ikke være tomt eller blankt",
            "'Sluttdato' må være lik eller etter 'startdato'",
            "Dersom 'jobberFortsattSomFrilans' er true, kan ikke 'sluttdato' være satt",
            "'123ABC' matcher ikke tillatt pattern '^\\d+$'",
            "Opplysningene må bekreftes for å sende inn søknad",
            "Må ha forstått rettigheter og plikter for å sende inn søknad"
        )
    }

    @Test
    fun `Gyldig søknad blir mappet til forventet k9Format`() {
        val søknad = defaultSøknad
        val k9Format = søknad.somK9Format(SøknadUtils.søker, metadata).somJson()
        val forventetK9Format = """
            {
              "søknadId": "${søknad.søknadId}",
              "versjon": "1.1.0",
              "mottattDato": "2022-01-02T03:04:05Z",
              "søker": {
                "norskIdentitetsnummer": "02119970078"
              },
              "ytelse": {
                "type": "OMP_UT",
                "fosterbarn": [
                  {
                    "norskIdentitetsnummer": "26104500284",
                    "fødselsdato": null
                  }
                ],
                "aktivitet": {
                  "selvstendigNæringsdrivende": [
                    {
                      "perioder": {
                        "2022-01-01/2022-10-01": {
                          "virksomhetstyper": [
                            "JORDBRUK_SKOGBRUK"
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
                  ],
                  "frilanser": {
                    "startdato": "2022-01-01",
                    "sluttdato": "2022-10-01"
                  }
                },
                "fraværsperioder": [
                  {
                    "periode": "2022-01-20/2022-01-25",
                    "duration": "PT5H30M",
                    "årsak": "ORDINÆRT_FRAVÆR",
                    "søknadÅrsak": null,
                    "aktivitetFravær": [
                      "FRILANSER"
                    ],
                    "arbeidsforholdId": null,
                    "arbeidsgiverOrgNr": null,
                    "delvisFravær": {
                      "normalarbeidstid":"PT7H",
                      "fravær":"PT5H"
                    }
                  },
                  {
                    "periode": "2022-01-20/2022-01-25",
                    "duration": null,
                    "årsak": "ORDINÆRT_FRAVÆR",
                    "søknadÅrsak": null,
                    "aktivitetFravær": [
                      "SELVSTENDIG_VIRKSOMHET"
                    ],
                    "arbeidsforholdId": null,
                    "arbeidsgiverOrgNr": null,
                    "delvisFravær": null
                  }
                ],
                "fraværsperioderKorrigeringIm": null,
                "bosteder": {
                  "perioder": {
                    "2022-01-01/2022-01-10": {
                      "land": "NLD"
                    }
                  },
                  "perioderSomSkalSlettes": {}
                },
                "utenlandsopphold": {
                  "perioder": {
                    "2022-02-01/2022-02-10": {
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
        JSONAssert.assertEquals(forventetK9Format, k9Format, true)
    }
}
