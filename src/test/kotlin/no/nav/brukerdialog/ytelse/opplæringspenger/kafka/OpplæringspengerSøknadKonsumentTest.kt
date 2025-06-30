package no.nav.brukerdialog.ytelse.opplæringspenger.kafka

import io.mockk.coEvery
import io.mockk.coVerify
import no.nav.brukerdialog.AbstractIntegrationTest
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.config.JacksonConfiguration.Companion.zonedDateTimeFormatter
import no.nav.brukerdialog.kafka.types.TopicEntry
import no.nav.brukerdialog.utils.KafkaUtils.leggPåTopic
import no.nav.brukerdialog.utils.KafkaUtils.lesMelding
import no.nav.brukerdialog.utils.MockMvcUtils.sendInnSøknad
import no.nav.brukerdialog.utils.TokenTestUtils.hentToken
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.OLPTopologyConfiguration.Companion.OLP_CLEANUP_TOPIC
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.OLPTopologyConfiguration.Companion.OLP_MOTTATT_TOPIC
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.OLPTopologyConfiguration.Companion.OLP_PREPROSESSERT_TOPIC
import no.nav.brukerdialog.ytelse.opplæringspenger.utils.OlpPdfSøknadUtils
import no.nav.brukerdialog.ytelse.opplæringspenger.utils.SøknadUtils.Companion.defaultSøknad
import org.intellij.lang.annotations.Language
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.net.URI
import java.time.ZonedDateTime
import java.util.*

class OpplæringspengerSøknadKonsumentTest : AbstractIntegrationTest() {

    override val consumerGroupPrefix = "olp"
    override val consumerGroupTopics = listOf(OLP_MOTTATT_TOPIC, OLP_PREPROSESSERT_TOPIC, OLP_CLEANUP_TOPIC)

    @Test
    fun `forvent at melding konsumeres riktig og dokumenter blir slettet`() {
        mockSøker()
        mockBarn()
        mockLagreDokument()
        mockJournalføring()

        val defaultSøknad = defaultSøknad().copy(
            vedlegg = listOf(),
            barn = defaultSøknad().barn.copy(
                fødselsattestVedleggUrls = listOf()
            )
        )

        mockMvc.sendInnSøknad(defaultSøknad, mockOAuth2Server.hentToken())

        coVerify(exactly = 1, timeout = 120 * 1000) {
            dokumentService.slettDokumenter(any(), any())
        }
    }

