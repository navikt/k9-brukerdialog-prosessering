package no.nav.brukerdialog.ytelse.opplæringspenger.api.k9Format

import no.nav.brukerdialog.config.JacksonConfiguration.Companion.zonedDateTimeFormatter
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.brukerdialog.utils.SøknadUtils.Companion.metadata
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.Ferieuttak
import no.nav.brukerdialog.ytelse.opplæringspenger.api.domene.FerieuttakIPerioden
import no.nav.brukerdialog.ytelse.opplæringspenger.utils.SøknadUtils
import no.nav.k9.søknad.JsonUtils
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

class K9FormatTest {

    @Test
    fun `Full OLP søknad blir til riktig K9Format`() {
        val mottatt = ZonedDateTime.parse("2020-01-02T03:04:05Z", zonedDateTimeFormatter)
        val søknadId = UUID.randomUUID().toString()
        val fraOgMed = LocalDate.parse("2021-01-01")
        val tilOgMed = LocalDate.parse("2021-01-10")
        val søknad = SøknadUtils.defaultSøknad(søknadId).copy(
            fraOgMed = fraOgMed,
            tilOgMed = tilOgMed,
            mottatt = mottatt,
            ferieuttakIPerioden = FerieuttakIPerioden(
                skalTaUtFerieIPerioden = true, ferieuttak = listOf(
                    Ferieuttak(
                        fraOgMed = fraOgMed,
                        tilOgMed = fraOgMed.plusDays(1)
                    )
                )
            ),
        )
        val søker = Søker(
            aktørId = "12345",
            fødselsdato = LocalDate.parse("2000-01-01"),
            fødselsnummer = "123456789"
        )
        val k9Format = søknad.somK9Format(søker, metadata)
        val forventetK9FormatJsonV2 =
            //language=json
            """
            {
              "søknadId": "$søknadId",
              "versjon": "1.0.0",
              "mottattDato": "2020-01-02T03:04:05Z",
              "søker": {
                "norskIdentitetsnummer": "123456789"
              },
              "ytelse": {
                "type": "OPPLÆRINGSPENGER",
                "barn": {
                  "norskIdentitetsnummer": "03028104560",
                  "fødselsdato": null
                },
                "søknadsperiode": [
                  "2021-01-01/2021-01-10"
                ],
                "trekkKravPerioder": [],
                "opptjeningAktivitet": {
                  "selvstendigNæringsdrivende": [
                    {
                      "perioder": {
                        "2021-01-01/..": {
                          "virksomhetstyper": [
                            "ANNEN"
                          ],
                          "regnskapsførerNavn": "Kjell Regnskap",
                          "regnskapsførerTlf": "123456789",
                          "erVarigEndring": true,
                          "erNyIArbeidslivet" : true,
                          "endringDato": "2020-01-01",
                          "endringBegrunnelse": "Korona",
                          "bruttoInntekt": 9999,
                          "erNyoppstartet": true,
                          "registrertIUtlandet": true,
                          "landkode": "DEU"
                        }
                      },
                      "virksomhetNavn": "TullOgTøys"
                    }
                  ],
                  "frilanser": {
                    "startdato": "2018-01-01",
                    "sluttdato": null
                  }
                },
                "bosteder": {
                  "perioder": {
                    "2017-01-01/2017-01-10": {
                      "land": "DEU"
                    },
                    "2018-01-01/2018-01-10": {
                      "land": "DEU"
                    }
                  },
                  "perioderSomSkalSlettes": {}
                },
                "utenlandsopphold" : {
                  "perioder" : {
                    "2019-10-10/2019-10-14" : {
                      "land" : "SE",
                      "årsak" : null,
                      "erSammenMedBarnet": false
                    },
                    "2019-10-15/2019-10-20" : {
                      "land" : "SE",
                      "årsak" : "barnetInnlagtIHelseinstitusjonDekketEtterAvtaleMedEtAnnetLandOmTrygd",
                      "erSammenMedBarnet": false
                    },
                    "2019-10-21/2019-11-10" : {
                      "land" : "SE",
                      "årsak" : null,
                      "erSammenMedBarnet": false
                    },
                    "2020-11-10/2020-11-12" : {
                      "land" : "SE",
                      "årsak" : "barnetInnlagtIHelseinstitusjonForNorskOffentligRegning",
                      "erSammenMedBarnet": false
                    },
                    "2020-11-13/2020-11-15" : {
                      "land" : "SE",
                      "årsak" : null,
                      "erSammenMedBarnet": false
                    },
                    "2022-12-10/2022-12-20" : {
                      "land" : "SE",
                      "årsak" : null,
                      "erSammenMedBarnet": false
                    }
                  },
                  "perioderSomSkalSlettes" : { }
                },
                "lovbestemtFerie": {
                  "perioder": {
                    "$fraOgMed/${fraOgMed.plusDays(1)}": {
                      "skalHaFerie": true
                    }
                  }
                },
                "arbeidstid": {
                  "arbeidstakerList": [
                    {
                      "norskIdentitetsnummer": null,
                      "organisasjonsnummer": "917755736",
                      "organisasjonsnavn": "Org",
                      "arbeidstidInfo": {
                         "perioder": {
                           "2021-01-01/2021-01-10": {
                             "jobberNormaltTimerPerDag": "PT7H30M",
                             "faktiskArbeidTimerPerDag": "PT7H30M"
                           }
                         }
                       }
                    },
                    {
                      "norskIdentitetsnummer": null,
                      "organisasjonsnummer": "977155436",
                      "organisasjonsnavn": "JobberIkkeHerLenger",
                      "arbeidstidInfo": {
                        "perioder": {
                          "2021-01-01/2021-01-10": {
                            "jobberNormaltTimerPerDag": "PT0S",
                            "faktiskArbeidTimerPerDag": "PT0S"
                          }
                        }
                      }
                    }
                  ],
                  "frilanserArbeidstidInfo": {
                    "perioder": {
                          "2021-01-01/2021-01-10": {
                            "jobberNormaltTimerPerDag": "PT7H30M",
                            "faktiskArbeidTimerPerDag": "PT7H30M"
                          }
                     }
                  },
                  "selvstendigNæringsdrivendeArbeidstidInfo": {
                    "perioder": {
                          "2021-01-01/2021-01-10": {
                            "jobberNormaltTimerPerDag": "PT7H30M",
                            "faktiskArbeidTimerPerDag": "PT7H30M"
                          }
                     }
                  }
                },
                "uttak": {
                  "perioder": {
                    "2021-01-01/2021-01-10": {
                      "timerPleieAvBarnetPerDag": "PT7H30M"
                    }
                  }
                },
                "omsorg": {
                  "relasjonTilBarnet": "ANNET",
                  "beskrivelseAvOmsorgsrollen": "Gudfar til barnet"
                },
                "kurs" : {
                  "kursholder" : {
                    "holder" : null,
                    "institusjonsidentifikator" : "0edb9541-dda5-4dc4-bfaf-587d01448a6a"
                  },
                  "kursperioder" : [ {
                    "avreise" : "2022-01-01",
                    "begrunnelseReisetidHjem" : null,
                    "begrunnelseReisetidTil" : null,
                    "hjemkomst" : "2022-01-10",
                    "periode" : "2022-01-01/2022-01-10"
                  } ]
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

        println(JsonUtils.toString(k9Format))
        JSONAssert.assertEquals(forventetK9FormatJsonV2, JsonUtils.toString(k9Format), true)
    }
}
