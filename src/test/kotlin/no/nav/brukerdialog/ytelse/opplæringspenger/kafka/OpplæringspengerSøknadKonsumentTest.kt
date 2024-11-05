package no.nav.brukerdialog.ytelse.opplæringspenger.kafka

import io.mockk.coVerify
import no.nav.brukerdialog.AbstractIntegrationTest
import no.nav.brukerdialog.utils.MockMvcUtils.sendInnSøknad
import no.nav.brukerdialog.utils.TokenTestUtils.hentToken
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.OLPTopologyConfiguration.Companion.OLP_CLEANUP_TOPIC
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.OLPTopologyConfiguration.Companion.OLP_MOTTATT_TOPIC
import no.nav.brukerdialog.ytelse.opplæringspenger.kafka.OLPTopologyConfiguration.Companion.OLP_PREPROSESSERT_TOPIC
import no.nav.brukerdialog.ytelse.opplæringspenger.utils.SøknadUtils.Companion.defaultSøknad
import org.junit.jupiter.api.Test

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
            fødselsattestVedleggUrls = listOf()
        )

        mockMvc.sendInnSøknad(defaultSøknad, mockOAuth2Server.hentToken())

        coVerify(exactly = 1, timeout = 120 * 1000) {
            k9DokumentMellomlagringService.slettDokumenter(any(), any())
        }
    }