    @Test
    fun `Forvent at melding bli prosessert på 5 forsøk etter 4 feil`() {
        val søknadId = UUID.randomUUID().toString()
        val mottatt = ZonedDateTime.parse("2024-07-19T08:36:49.490Z", zonedDateTimeFormatter)
        val søknadMottatt = OlpPdfSøknadUtils.gyldigSøknad(soknadsId = søknadId, mottatt = mottatt)
        val correlationId = UUID.randomUUID().toString()
        val metadata = MetaInfo(version = 1, correlationId = correlationId)
        val topicEntry = TopicEntry(metadata, søknadMottatt)
        val topicEntryJson = objectMapper.writeValueAsString(topicEntry)

        coEvery { dokumentService.lagreDokument(any(), any(), any()) }
            .throws(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
            .andThenMany(listOf("123456789", "987654321"))

        producer.leggPåTopic(key = søknadId, value = topicEntryJson, topic = OLP_MOTTATT_TOPIC)
        val lesMelding = consumer.lesMelding(key = søknadId, topic = OLP_PREPROSESSERT_TOPIC).value()

        val preprosessertSøknadJson = JSONObject(lesMelding).getJSONObject("data").toString()
        JSONAssert.assertEquals(preprosessertSøknadSomJson(søknadId, mottatt.toString()), preprosessertSøknadJson, true)
    }

    @Language("JSON")
    private fun preprosessertSøknadSomJson(søknadId: String, mottatt: String) = """          
          {
            "harForstattRettigheterOgPlikter": true,
            "selvstendigNæringsdrivende": {
              "arbeidsforhold": {
                "arbeidIPeriode": {
                  "enkeltdager": [
                    {
                      "dato": "2021-01-01",
                      "tid": "PT7H30M"
                    },
                    {
                      "dato": "2021-01-02",
                      "tid": "PT7H30M"
                    },
                    {
                      "dato": "2021-01-03",
                      "tid": "PT7H30M"
                    },
                    {
                      "dato": "2021-01-04",
                      "tid": "PT7H30M"
                    },
                    {
                      "dato": "2021-01-05",
                      "tid": "PT7H30M"
                    },
                    {
                      "dato": "2021-01-06",
                      "tid": "PT7H30M"
                    },
                    {
                      "dato": "2021-01-07",
                      "tid": "PT7H30M"
                    },
                    {
                      "dato": "2021-01-08",
                      "tid": "PT7H30M"
                    },
                    {
                      "dato": "2021-01-09",
                      "tid": "PT7H30M"
                    },
                    {
                      "dato": "2021-01-10",
                      "tid": "PT7H30M"
                    }
                  ],
                  "jobberIPerioden": "HELT_FRAVÆR"
                },
                "jobberNormaltTimer": 37.5
              },
              "virksomhet": {
                "varigEndring": null,
                "harFlereAktiveVirksomheter": false,
                "næringsinntekt": 9656876,
                "fraOgMed": "2015-01-01",
                "erNyoppstartet": false,
                "yrkesaktivSisteTreFerdigliknedeÅrene": null,
                "næringstype": "ANNEN",
                "tilOgMed": "2021-01-01",
                "fiskerErPåBladB": true,
                "navnPåVirksomheten": "Bjarnes Bakeri",
                "registrertINorge": false,
                "organisasjonsnummer": null,
                "registrertIUtlandet": {
                  "landkode": "CUB",
                  "landnavn": "Cuba"
                },
                "regnskapsfører": null
              }
            },
            "k9FormatSøknad": {
              "språk": "nb",
              "kildesystem": null,
              "mottattDato": "2024-07-19T08:36:49.490Z",
              "søknadId": "$søknadId",
              "søker": {
                "norskIdentitetsnummer": "12345678910"
              },
              "ytelse": {
                "endringsperiode": [],
                "annetDataBruktTilUtledning": null,
                "dataBruktTilUtledning": {
                  "ukjenteArbeidsforhold": [
                    {
                      "normalarbeidstid": {
                        "timerPerUke": "PT8H"
                      },
                      "organisasjonsnavn": "Something Fishy AS",
                      "erAnsatt": true,
                      "arbeiderIPerioden": "HELT_FRAVÆR",
                      "organisasjonsnummer": "926032925"
                    }
                  ],
                  "harMedsøker": true,
                  "samtidigHjemme": true,
                  "soknadDialogCommitSha": "commit-abc-123",
                  "harForståttRettigheterOgPlikter": true,
                  "bekrefterPeriodeOver8Uker": true,
                  "harBekreftetOpplysninger": true
                },
                "nattevåk": {
                  "perioder": {
                    "2020-01-01/2020-01-05": {
                      "tilleggsinformasjon": "Jeg skal ha nattevåk. Basta!"
                    },
                    "2020-01-07/2020-01-10": {
                      "tilleggsinformasjon": "Jeg skal ha nattevåk i perioden også. Basta!"
                    }
                  },
                  "perioderSomSkalSlettes": {}
                },
                "søknadsperiode": [
                  "2020-01-01/2020-01-10"
                ],
                "type": "PLEIEPENGER_SYKT_BARN",
                "bosteder": {
                  "perioder": {
                    "2020-01-01/2020-01-05": {
                      "land": "ESP"
                    },
                    "2020-01-06/2020-01-10": {
                      "land": "NOR"
                    }
                  },
                  "perioderSomSkalSlettes": {}
                },
                "lovbestemtFerie": {
                  "perioder": {
                    "2020-01-01/2020-01-05": {
                      "skalHaFerie": true
                    },
                    "2020-01-06/2020-01-10": {
                      "skalHaFerie": true
                    }
                  }
                },
                "omsorg": {
                  "beskrivelseAvOmsorgsrollen": "Blabla beskrivelse",
                  "relasjonTilBarnet": "MOR"
                },
                "arbeidstid": {
                  "frilanserArbeidstidInfo": null,
                  "arbeidstakerList": [
                    {
                      "organisasjonsnavn": "Something Fishy AS",
                      "arbeidstidInfo": {
                        "perioder": {
                          "2020-01-06/2020-01-10": {
                            "faktiskArbeidTimerPerDag": "PT2H",
                            "jobberNormaltTimerPerDag": "PT8H"
                          },
                          "2018-01-01/2020-01-05": {
                            "faktiskArbeidTimerPerDag": "PT4H",
                            "jobberNormaltTimerPerDag": "PT8H"
                          }
                        }
                      },
                      "organisasjonsnummer": "926032925",
                      "norskIdentitetsnummer": "12345678910"
                    }
                  ],
                  "selvstendigNæringsdrivendeArbeidstidInfo": null
                },
                "tilsynsordning": {
                  "perioder": {
                    "2020-01-01/2020-01-05": {
                      "etablertTilsynTimerPerDag": "PT8H"
                    },
                    "2020-01-06/2020-01-10": {
                      "etablertTilsynTimerPerDag": "PT4H"
                    }
                  }
                },
                "utenlandsopphold": {
                  "perioder": {
                    "2020-01-01/2020-01-05": {
                      "årsak": "barnetInnlagtIHelseinstitusjonDekketEtterAvtaleMedEtAnnetLandOmTrygd",
                      "erSammenMedBarnet": true,
                      "land": "CAN"
                    },
                    "2020-01-06/2020-01-10": {
                      "årsak": "barnetInnlagtIHelseinstitusjonForNorskOffentligRegning",
                      "erSammenMedBarnet": true,
                      "land": "SWE"
                    }
                  },
                  "perioderSomSkalSlettes": {}
                },
                "infoFraPunsj": null,
                "barn": {
                  "fødselsdato": null,
                  "norskIdentitetsnummer": "10987654321"
                },
                "beredskap": {
                  "perioder": {
                    "2020-01-01/2020-01-05": {
                      "tilleggsinformasjon": "Jeg skal være i beredskap. Basta!"
                    },
                    "2020-01-07/2020-01-10": {
                      "tilleggsinformasjon": "Jeg skal være i beredskap i denne perioden også. Basta!"
                    }
                  },
                  "perioderSomSkalSlettes": {}
                },
                "erSammenMedBarnet": null,
                "uttak": {
                  "perioder": {
                    "2020-01-01/2020-01-05": {
                      "timerPleieAvBarnetPerDag": "PT4H"
                    },
                    "2020-01-06/2020-01-10": {
                      "timerPleieAvBarnetPerDag": "PT2H"
                    }
                  }
                },
                "opptjeningAktivitet": {
                  "selvstendigNæringsdrivende": [
                    {
                      "perioder": {
                        "2018-01-01/2020-01-01": {
                          "regnskapsførerNavn": "Regnskapsfører Svensen",
                          "erNyoppstartet": true,
                          "landkode": "NOR",
                          "erVarigEndring": true,
                          "regnskapsførerTlf": "+4799887766",
                          "endringBegrunnelse": "Grunnet Covid-19",
                          "endringDato": "2020-01-01",
                          "virksomhetstyper": [
                            "DAGMAMMA",
                            "ANNEN"
                          ],
                          "bruttoInntekt": 5000000,
                          "registrertIUtlandet": false
                        }
                      },
                      "virksomhetNavn": "Mamsen Bamsen AS",
                      "organisasjonsnummer": "12345678910112233444455667"
                    },
                    {
                      "perioder": {
                        "2015-01-01/2017-01-01": {
                          "erNyoppstartet": false,
                          "landkode": "ESP",
                          "erVarigEndring": false,
                          "virksomhetstyper": [
                            "FISKE"
                          ],
                          "bruttoInntekt": 500000,
                          "registrertIUtlandet": true
                        }
                      },
                      "virksomhetNavn": "Something Fishy AS",
                      "organisasjonsnummer": "54549049090490498048940940"
                    }
                  ],
                  "frilanser": {
                    "startdato": "2020-01-01",
                    "sluttdato": null
                  }
                },
                "trekkKravPerioder": []
              },
              "journalposter": [],
              "begrunnelseForInnsending": {
                "tekst": null
              },
              "versjon": "1.0.0"
            },
            "søknadId": "$søknadId",
            "medlemskap": {
              "skalBoIUtlandetNeste12Mnd": false,
              "harBoddIUtlandetSiste12Mnd": true,
              "utenlandsoppholdSiste12Mnd": [
                {
                  "fraOgMed": "2020-01-02",
                  "landkode": "US",
                  "landnavn": "USA",
                  "tilOgMed": "2020-01-03"
                }
              ],
              "utenlandsoppholdNeste12Mnd": []
            },
            "ferieuttakIPerioden": {
              "ferieuttak": [
                {
                  "fraOgMed": "2020-01-01",
                  "tilOgMed": "2020-01-05"
                },
                {
                  "fraOgMed": "2020-01-07",
                  "tilOgMed": "2020-01-15"
                },
                {
                  "fraOgMed": "2020-02-01",
                  "tilOgMed": "2020-02-05"
                }
              ],
              "skalTaUtFerieIPerioden": true
            },
            "frilans": {
                "harHattInntektSomFrilanser": true,
                "jobberFortsattSomFrilans": true,
                "startdato": "2019-01-01",
                "arbeidsforhold": {
                    "arbeidIPeriode": {
                        "enkeltdager": [
                            {
                                "dato": "2021-01-01",
                                "tid": "PT7H30M"
                            },
                            {
                                "dato": "2021-01-02",
                                "tid": "PT7H30M"
                            },
                            {
                                "dato": "2021-01-03",
                                "tid": "PT7H30M"
                            }
                        ],
                        "jobberIPerioden": "HELT_FRAVÆR"
                    },
                    "jobberNormaltTimer": 7.5
                },
                "sluttdato": null
            },
            "harVærtEllerErVernepliktig": true,
            "opptjeningIUtlandet": [
              {
                "fraOgMed": "2020-01-01",
                "tilOgMed": "2020-10-01",
                "navn": "Yolo AS",
                "land": {
                  "landkode": "NDL",
                  "landnavn": "Nederland"
                },
                "opptjeningType": "ARBEIDSTAKER"
              }
            ],
            "stønadGodtgjørelse": {
              "mottarStønadGodtgjørelse": true,
              "startdato": "2021-01-01",
              "sluttdato": "2021-01-21"
            },
            "fraOgMed": "2022-01-01",
            "språk": "nb",
            "utenlandskNæring": [
              {
                "fraOgMed": "2020-01-09",
                "næringstype": "FISKE",
                "tilOgMed": null,
                "land": {
                  "landkode": "NDL",
                  "landnavn": "Nederland"
                },
                "navnPåVirksomheten": "Fiskeriet AS",
                "organisasjonsnummer": "123ABC"
              }
            ],
            "arbeidsgivere": [
              {
                "erAnsatt": true,
                "arbeidsforhold": {
                  "arbeidIPeriode": {
                    "enkeltdager": [
                      {
                        "dato": "2022-01-01",
                        "tid": "PT4H"
                      },
                      {
                        "dato": "2022-01-02",
                        "tid": "PT4H"
                      },
                      {
                        "dato": "2022-01-03",
                        "tid": "PT4H"
                      },
                      {
                        "dato": "2022-01-04",
                        "tid": "PT4H"
                      },
                      {
                        "dato": "2022-02-01",
                        "tid": "PT4H"
                      },
                      {
                        "dato": "2022-02-02",
                        "tid": "PT4H"
                      },
                      {
                        "dato": "2022-04-10",
                        "tid": "PT4H"
                      }
                    ],
                    "jobberIPerioden": "REDUSERT"
                  },
                  "jobberNormaltTimer": 40
                },
                "navn": "Arbeidsplassen AS",
                "sluttetFørSøknadsperiode": null,
                "organisasjonsnummer": "917755736"
              },
              {
                "erAnsatt": false,
                "arbeidsforhold": null,
                "navn": "JobberIkkeHerLenger AS",
                "sluttetFørSøknadsperiode": false,
                "organisasjonsnummer": "977155436"
              }
            ],
            "barn": {
              "aktørId": "11111111111",
              "fødselsdato": null,
              "navn": "OLE DOLE",
              "norskIdentifikator": "02119970078",
              "årsakManglerIdentitetsnummer": null,
              "relasjonTilBarnet": "ANNET",
              "relasjonTilBarnetBeskrivelse": "Blaabla annet"
            },
            "mottatt": "$mottatt",
            "søker": {
              "etternavn": "ÅNSNES",
              "mellomnavn": "ØVERBØ",
              "aktørId": "123456",
              "fødselsdato": "1990-09-29",
              "fornavn": "Ærling",
              "fødselsnummer": "29099012345"
            },
            "tilOgMed": "2022-02-01",
            "dokumentId": [
              [
                "123456789",
                "987654321"
              ],
              [
                "123"
              ],
              [
                "456"
              ]
            ],
            "apiDataVersjon": null,
            "harBekreftetOpplysninger": true
          }
        """.trimIndent()

}
