package no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.domene.k9Format

import no.nav.k9.søknad.JsonUtils
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.SøknadUtils
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.Enkeltdag
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.Ferieuttak
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.FerieuttakIPerioden
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.Omsorgstilbud
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.OmsorgstilbudSvarFortid
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.OmsorgstilbudSvarFremtid
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.PlanUkedager
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.tilK9Tilsynsordning0Timer
import no.nav.k9brukerdialogprosessering.config.JacksonConfiguration.Companion.zonedDateTimeFormatter
import no.nav.k9brukerdialogprosessering.oppslag.soker.Søker
import no.nav.k9brukerdialogprosessering.utils.SøknadUtils.Companion.metadata
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

class K9FormatTest {

    @Test
    fun `Full PP søknad blir til riktig K9Format`() {
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
            omsorgstilbud = Omsorgstilbud(
                svarFortid = OmsorgstilbudSvarFortid.JA,
                svarFremtid = null,
                enkeltdager = null,
                erLiktHverUke = true,
                ukedager = PlanUkedager(
                    mandag = Duration.ofHours(4),
                    tirsdag = null,
                    onsdag = Duration.ofHours(2),
                    torsdag = null,
                    fredag = Duration.ofHours(5),
                )
            )
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
                "type": "PLEIEPENGER_SYKT_BARN",
                "barn": {
                  "norskIdentitetsnummer": "03028104560",
                  "fødselsdato": null
                },
                "erSammenMedBarnet": null,
                "søknadsperiode": [
                  "2021-01-01/2021-01-10"
                ],
                "endringsperiode": [],
                "trekkKravPerioder": [],
                "beredskap" : {
                  "perioder" : {
                    "2021-01-01/2021-01-10" : {
                      "tilleggsinformasjon" : "Ikke beredskap"
                    }
                  },
                  "perioderSomSkalSlettes" : { }
                },
                "nattevåk" : {
                  "perioder" : {
                    "2021-01-01/2021-01-10" : {
                      "tilleggsinformasjon" : "Æ har nattevåk"
                    }
                  },
                  "perioderSomSkalSlettes" : { }
                },
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
                "dataBruktTilUtledning": null,
                "infoFraPunsj": null,
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
                "tilsynsordning": {
                  "perioder": {
                    "2021-01-01/2021-01-01": {
                      "etablertTilsynTimerPerDag": "PT5H"
                    },
                    "2021-01-04/2021-01-04": {
                      "etablertTilsynTimerPerDag": "PT4H"
                    },
                    "2021-01-05/2021-01-05": {
                      "etablertTilsynTimerPerDag": "PT0S"
                    },
                    "2021-01-06/2021-01-06": {
                      "etablertTilsynTimerPerDag": "PT2H"
                    },
                    "2021-01-07/2021-01-07": {
                      "etablertTilsynTimerPerDag": "PT0S"
                    },
                    "2021-01-08/2021-01-08": {
                      "etablertTilsynTimerPerDag": "PT5H"
                    }
                  }
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
                "annetDataBruktTilUtledning": {
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

    @Test
    fun `gitt søknadsperiode man-fre, tilsyn alle dager, forvent 5 perioder`() {
        val k9Tilsynsordning = Omsorgstilbud(
            svarFortid = OmsorgstilbudSvarFortid.JA,
            svarFremtid = null,
            erLiktHverUke = true,
            ukedager = PlanUkedager(
                mandag = Duration.ofHours(5),
                tirsdag = Duration.ofHours(5),
                onsdag = Duration.ofHours(5),
                torsdag = Duration.ofHours(5),
                fredag = Duration.ofHours(5)
            )
        ).tilK9Tilsynsordning(
            periode = Periode(LocalDate.parse("2021-01-04"), LocalDate.parse("2021-01-08"))
        )

        assertEquals(5, k9Tilsynsordning.perioder.size)

        JSONAssert.assertEquals(
            //language=json
            """
            {
              "perioder" : {
                "2021-01-04/2021-01-04" : {
                  "etablertTilsynTimerPerDag" : "PT5H"
                },
                "2021-01-05/2021-01-05" : {
                  "etablertTilsynTimerPerDag" : "PT5H"
                },
                "2021-01-06/2021-01-06" : {
                  "etablertTilsynTimerPerDag" : "PT5H"
                },
                "2021-01-07/2021-01-07" : {
                  "etablertTilsynTimerPerDag" : "PT5H"
                },
                "2021-01-08/2021-01-08" : {
                  "etablertTilsynTimerPerDag" : "PT5H"
                }
              }
            }
        """.trimIndent(), JsonUtils.toString(k9Tilsynsordning), true
        )
    }

    @Test
    fun `gitt søknadsperiode ons-man, tilsyn alle dager, forvent 4 perioder med lør-søn ekskludert`() {
        val k9Tilsynsordning = Omsorgstilbud(
            svarFortid = OmsorgstilbudSvarFortid.JA,
            svarFremtid = null,
            erLiktHverUke = true,
            ukedager = PlanUkedager(
                mandag = Duration.ofHours(5),
                tirsdag = Duration.ofHours(5),
                onsdag = Duration.ofHours(5),
                torsdag = Duration.ofHours(5),
                fredag = Duration.ofHours(5)
            )
        ).tilK9Tilsynsordning(
            periode = Periode(LocalDate.parse("2021-01-06"), LocalDate.parse("2021-01-11"))
        )

        assertEquals(4, k9Tilsynsordning.perioder.size)

        JSONAssert.assertEquals(
            //language=json
            """
            {
              "perioder" : {
                "2021-01-06/2021-01-06" : {
                  "etablertTilsynTimerPerDag" : "PT5H"
                },
                "2021-01-07/2021-01-07" : {
                  "etablertTilsynTimerPerDag" : "PT5H"
                },
                "2021-01-08/2021-01-08" : {
                  "etablertTilsynTimerPerDag" : "PT5H"
                },
                "2021-01-11/2021-01-11" : {
                  "etablertTilsynTimerPerDag" : "PT5H"
                }
              }
            }
        """.trimIndent(), JsonUtils.toString(k9Tilsynsordning), true
        )
    }

    @Test
    fun `gitt søknadsperiode man-fre, tilsyn man-ons og fre, forvent 4 perioder med tilsyn og 1 uten`() {
        val k9Tilsynsordning = Omsorgstilbud(
            svarFortid = OmsorgstilbudSvarFortid.JA,
            svarFremtid = null,
            erLiktHverUke = true,
            ukedager = PlanUkedager(
                mandag = Duration.ofHours(5),
                tirsdag = Duration.ofHours(5),
                onsdag = Duration.ofHours(5),
                torsdag = null,
                fredag = Duration.ofHours(5)
            )
        ).tilK9Tilsynsordning(
            periode = Periode(LocalDate.parse("2021-01-04"), LocalDate.parse("2021-01-08"))
        )

        assertEquals(5, k9Tilsynsordning.perioder.size)

        JSONAssert.assertEquals(
            //language=json
            """
            {
              "perioder" : {
                "2021-01-04/2021-01-04" : {
                  "etablertTilsynTimerPerDag" : "PT5H"
                },
                "2021-01-05/2021-01-05" : {
                  "etablertTilsynTimerPerDag" : "PT5H"
                },
                "2021-01-06/2021-01-06" : {
                  "etablertTilsynTimerPerDag" : "PT5H"
                },
                "2021-01-07/2021-01-07" : {
                  "etablertTilsynTimerPerDag" : "PT0S"
                },
                "2021-01-08/2021-01-08" : {
                  "etablertTilsynTimerPerDag" : "PT5H"
                }
              }
            }
        """.trimIndent(), JsonUtils.toString(k9Tilsynsordning), true
        )
    }

    @Test
    fun `gitt søknadsperiode man-fre, uten tilsyn, forvent 1 periode med 0 timer`() {
        val k9Tilsynsordning =
            tilK9Tilsynsordning0Timer(Periode(LocalDate.parse("2021-01-04"), LocalDate.parse("2021-01-08")))

        assertEquals(1, k9Tilsynsordning.perioder.size)

        JSONAssert.assertEquals(
            //language=json
            """
            {
              "perioder" : {
                "2021-01-04/2021-01-08" : {
                  "etablertTilsynTimerPerDag" : "PT0S"
                }
              }
            }
        """.trimIndent(), JsonUtils.toString(k9Tilsynsordning), true
        )
    }

    @Test
    fun `gitt søknadsperiode man-fre, tilsyn 10t alle dager, forvent 5 perioder med 7t 30m`() {
        val k9Tilsynsordning = Omsorgstilbud(
            svarFortid = OmsorgstilbudSvarFortid.JA,
            svarFremtid = null,
            erLiktHverUke = true,
            ukedager = PlanUkedager(
                mandag = Duration.ofHours(10),
                tirsdag = Duration.ofHours(10),
                onsdag = Duration.ofHours(10),
                torsdag = Duration.ofHours(10),
                fredag = Duration.ofHours(10)
            )
        ).tilK9Tilsynsordning(
            periode = Periode(LocalDate.parse("2021-01-04"), LocalDate.parse("2021-01-08"))
        )

        assertEquals(5, k9Tilsynsordning.perioder.size)

        JSONAssert.assertEquals(
            //language=json
            """
            {
              "perioder" : {
                "2021-01-04/2021-01-04" : {
                  "etablertTilsynTimerPerDag" : "PT7H30M"
                },
                "2021-01-05/2021-01-05" : {
                  "etablertTilsynTimerPerDag" : "PT7H30M"
                },
                "2021-01-06/2021-01-06" : {
                  "etablertTilsynTimerPerDag" : "PT7H30M"
                },
                "2021-01-07/2021-01-07" : {
                  "etablertTilsynTimerPerDag" : "PT7H30M"
                },
                "2021-01-08/2021-01-08" : {
                  "etablertTilsynTimerPerDag" : "PT7H30M"
                }
              }
            }
        """.trimIndent(), JsonUtils.toString(k9Tilsynsordning), true
        )
    }

    @Test
    fun `gitt omsorgstilbud med 4 enkeltdager, forvent riktig mapping`() {
        val tilsynsordning = Omsorgstilbud(
            svarFortid = OmsorgstilbudSvarFortid.JA,
            svarFremtid = OmsorgstilbudSvarFremtid.NEI,
            erLiktHverUke = false,
            enkeltdager = listOf(
                Enkeltdag(
                    LocalDate.parse("2021-01-01"),
                    Duration.ofHours(4)
                ),
                Enkeltdag(
                    LocalDate.parse("2021-01-02"),
                    Duration.ofHours(4)
                ),
                Enkeltdag(
                    LocalDate.parse("2021-01-03"),
                    Duration.ofHours(4)
                ),
                Enkeltdag(
                    LocalDate.parse("2021-01-04"),
                    Duration.ofHours(4)
                )
            )
        ).tilK9Tilsynsordning(Periode(LocalDate.now(), LocalDate.now().plusDays(7)))

        assertEquals(4, tilsynsordning.perioder.size)
    }

    @Test
    fun `Omsorgstilbud med ukedager blir som forventet k9format`(){
        val tilsynsordning = Omsorgstilbud(
            svarFortid = OmsorgstilbudSvarFortid.JA,
            svarFremtid = null,
            erLiktHverUke = true,
            enkeltdager = null,
            ukedager = PlanUkedager(
                mandag = Duration.ofHours(1),
                tirsdag = Duration.ofHours(2),
                onsdag = Duration.ofHours(3),
                torsdag = Duration.ofHours(4),
                fredag = Duration.ofHours(5),
            )
        ).tilK9Tilsynsordning(Periode(LocalDate.parse("2022-01-07"), LocalDate.parse("2022-01-14")))

        //language=json
        val forventet = """
            {
              "perioder": {
                "2022-01-07/2022-01-07": {
                  "etablertTilsynTimerPerDag": "PT5H"
                },
                "2022-01-10/2022-01-10": {
                  "etablertTilsynTimerPerDag": "PT1H"
                },
                "2022-01-11/2022-01-11": {
                  "etablertTilsynTimerPerDag": "PT2H"
                },
                "2022-01-12/2022-01-12": {
                  "etablertTilsynTimerPerDag": "PT3H"
                },
                "2022-01-13/2022-01-13": {
                  "etablertTilsynTimerPerDag": "PT4H"
                },
                "2022-01-14/2022-01-14": {
                  "etablertTilsynTimerPerDag": "PT5H"
                }
              }
            }
        """.trimIndent()

        JSONAssert.assertEquals(forventet, JsonUtils.toString(tilsynsordning), true)
    }
}
