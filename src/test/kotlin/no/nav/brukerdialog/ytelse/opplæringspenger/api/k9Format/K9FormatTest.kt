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
            "mottattDato" : "2020-01-02T03:04:05Z",
            "språk" : "nb",
            "søker" : {
              "norskIdentitetsnummer" : "123456789"
            },
            "søknadId": "$søknadId",
            "versjon" : "1.0.0",
            "ytelse" : {
              "type" : "OPPLÆRINGSPENGER",
              "arbeidstid" : {
                "arbeidstakerList" : [ {
                  "arbeidstidInfo" : {
                    "perioder" : {
                      "2021-01-01/2021-01-01" : {
                        "faktiskArbeidTimerPerDag" : "PT4H",
                        "jobberNormaltTimerPerDag" : "PT8H"
                      },
                      "2021-01-02/2021-01-02" : {
                        "faktiskArbeidTimerPerDag" : "PT4H",
                        "jobberNormaltTimerPerDag" : "PT8H"
                      },
                      "2021-01-03/2021-01-03" : {
                        "faktiskArbeidTimerPerDag" : "PT4H",
                        "jobberNormaltTimerPerDag" : "PT8H"
                      },
                      "2021-01-04/2021-01-04" : {
                        "faktiskArbeidTimerPerDag" : "PT4H",
                        "jobberNormaltTimerPerDag" : "PT8H"
                      },
                      "2021-01-05/2021-01-05" : {
                        "faktiskArbeidTimerPerDag" : "PT4H",
                        "jobberNormaltTimerPerDag" : "PT8H"
                      },
                      "2021-01-06/2021-01-06" : {
                        "faktiskArbeidTimerPerDag" : "PT4H",
                        "jobberNormaltTimerPerDag" : "PT8H"
                      },
                      "2021-01-07/2021-01-07" : {
                        "faktiskArbeidTimerPerDag" : "PT4H",
                        "jobberNormaltTimerPerDag" : "PT8H"
                      },
                      "2021-01-08/2021-01-08" : {
                        "faktiskArbeidTimerPerDag" : "PT4H",
                        "jobberNormaltTimerPerDag" : "PT8H"
                      },
                      "2021-01-09/2021-01-09" : {
                        "faktiskArbeidTimerPerDag" : "PT4H",
                        "jobberNormaltTimerPerDag" : "PT8H"
                      },
                      "2021-01-10/2021-01-10" : {
                        "faktiskArbeidTimerPerDag" : "PT4H",
                        "jobberNormaltTimerPerDag" : "PT8H"
                      }
                    }
                  },
                  "norskIdentitetsnummer" : null,
                  "organisasjonsnavn" : "Org",
                  "organisasjonsnummer" : "917755736"
                }, {
                  "arbeidstidInfo" : {
                    "perioder" : {
                      "2021-01-01/2021-01-10" : {
                        "faktiskArbeidTimerPerDag" : "PT0S",
                        "jobberNormaltTimerPerDag" : "PT0S"
                      }
                    }
                  },
                  "norskIdentitetsnummer" : null,
                  "organisasjonsnavn" : "JobberIkkeHerLenger",
                  "organisasjonsnummer" : "977155436"
                } ],
                "frilanserArbeidstidInfo" : {
                  "perioder" : {
                    "2021-01-01/2021-01-10" : {
                      "faktiskArbeidTimerPerDag" : "PT0S",
                      "jobberNormaltTimerPerDag" : "PT0S"
                    }
                  }
                },
                "selvstendigNæringsdrivendeArbeidstidInfo" : {
                  "perioder" : {
                    "2021-01-01/2021-01-01" : {
                      "faktiskArbeidTimerPerDag" : "PT7H30M",
                      "jobberNormaltTimerPerDag" : "PT7H30M"
                    },
                    "2021-01-02/2021-01-02" : {
                      "faktiskArbeidTimerPerDag" : "PT7H30M",
                      "jobberNormaltTimerPerDag" : "PT7H30M"
                    },
                    "2021-01-03/2021-01-03" : {
                      "faktiskArbeidTimerPerDag" : "PT7H30M",
                      "jobberNormaltTimerPerDag" : "PT7H30M"
                    },
                    "2021-01-04/2021-01-04" : {
                      "faktiskArbeidTimerPerDag" : "PT7H30M",
                      "jobberNormaltTimerPerDag" : "PT7H30M"
                    },
                    "2021-01-05/2021-01-05" : {
                      "faktiskArbeidTimerPerDag" : "PT7H30M",
                      "jobberNormaltTimerPerDag" : "PT7H30M"
                    },
                    "2021-01-06/2021-01-06" : {
                      "faktiskArbeidTimerPerDag" : "PT7H30M",
                      "jobberNormaltTimerPerDag" : "PT7H30M"
                    },
                    "2021-01-07/2021-01-07" : {
                      "faktiskArbeidTimerPerDag" : "PT7H30M",
                      "jobberNormaltTimerPerDag" : "PT7H30M"
                    },
                    "2021-01-08/2021-01-08" : {
                      "faktiskArbeidTimerPerDag" : "PT7H30M",
                      "jobberNormaltTimerPerDag" : "PT7H30M"
                    },
                    "2021-01-09/2021-01-09" : {
                      "faktiskArbeidTimerPerDag" : "PT7H30M",
                      "jobberNormaltTimerPerDag" : "PT7H30M"
                    },
                    "2021-01-10/2021-01-10" : {
                      "faktiskArbeidTimerPerDag" : "PT7H30M",
                      "jobberNormaltTimerPerDag" : "PT7H30M"
                    }
                  }
                }
              },
              "barn" : {
                "fødselsdato" : null,
                "norskIdentitetsnummer" : "03028104560"
              },
              "bosteder" : {
                "perioder" : {
                  "2017-01-01/2017-01-10" : {
                    "land" : "DEU"
                  },
                  "2018-01-01/2018-01-10" : {
                    "land" : "DEU"
                  }
                },
                "perioderSomSkalSlettes" : { }
              },
              "dataBruktTilUtledning" : {
                "annetData" : "{\"string\": \"tekst\", \"boolean\": false, \"number\": 1, \"array\": [1,2,3], \"object\": {\"key\": \"value\"}}",
                "harBekreftetOpplysninger" : true,
                "harForståttRettigheterOgPlikter" : true,
                "soknadDialogCommitSha" : "abc-123"
              },
              "kurs" : {
                "kursholder" : {
                  "holder" : null,
                  "institusjonsidentifikator" : null
                },
                "kursperioder" : [ {
                  "avreise" : "2022-01-01",
                  "begrunnelseReisetidHjem" : null,
                  "begrunnelseReisetidTil" : "Reisetid til kurs tok mer enn en dag",
                  "hjemkomst" : "2022-01-10",
                  "periode" : "2022-01-01/2022-01-10"
                } ]
              },
              "lovbestemtFerie" : {
                "perioder" : {
                  "2021-01-01/2021-01-02" : {
                    "skalHaFerie" : true
                  }
                }
              },
              "omsorg" : {
                "beskrivelseAvOmsorgsrollen" : "Gudfar til barnet",
                "relasjonTilBarnet" : "ANNET"
              },
              "opptjeningAktivitet" : {
                "frilanser" : {
                  "sluttdato" : "2021-05-01",
                  "startdato" : "2019-01-01"
                },
                "selvstendigNæringsdrivende" : [ {
                  "perioder" : {
                    "2015-01-01/2021-01-01" : {
                      "bruttoInntekt" : 9656876,
                      "erNyoppstartet" : false,
                      "landkode" : "CUB",
                      "registrertIUtlandet" : true,
                      "virksomhetstyper" : [ "ANNEN" ]
                    }
                  },
                  "virksomhetNavn" : "Bjarnes Bakeri"
                } ]
              },
              "søknadsperiode" : [ "2021-01-01/2021-01-10" ],
              "trekkKravPerioder" : [ ],
              "utenlandsopphold" : {
                "perioder" : { },
                "perioderSomSkalSlettes" : { }
              },
              "uttak" : {
                "perioder" : {
                  "2021-01-01/2021-01-10" : {
                    "timerPleieAvBarnetPerDag" : "PT7H30M"
                  }
                }
              }
            },
            "begrunnelseForInnsending" : {
              "tekst" : null
            },
            "journalposter" : [ ],
            "kildesystem" : "søknadsdialog"
          }
        """.trimIndent()

        println(JsonUtils.toString(k9Format))
        JSONAssert.assertEquals(forventetK9FormatJsonV2, JsonUtils.toString(k9Format), true)
    }
}