//
//    @Test
//    fun `Forvent at melding bli prosessert på 5 forsøk etter 4 feil`() {
//        val søknadId = UUID.randomUUID().toString()
//        val mottatt = ZonedDateTime.parse("2024-07-19T08:36:49.490Z", zonedDateTimeFormatter)
//        val søknadMottatt = PSBSøknadUtils.psbMottattSøknad(søknadId = søknadId, mottatt = mottatt)
//        val correlationId = UUID.randomUUID().toString()
//        val metadata = MetaInfo(version = 1, correlationId = correlationId)
//        val topicEntry = TopicEntry(metadata, søknadMottatt)
//        val topicEntryJson = objectMapper.writeValueAsString(topicEntry)
//
//        coEvery { k9DokumentMellomlagringService.lagreDokument(any()) }
//            .throws(IllegalStateException("Feilet med lagring av dokument..."))
//            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
//            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
//            .andThenThrows(IllegalStateException("Feilet med lagring av dokument..."))
//            .andThenMany(listOf("123456789", "987654321").map { URI("http://localhost:8080/dokument/$it") })
//
//        producer.leggPåTopic(key = søknadId, value = topicEntryJson, topic = PSB_MOTTATT_TOPIC)
//        val lesMelding = consumer.lesMelding(key = søknadId, topic = PSB_PREPROSESSERT_TOPIC).value()
//
//        val preprosessertSøknadJson = JSONObject(lesMelding).getJSONObject("data").toString()
//        JSONAssert.assertEquals(preprosessertSøknadSomJson(søknadId, mottatt.toString()), preprosessertSøknadJson, true)
//    }
//
//    @Language("JSON")
//    private fun preprosessertSøknadSomJson(søknadId: String, mottatt: String) = """
//         {
//            "apiDataVersjon": null,
//            "søknadId": "$søknadId",
//            "mottatt": "$mottatt",
//            "språk": "nb",
//            "søker": {
//              "aktørId": "123456",
//              "etternavn": "Nordmann",
//              "fornavn": "Ola",
//              "fødselsnummer": "02119970078",
//               "fødselsdato": "1999-11-02",
//              "mellomnavn": "Mellomnavn"
//            },
//            "barn": {
//              "aktørId": "11111111111",
//              "fødselsdato": null,
//              "fødselsnummer": "02119970078",
//              "navn": "Ole Dole",
//              "årsakManglerIdentitetsnummer": null
//            },
//            "tilOgMed": "2021-01-01",
//            "fraOgMed": "2021-01-01",
//            "arbeidsgivere": [
//              {
//                "arbeidsforhold": {
//                  "arbeidIPeriode": {
//                    "type": "ARBEIDER_VANLIG",
//                    "redusertArbeid": null
//                  },
//                  "normalarbeidstid": {
//                    "timerPerUkeISnitt": "PT37H30M"
//                  }
//                },
//                "erAnsatt": true,
//                "navn": "Peppes",
//                "organisasjonsnummer": "917755736",
//                "sluttetFørSøknadsperiode": null
//              },
//              {
//                "arbeidsforhold": {
//                  "arbeidIPeriode": {
//                    "type": "ARBEIDER_VANLIG",
//                    "redusertArbeid": null
//                  },
//                  "normalarbeidstid": {
//                    "timerPerUkeISnitt": "PT37H30M"
//                  }
//                },
//                "erAnsatt": true,
//                "navn": "Pizzabakeren",
//                "organisasjonsnummer": "917755736",
//                "sluttetFørSøknadsperiode": null
//              }
//            ],
//            "barnRelasjon": null,
//            "barnRelasjonBeskrivelse": null,
//            "beredskap": {
//              "beredskap": true,
//              "tilleggsinformasjon": "I Beredskap"
//            },
//            "ferieuttakIPerioden": {
//              "ferieuttak": [
//                {
//                  "fraOgMed": "2020-01-07",
//                  "tilOgMed": "2020-01-08"
//                },
//                {
//                  "fraOgMed": "2020-01-09",
//                  "tilOgMed": "2020-01-10"
//                }
//              ],
//              "skalTaUtFerieIPerioden": true
//            },
//            "frilans": {
//              "arbeidsforhold": {
//                "arbeidIPeriode": {
//                  "type": "ARBEIDER_VANLIG",
//                  "redusertArbeid": null
//                },
//                "normalarbeidstid": {
//                  "timerPerUkeISnitt": "PT37H30M"
//                }
//              },
//              "startetFørSisteTreHeleMåneder": true,
//              "harInntektSomFrilanser": true,
//              "jobberFortsattSomFrilans": true,
//              "sluttdato": null,
//              "startdato": "2019-01-01",
//              "type": "FRILANS",
//              "misterHonorar": true
//            },
//          "stønadGodtgjørelse": {
//           "mottarStønadGodtgjørelse": true,
//           "startdato": "2023-03-13",
//           "sluttdato": "2023-03-29"
//          },
//            "harBekreftetOpplysninger": true,
//            "harForstattRettigheterOgPlikter": true,
//            "harVærtEllerErVernepliktig": true,
//            "medlemskap": {
//              "harBoddIUtlandetSiste12Mnd": true,
//              "skalBoIUtlandetNeste12Mnd": false,
//              "utenlandsoppholdNeste12Mnd": [],
//              "utenlandsoppholdSiste12Mnd": [
//                {
//                  "fraOgMed": "2020-01-02",
//                  "landkode": "US",
//                  "landnavn": "USA",
//                  "tilOgMed": "2020-01-03"
//                }
//              ]
//            },
//            "nattevåk": {
//              "harNattevåk": true,
//              "tilleggsinformasjon": "Har Nattevåk"
//            },
//            "omsorgstilbud": null,
//            "opptjeningIUtlandet": [
//              {
//                "fraOgMed": "2020-01-01",
//                "land": {
//                  "landkode": "NDL",
//                  "landnavn": "Nederland"
//                },
//                "navn": "Yolo AS",
//                "opptjeningType": "ARBEIDSTAKER",
//                "tilOgMed": "2020-10-01"
//              }
//            ],
//            "selvstendigNæringsdrivende": {
//              "arbeidsforhold": {
//                "arbeidIPeriode": {
//                  "type": "ARBEIDER_VANLIG",
//                  "redusertArbeid": null
//                },
//                "normalarbeidstid": {
//                  "timerPerUkeISnitt": "PT37H30M"
//                }
//              },
//              "harInntektSomSelvstendig": true,
//              "virksomhet": {
//                "fiskerErPåBladB": null,
//                "fraOgMed": "2021-01-01",
//                "harFlereAktiveVirksomheter": null,
//                "navnPåVirksomheten": "Kjells Møbelsnekkeri",
//                "næringsinntekt": null,
//                "næringstype": "ANNEN",
//                "organisasjonsnummer": "111111",
//                "registrertINorge": true,
//                "registrertIUtlandet": null,
//                "regnskapsfører": null,
//                "tilOgMed": "2021-01-10",
//                "varigEndring": null,
//                "yrkesaktivSisteTreFerdigliknedeÅrene": {
//                  "oppstartsdato": "2021-01-01"
//                }
//              }
//            },
//            "utenlandskNæring": [
//              {
//                "fraOgMed": "2020-01-01",
//                "land": {
//                  "landkode": "NDL",
//                  "landnavn": "Nederland"
//                },
//                "navnPåVirksomheten": "Dagmamma AS",
//                "næringstype": "DAGMAMMA",
//                "organisasjonsnummer": null,
//                "tilOgMed": null
//              }
//            ],
//            "utenlandsoppholdIPerioden": {
//              "opphold": [
//                {
//                  "erBarnetInnlagt": true,
//                  "erUtenforEøs": true,
//                  "erSammenMedBarnet": true,
//                  "fraOgMed": "2020-01-01",
//                  "landkode": "BAH",
//                  "landnavn": "Bahamas",
//                  "perioderBarnetErInnlagt": [
//                    {
//                      "fraOgMed": "2020-01-01",
//                      "tilOgMed": "2020-01-01"
//                    },
//                    {
//                      "fraOgMed": "2020-01-03",
//                      "tilOgMed": "2020-01-04"
//                    }
//                  ],
//                  "tilOgMed": "2020-01-10",
//                  "årsak": "ANNET"
//                },
//                {
//                  "erBarnetInnlagt": true,
//                  "erUtenforEøs": false,
//                  "erSammenMedBarnet": true,
//                  "fraOgMed": "2020-01-01",
//                  "landkode": "BHS",
//                  "landnavn": "Svergie",
//                  "perioderBarnetErInnlagt": [
//                    {
//                      "fraOgMed": "2020-01-01",
//                      "tilOgMed": "2020-01-01"
//                    },
//                    {
//                      "fraOgMed": "2020-01-03",
//                      "tilOgMed": "2020-01-04"
//                    },
//                    {
//                      "fraOgMed": "2020-01-05",
//                      "tilOgMed": "2020-01-05"
//                    }
//                  ],
//                  "tilOgMed": "2020-01-10",
//                  "årsak": "ANNET"
//                }
//              ],
//              "skalOppholdeSegIUtlandetIPerioden": true
//            },
//            "dokumentId": [
//                [
//                  "123456789",
//                  "987654321"
//                ],
//                [
//                  "123"
//                ],
//                [
//                  "456"
//                ],
//                [
//                  "789"
//                ]
//              ],
//            "k9FormatSøknad": {
//              "søknadId": "$søknadId",
//              "mottattDato": "$mottatt",
//              "språk": "nb",
//              "søker": {
//                "norskIdentitetsnummer": "12345678910"
//              },
//              "versjon": "1.0.0",
//              "ytelse": {
//                "type": "PLEIEPENGER_SYKT_BARN",
//                "arbeidstid": {
//                  "arbeidstakerList": [
//                    {
//                      "arbeidstidInfo": {
//                        "perioder": {
//                          "2018-01-01/2020-01-05": {
//                            "faktiskArbeidTimerPerDag": "PT4H",
//                            "jobberNormaltTimerPerDag": "PT8H"
//                          },
//                          "2020-01-06/2020-01-10": {
//                            "faktiskArbeidTimerPerDag": "PT2H",
//                            "jobberNormaltTimerPerDag": "PT8H"
//                          }
//                        }
//                      },
//                      "norskIdentitetsnummer": "12345678910",
//                      "organisasjonsnummer": "926032925",
//                      "organisasjonsnavn": "Something Fishy AS"
//                    }
//                  ],
//                  "frilanserArbeidstidInfo": null,
//                  "selvstendigNæringsdrivendeArbeidstidInfo": null
//                },
//                "barn": {
//                  "fødselsdato": null,
//                  "norskIdentitetsnummer": "10987654321"
//                },
//                "erSammenMedBarnet": null,
//                "beredskap": {
//                  "perioder": {
//                    "2020-01-01/2020-01-05": {
//                      "tilleggsinformasjon": "Jeg skal være i beredskap. Basta!"
//                    },
//                    "2020-01-07/2020-01-10": {
//                      "tilleggsinformasjon": "Jeg skal være i beredskap i denne perioden også. Basta!"
//                    }
//                  },
//                  "perioderSomSkalSlettes": {}
//                },
//                "bosteder": {
//                  "perioder": {
//                    "2020-01-01/2020-01-05": {
//                      "land": "ESP"
//                    },
//                    "2020-01-06/2020-01-10": {
//                      "land": "NOR"
//                    }
//                  },
//                  "perioderSomSkalSlettes": {}
//                },
//                "annetDataBruktTilUtledning": null,
//                "dataBruktTilUtledning": {
//                  "bekrefterPeriodeOver8Uker": true,
//                  "harBekreftetOpplysninger": true,
//                  "harForståttRettigheterOgPlikter": true,
//                  "harMedsøker": true,
//                  "samtidigHjemme": true,
//                  "soknadDialogCommitSha": "commit-abc-123",
//                  "ukjenteArbeidsforhold": [
//                    {
//                      "organisasjonsnummer": "926032925",
//                      "organisasjonsnavn": "Something Fishy AS",
//                      "erAnsatt": true,
//                      "normalarbeidstid": {
//                        "timerPerUke": "PT8H"
//                      },
//                      "arbeiderIPerioden": "HELT_FRAVÆR"
//                    }
//                  ]
//                },
//                "endringsperiode": [],
//                "infoFraPunsj": null,
//                "lovbestemtFerie": {
//                  "perioder": {
//                    "2020-01-01/2020-01-05": {
//                      "skalHaFerie": true
//                    },
//                    "2020-01-06/2020-01-10": {
//                      "skalHaFerie": true
//                    }
//                  }
//                },
//                "nattevåk": {
//                  "perioder": {
//                    "2020-01-01/2020-01-05": {
//                      "tilleggsinformasjon": "Jeg skal ha nattevåk. Basta!"
//                    },
//                    "2020-01-07/2020-01-10": {
//                      "tilleggsinformasjon": "Jeg skal ha nattevåk i perioden også. Basta!"
//                    }
//                  },
//                  "perioderSomSkalSlettes": {}
//                },
//                "omsorg": {
//                  "beskrivelseAvOmsorgsrollen": "Blabla beskrivelse",
//                  "relasjonTilBarnet": "MOR"
//                },
//                "opptjeningAktivitet": {
//                  "frilanser": {
//                    "sluttdato": null,
//                    "startdato": "2020-01-01"
//                  },
//                  "selvstendigNæringsdrivende": [
//                    {
//                      "organisasjonsnummer": "12345678910112233444455667",
//                      "perioder": {
//                        "2018-01-01/2020-01-01": {
//                          "bruttoInntekt": 5000000,
//                          "endringBegrunnelse": "Grunnet Covid-19",
//                          "endringDato": "2020-01-01",
//                          "erNyoppstartet": true,
//                          "erVarigEndring": true,
//                          "landkode": "NOR",
//                          "registrertIUtlandet": false,
//                          "regnskapsførerNavn": "Regnskapsfører Svensen",
//                          "regnskapsførerTlf": "+4799887766",
//                          "virksomhetstyper": [
//                            "DAGMAMMA",
//                            "ANNEN"
//                          ]
//                        }
//                      },
//                      "virksomhetNavn": "Mamsen Bamsen AS"
//                    },
//                    {
//                      "organisasjonsnummer": "54549049090490498048940940",
//                      "perioder": {
//                        "2015-01-01/2017-01-01": {
//                          "bruttoInntekt": 500000,
//                          "erNyoppstartet": false,
//                          "erVarigEndring": false,
//                          "landkode": "ESP",
//                          "registrertIUtlandet": true,
//                          "virksomhetstyper": [
//                            "FISKE"
//                          ]
//                        }
//                      },
//                      "virksomhetNavn": "Something Fishy AS"
//                    }
//                  ]
//                },
//                "søknadsperiode": [
//                  "2020-01-01/2020-01-10"
//                ],
//                "tilsynsordning": {
//                  "perioder": {
//                    "2020-01-01/2020-01-05": {
//                      "etablertTilsynTimerPerDag": "PT8H"
//                    },
//                    "2020-01-06/2020-01-10": {
//                      "etablertTilsynTimerPerDag": "PT4H"
//                    }
//                  }
//                },
//                "trekkKravPerioder": [],
//                "utenlandsopphold": {
//                  "perioder": {
//                    "2020-01-01/2020-01-05": {
//                      "land": "CAN",
//                      "årsak": "barnetInnlagtIHelseinstitusjonDekketEtterAvtaleMedEtAnnetLandOmTrygd",
//                      "erSammenMedBarnet": true
//                    },
//                    "2020-01-06/2020-01-10": {
//                      "land": "SWE",
//                      "årsak": "barnetInnlagtIHelseinstitusjonForNorskOffentligRegning",
//                      "erSammenMedBarnet": true
//                    }
//                  },
//                  "perioderSomSkalSlettes": {}
//                },
//                "uttak": {
//                  "perioder": {
//                    "2020-01-01/2020-01-05": {
//                      "timerPleieAvBarnetPerDag": "PT4H"
//                    },
//                    "2020-01-06/2020-01-10": {
//                      "timerPleieAvBarnetPerDag": "PT2H"
//                    }
//                  }
//                }
//              },
//              "begrunnelseForInnsending": {
//                "tekst": null
//              },
//              "kildesystem": null,
//              "journalposter": []
//            }
//          }
//        """.trimIndent()

}
